package com.ww.springboot.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.AuthTypeEnum;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.AesUtil;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.enums.v3.CertAlgorithmTypeEnum;
import com.ijpay.wxpay.model.v3.*;
import com.ww.springboot.dao.WxPayV3Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-21 18:04
 * @Description: TODO
 * @Version: 1.0
 */
@Controller
@RequestMapping("/v3")
public class WxPayV3Controller {
    private final static int OK = 200;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private WxPayV3Bean wxPayV3Bean;
    private String serialNo;
    private String getSerialNumber() {
        if (StrUtil.isEmpty(serialNo)) {
            // 获取证书序列号
            X509Certificate certificate = PayKit.getCertificate(wxPayV3Bean.getPrivateCertPath());
            if (null != certificate) {
                serialNo = certificate.getSerialNumber().toString(16).toUpperCase();
                // 提前两天检查证书是否有效
                boolean isValid = PayKit.checkCertificateIsValid(certificate, wxPayV3Bean.getMchId(), -2);
                log.info("证书是否可用 {} 证书有效期为 {}", isValid, DateUtil.format(certificate.getNotAfter(), DatePattern.NORM_DATETIME_PATTERN));
            }
           // System.out.println("输出证书信息:\n" + certificate.toString());
           // // 输出关键信息，截取部分并进行标记
           // System.out.println("证书序列号:" + certificate.getSerialNumber().toString(16));
           // System.out.println("版本号:" + certificate.getVersion());
           // System.out.println("签发者：" + certificate.getIssuerDN());
           // System.out.println("有效起始日期：" + certificate.getNotBefore());
           // System.out.println("有效终止日期：" + certificate.getNotAfter());
           // System.out.println("主体名：" + certificate.getSubjectDN());
           // System.out.println("签名算法：" + certificate.getSigAlgName());
           // System.out.println("签名：" + certificate.getSignature().toString());
        }
        System.out.println("serialNo:" + serialNo);
        return serialNo;
    }


    @RequestMapping("/getSerialNumber")
    @ResponseBody
    public String serialNumber() {
        return getSerialNumber();
    }

    private String savePlatformCert(String associatedData, String nonce, String cipherText, String algorithm, String certPath) {
        try {
            String key3 = wxPayV3Bean.getApiKey3();
            String publicKey;
            if (StrUtil.equals(algorithm, AuthTypeEnum.SM2.getPlatformCertAlgorithm())) {
                publicKey = PayKit.sm4DecryptToString(key3, cipherText, nonce, associatedData);
            } else {
                AesUtil aesUtil = new AesUtil(wxPayV3Bean.getApiKey3().getBytes(StandardCharsets.UTF_8));
                // 平台证书密文解密
                // encrypt_certificate 中的  associated_data nonce  ciphertext
                publicKey = aesUtil.decryptToString(
                        associatedData.getBytes(StandardCharsets.UTF_8),
                        nonce.getBytes(StandardCharsets.UTF_8),
                        cipherText
                );
            }
            if (StrUtil.isNotEmpty(publicKey)) {
                // 保存证书
                FileWriter writer = new FileWriter(certPath);
                writer.write(publicKey);
                // 获取平台证书序列号
                X509Certificate certificate = PayKit.getCertificate(new ByteArrayInputStream(publicKey.getBytes()));
                return certificate.getSerialNumber().toString(16).toUpperCase();
            }
            return "";
        } catch (Exception e) {
            log.error("保存平台证书异常", e);
            return e.getMessage();
        }
    }

