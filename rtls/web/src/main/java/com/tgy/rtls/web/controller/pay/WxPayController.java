package com.tgy.rtls.web.controller.pay;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.AuthTypeEnum;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.HttpKit;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.model.v3.*;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.OrderNoUtils;
import com.tgy.rtls.data.entity.pay.OrderInfo;
import com.tgy.rtls.data.entity.pay.Product;
import com.tgy.rtls.data.entity.pay.WxPayBean;
import com.tgy.rtls.data.enums.PayType;
import com.tgy.rtls.data.service.park.ProductService;
import com.tgy.rtls.data.service.pay.OrderInfoService;
import lombok.RequiredArgsConstructor;
import me.chanjar.weixin.mp.api.WxMpService;
import com.tgy.rtls.data.entity.pay.vo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-21 18:04
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/wechat")
@CrossOrigin
@RequiredArgsConstructor
public class WxPayController {
    private final static int OK = 200;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private OrderInfoService orderInfoService;
    @Resource
    private ProductService productService;
    @Resource
    private WxPayBean wxPayBean;

    @GetMapping("list")
    public R list(){
        List<Product> list = productService.list();
        return R.ok().data("productList", list);
    }
    @RequestMapping("/nativePay")
    @ResponseBody
    public R nativePay(Integer productId, int map) {
        try {
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, map, PayType.WXPAY.getType());
            String codeUrl = orderInfo.getCodeUrl();
            if(!NullUtils.isEmpty(codeUrl)){
                log.info("订单已存在，二维码已保存");
                //返回二维码
                Map<String, Object> hashMap = new HashMap<>();
                hashMap.put("codeUrl", codeUrl);
                hashMap.put("orderNo", orderInfo.getOrderNo());
                return R.ok().setCode(200).setData(hashMap);
            }
            Map<String,Object> mapJson = new HashMap<>();
            mapJson.put("map",map);
            mapJson.put("productId",productId);
            int money=new BigDecimal(String.valueOf(orderInfo.getTotalFee())).movePointRight(2).intValue();

            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 12);
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    .setAppid(wxPayBean.getAppId())
                    .setMchid(wxPayBean.getMchId())
                    .setDescription(orderInfo.getTitle())
                    .setOut_trade_no(orderInfo.getOrderNo())
                    .setTime_expire(timeExpire)
                    .setAttach(JSON.toJSONString(mapJson))
                    .setNotify_url(wxPayBean.getDomain().concat("/wechat/payNotify"))
                    .setAmount(new Amount().setTotal(money).setCurrency("CNY"));

