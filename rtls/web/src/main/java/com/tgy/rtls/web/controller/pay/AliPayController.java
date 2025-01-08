package com.tgy.rtls.web.controller.pay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayConstants;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.ijpay.alipay.AliPayApi;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.pay.AliPayBean;
import com.tgy.rtls.data.entity.pay.OrderInfo;
import com.tgy.rtls.data.enums.OrderStatus;
import com.tgy.rtls.data.enums.PayType;
import com.tgy.rtls.data.service.pay.OrderInfoService;
import com.tgy.rtls.data.entity.pay.vo.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-10 16:18
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/aliPay")
public class AliPayController {
    @Resource
    private AliPayBean aliPayBean;
    @Resource
    private OrderInfoService orderInfoService;
    private static final Logger log = LoggerFactory.getLogger(AliPayController.class);


    @RequestMapping(value = "/pcPay")
    @ResponseBody
    public void pcPay(HttpServletResponse response,Integer productId,int map) {
        try {
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, map,PayType.ALIPAY.getType());
            LocalDateTime absoluteTime = LocalDateTime.now().plusMinutes(12);
            String timeExpire = absoluteTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            AlipayTradePagePayModel model = new AlipayTradePagePayModel();
            model.setOutTradeNo(orderInfo.getOrderNo());
            model.setProductCode("FAST_INSTANT_TRADE_PAY");
            model.setTotalAmount(String.valueOf(orderInfo.getTotalFee()));
            model.setSubject(orderInfo.getTitle());
            model.setTimeExpire(timeExpire);
            model.setQrPayMode("4");
            model.setQrcodeWidth(200L);
            log.error("异步通知地址：");log.error(aliPayBean.getDomain());
            AliPayApi.tradePage(response,  model, aliPayBean.getDomain(), null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping( value = "/getOrderStatus")
    public R getOrderStatus(String orderNo){
        String orderStatus = orderInfoService.getOrderStatus(orderNo);
        if(OrderStatus.SUCCESS.getType().equals(orderStatus)){
            return R.ok().setCode(200).setMessage("支付成功"); //支付成功
        }
        return R.ok().setMessage("支付中......");
    }


    @RequestMapping(value = "/notifyUrl")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request) {
            String result = "failure";
        try {
            // 获取支付宝POST过来反馈信息
            Map<String, String> params = AliPayApi.toMap(request);
            for (Map.Entry<String, String> entry : params.entrySet()) {

                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            boolean verifyResult = AlipaySignature.rsaCheckV1(params, aliPayBean.getPublicKey(), AlipayConstants.CHARSET_UTF8, AlipayConstants.SIGN_TYPE_RSA2);

            if (!verifyResult) {
                // System.out.println("notify_url 验证失败");
                return result;
            }
            String outTradeNo = params.get("out_trade_no");
            OrderInfo order = orderInfoService.getOrderByOrderNo(outTradeNo);
            if(order == null){
                log.error("订单不存在");
                return result;
            }

            String totalAmount = params.get("total_amount");
            String totalFeeInt = order.getTotalFee().toString();
            if(!totalAmount.equals(totalFeeInt)){
                log.error("金额校验失败");
                return result;
            }
            String sellerId = params.get("seller_id");
            String sellerIdProperty = aliPayBean.getSellerId();
            if(!sellerId.equals(sellerIdProperty)){
                log.error("商家pid校验失败");
                return result;
            }

            String appId = params.get("app_id");
            String appIdProperty = aliPayBean.getAppId();
            if(!appId.equals(appIdProperty)){
                log.error("appid校验失败");
                return result;
            }

            String tradeStatus = params.get("trade_status");
            if(!"TRADE_SUCCESS".equals(tradeStatus)){
                return result;
            }

            orderInfoService.processOrder(params,order.getProductId(),order.getMap());

            result = "success";
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return "failure";
        }
            return result;
    }

    @RequestMapping(value = "/ttttt")
    @ResponseBody
    public int tttt(@RequestParam(required = false, name = "outTradeNo") String outTradeNo,
                             @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        return orderInfoService.updateStatusByOrderNo(outTradeNo,"1","10",OrderStatus.SUCCESS);
    }

    @RequestMapping(value = "/tradeQuery")
    @ResponseBody
    public String tradeQuery(@RequestParam(required = false, name = "outTradeNo") String outTradeNo,
                             @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        return orderInfoService.tradeQuery(outTradeNo,tradeNo);
    }

    @RequestMapping(value = "/tradeRefund")
    @ResponseBody
    public String tradeRefund(@RequestParam(required = false, name = "outTradeNo") String outTradeNo, @RequestParam(required = false, name = "tradeNo") String tradeNo) {

        try {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            if (!NullUtils.isEmpty(outTradeNo)) {
                model.setOutTradeNo(outTradeNo);
            }
            if (!NullUtils.isEmpty(tradeNo)) {
                model.setTradeNo(tradeNo);
            }
            model.setRefundAmount("0.01");
            model.setRefundReason("正常退款");
            return AliPayApi.tradeRefundToResponse(model).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }
}