    @RequestMapping("/get")
    @ResponseBody
    public String v3Get() {
        // 获取平台证书列表
        try {
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.GET,
                    WxDomainEnum.CHINA.toString(),
                    CertAlgorithmTypeEnum.getCertSuffixUrl(CertAlgorithmTypeEnum.RSA.getCode()),
                    wxPayV3Bean.getMchId(),
                    getSerialNumber(),
                    null,
                    wxPayV3Bean.getPrivateKeyPath(),
                    "",
                    AuthTypeEnum.RSA.getCode()
            );
            Map<String, List<String>> headers = response.getHeaders();
            log.info("请求头: {}", headers);
            String timestamp = response.getHeader("Wechatpay-Timestamp");
            String nonceStr = response.getHeader("Wechatpay-Nonce");
            String serialNumber = response.getHeader("Wechatpay-Serial");
            String signature = response.getHeader("Wechatpay-Signature");

            String body = response.getBody();
            int status = response.getStatus();

            log.info("serialNumber: {}", serialNumber);
            log.info("status: {}", status);
            log.info("body: {}", body);
            int isOk = 200;
            if (status == isOk) {
                JSONObject jsonObject = JSONUtil.parseObj(body);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                // 默认认为只有一个平台证书
                JSONObject encryptObject = dataArray.getJSONObject(0);
                JSONObject encryptCertificate = encryptObject.getJSONObject("encrypt_certificate");
                String associatedData = encryptCertificate.getStr("associated_data");
                String cipherText = encryptCertificate.getStr("ciphertext");
                String nonce = encryptCertificate.getStr("nonce");
                String algorithm = encryptCertificate.getStr("algorithm");
                String serialNo = encryptObject.getStr("serial_no");
                final String platSerialNo = savePlatformCert(associatedData, nonce, cipherText, algorithm, wxPayV3Bean.getPlatformCertPath());
                log.info("平台证书序列号: {} serialNo: {}", platSerialNo, serialNo);
                // 根据证书序列号查询对应的证书来验证签名结果
                boolean verifySignature = WxPayKit.verifySignature(response, wxPayV3Bean.getPlatformCertPath());
                log.info("verifySignature:{}", verifySignature);
            }
            return body;
        } catch (Exception e) {
            log.error("获取平台证书列表异常", e);
            return null;
        }
    }


    @RequestMapping("/jsApiPay")
    @ResponseBody
    public String jsApiPay(@RequestParam(value = "openId", required = false, defaultValue = "o-_-itxuXeGW3O1cxJ7FXNmq8Wf8") String openId) {
        try {
            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    .setAppid(wxPayV3Bean.getAppId())
                    .setMchid(wxPayV3Bean.getMchId())
                    .setDescription("IJPay 让支付触手可及")
                    .setOut_trade_no(PayKit.generateStr())
                    .setTime_expire(timeExpire)
                    .setAttach("微信系开发脚手架 https://gitee.com/javen205/TNWX")
                    .setNotify_url(wxPayV3Bean.getDomain().concat("/v3/payNotify"))
                    .setAmount(new Amount().setTotal(1))
                    .setPayer(new Payer().setOpenid(openId));

            log.info("统一下单参数 {}", JSONUtil.toJsonStr(unifiedOrderModel));
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.JS_API_PAY.toString(),
                    wxPayV3Bean.getMchId(),
                    getSerialNumber(),
                    null,
                    wxPayV3Bean.getPrivateKeyPath(),
                    JSONUtil.toJsonStr(unifiedOrderModel)
            );
            log.info("统一下单响应 {}", response);
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayV3Bean.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            if (response.getStatus() == OK && verifySignature) {
                String body = response.getBody();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String prepayId = jsonObject.getStr("prepay_id");
                Map<String, String> map = WxPayKit.jsApiCreateSign(wxPayV3Bean.getAppId(), prepayId, wxPayV3Bean.getPrivateKeyPath());
                log.info("唤起支付参数:{}", map);
                return JSONUtil.toJsonStr(map);
            }
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
    }


	@RequestMapping(value = "/payNotify", method = {RequestMethod.POST, RequestMethod.GET})
	@ResponseBody
	public void payNotify(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> map = new HashMap<>(12);
		try {
			String timestamp = request.getHeader("Wechatpay-Timestamp");
			String nonce = request.getHeader("Wechatpay-Nonce");
			String serialNo = request.getHeader("Wechatpay-Serial");
			String signature = request.getHeader("Wechatpay-Signature");

			log.info("timestamp:{} nonce:{} serialNo:{} signature:{}", timestamp, nonce, serialNo, signature);
			String result = HttpKit.readData(request);
			log.info("支付通知密文 {}", result);

			// 需要通过证书序列号查找对应的证书，verifyNotify 中有验证证书的序列号
			String plainText = WxPayKit.verifyNotify(serialNo, result, signature, nonce, timestamp,
				wxPayV3Bean.getApiKey3(), wxPayV3Bean.getPlatformCertPath());

			log.info("支付通知明文 {}", plainText);

			if (StrUtil.isNotEmpty(plainText)) {
				response.setStatus(200);
				map.put("code", "SUCCESS");
				map.put("message", "SUCCESS");
			} else {
				response.setStatus(500);
				map.put("code", "ERROR");
				map.put("message", "签名错误");
			}
			response.setHeader("Content-type", ContentType.JSON.toString());
			response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
			response.flushBuffer();
		} catch (Exception e) {
			log.error("系统异常", e);
		}
	}

    @RequestMapping("/refund")
    @ResponseBody
    public String refund(@RequestParam(required = false) String transactionId,
                         @RequestParam(required = false) String outTradeNo) {
        try {
            String outRefundNo = PayKit.generateStr();
            log.info("商户退款单号: {}", outRefundNo);

            List<RefundGoodsDetail> list = new ArrayList<>();
            RefundGoodsDetail refundGoodsDetail = new RefundGoodsDetail()
                    .setMerchant_goods_id("123")
                    .setGoods_name("IJPay 测试")
                    .setUnit_price(1)
                    .setRefund_amount(1)
                    .setRefund_quantity(1);
            list.add(refundGoodsDetail);

            RefundModel refundModel = new RefundModel()
                    .setOut_refund_no(outRefundNo)
                    .setReason("IJPay 测试退款")
                    .setNotify_url(wxPayV3Bean.getDomain().concat("/v3/refundNotify"))
                    .setAmount(new RefundAmount().setRefund(1).setTotal(1).setCurrency("CNY"))
                    .setGoods_detail(list);

            if (StrUtil.isNotEmpty(transactionId)) {
                refundModel.setTransaction_id(transactionId);
            }
            if (StrUtil.isNotEmpty(outTradeNo)) {
                refundModel.setOut_trade_no(outTradeNo);
            }
            log.info("退款参数 {}", JSONUtil.toJsonStr(refundModel));
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.REFUND.toString(),
                    wxPayV3Bean.getMchId(),
                    getSerialNumber(),
                    null,
                    wxPayV3Bean.getPrivateKeyPath(),
                    JSONUtil.toJsonStr(refundModel)
            );
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayV3Bean.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            log.info("退款响应 {}", response);

            if (verifySignature) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
        return null;
    }
}