            log.info("统一下单参数 {}", JSONUtil.toJsonStr(unifiedOrderModel));
            Gson gson = new Gson();
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.NATIVE_PAY.toString(),
                    wxPayBean.getMchId(),
                    OrderNoUtils.SerialNumberUtils.getSerialNumber(wxPayBean.getPrivateCertPath(), wxPayBean.getMchId()),
                    null,
                    wxPayBean.getPrivateKeyPath(),
                    JSONUtil.toJsonStr(unifiedOrderModel),
                    AuthTypeEnum.RSA.getCode()
            );
            log.info("统一下单响应 {}", response);
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayBean.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);

            Map<String, String> resultMap = gson.fromJson(response.getBody(), HashMap.class);
            codeUrl = resultMap.get("code_url");

            //保存二维码
            String orderNo = orderInfo.getOrderNo();
            orderInfoService.saveCodeUrl(orderNo, codeUrl);
            //返回二维码
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("codeUrl", codeUrl);//bug
            mapData.put("orderNo", orderInfo.getOrderNo());

            return R.ok().setCode(200).setData(mapData);
        } catch (Exception e) {
            log.error("系统异常", e);
            return R.error().setMessage(e.getMessage());
        }
    }


    @PostMapping(value = "/payNotify")
    @ResponseBody
    public void payNotify(HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> map = new HashMap<>(12);
        Gson gson = new Gson();
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
                    wxPayBean.getApiKey3(), wxPayBean.getPlatformCertPath());
            log.info("支付通知明文 {}", plainText);
            HashMap<String,Object> plainTextMap = gson.fromJson(plainText, HashMap.class);
            log.info("plainTextMap {}", plainTextMap);
            plainTextMap.put("content", plainText);


            if (!StrUtil.isNotEmpty(plainText)) {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "签名错误");
                return;
            }

            String outTradeNo = (String) plainTextMap.get("out_trade_no");
            OrderInfo order = orderInfoService.getOrderByOrderNo(outTradeNo);
            if(order == null){
                log.error("订单不存在");
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "订单不存在");
                return;
            }
            log.info("通知验签成功");
            orderInfoService.processOrder(plainTextMap,order.getProductId(),order.getMap());
            response.setStatus(200);
            map.put("code", "SUCCESS");
            map.put("message", "SUCCESS");
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            log.error("系统异常", e);
            e.printStackTrace();
            response.setStatus(500);
            map.put("code", "SUCCESS");
            map.put("message", "SUCCESS");
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
                    .setNotify_url(wxPayBean.getDomain().concat("/wechat/refundNotify"))
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
                    wxPayBean.getMchId(),
                    OrderNoUtils.SerialNumberUtils.getSerialNumber(wxPayBean.getPrivateCertPath(), wxPayBean.getMchId()),
                    null,
                    wxPayBean.getPrivateKeyPath(),
                    JSONUtil.toJsonStr(refundModel)
            );
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayBean.getPlatformCertPath());
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
    private final WxMpService wxMpService;


    @RequestMapping("/jsApiPay")
    @ResponseBody
    public String jsApiPay(@RequestParam(value = "openId", required = false, defaultValue = "oMgIe65An7ijz1-KecvGJLvipWtk") String openId,String code) {
    // public String jsApiPay(@RequestParam(value = "openId", required = false, defaultValue = "o-_-itxuXeGW3O1cxJ7FXNmq8Wf8") String openId) {
        try {
            // WxOAuth2UserInfo callback = callback(code);
            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    .setAppid(wxPayBean.getAppId())
                    .setMchid(wxPayBean.getMchId())
                    .setDescription("IJPay 让支付触手可及")
                    .setOut_trade_no(PayKit.generateStr())
                    .setTime_expire(timeExpire)
                    .setAttach("微信系开发脚手架 https://gitee.com/javen205/TNWX")
                    .setNotify_url(wxPayBean.getDomain().concat("/wechat/jsApiPayNotify"))
                    .setAmount(new Amount().setTotal(1))
                    .setPayer(new Payer().setOpenid(openId));

            log.info("统一下单参数 {}", JSONUtil.toJsonStr(unifiedOrderModel));
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.JS_API_PAY.toString(),
                    wxPayBean.getMchId(),
                    OrderNoUtils.SerialNumberUtils.getSerialNumber(wxPayBean.getPrivateCertPath(), wxPayBean.getMchId()),
                    null,
                    wxPayBean.getPrivateKeyPath(),
                    JSONUtil.toJsonStr(unifiedOrderModel)
            );
            log.info("统一下单响应 {}", response);
            // 根据证书序列号查询对应的证书来验证签名结果
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayBean.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            if (response.getStatus() == OK && verifySignature) {
                String body = response.getBody();
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String prepayId = jsonObject.getStr("prepay_id");
                Map<String, String> map = WxPayKit.jsApiCreateSign(wxPayBean.getAppId(), prepayId, wxPayBean.getPrivateKeyPath());
                log.info("唤起支付参数:{}", map);
                return JSONUtil.toJsonStr(map);
            }
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/jsApiPayNotify", method = RequestMethod.POST)
    @ResponseBody
    public void jsApiPayNotify(HttpServletRequest request, HttpServletResponse response) {
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
                    wxPayBean.getApiKey3(), wxPayBean.getPlatformCertPath());

            log.info("支付通知明文 {}", plainText);

            if (StrUtil.isNotEmpty(plainText)) {
                response.setStatus(200);
                map.put("code", "SUCCESS");
                map.put("message", "SUCCESS");
                log.info("支付SUCCESS {}", "SUCCESS");
            } else {
                response.setStatus(500);
                map.put("code", "ERROR");
                map.put("message", "签名错误");
                log.info("支付ERROR {}", map);
            }
            response.setHeader("Content-type", ContentType.JSON.toString());
            response.getOutputStream().write(JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8));
            response.flushBuffer();
        } catch (Exception e) {
            log.error("系统异常", e);
        }
    }
}
