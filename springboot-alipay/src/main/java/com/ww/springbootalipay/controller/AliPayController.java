package com.ww.springbootalipay.controller;

import com.alipay.api.*;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.ijpay.alipay.AliPayApi;
import com.ww.springbootalipay.dao.AliPayBean;
import com.ww.springbootalipay.dao.OrderInfo;
import com.ww.springbootalipay.enums.PayType;
import com.ww.springbootalipay.service.OrderInfoService;
import com.ww.springbootalipay.vo.R;
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

    /**
     * PC支付
     * 你使用工具生成的是应用公私钥，
     * 然后公钥上传到后台获取支付宝公钥，
     * 代码中需要使用应用私钥和支付宝公钥
     */
    @RequestMapping(value = "/pcPay")
    @ResponseBody
    public void pcPay(HttpServletResponse response,Integer productId,int map) {
        try {
            OrderInfo orderInfo = orderInfoService.createOrderByProductId(productId, map, PayType.ALIPAY.getType());
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
        return R.ok().setMessage("成功").data("a",orderInfoService.getOrderStatus(orderNo));
    }


    @RequestMapping( "/del")
    public boolean del(int id){
        return orderInfoService.removeById(id);
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
                System.out.println("notify_url 验证失败");
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


    /**
     * 交易查询
     */
    @RequestMapping(value = "/tradeQuery")
    @ResponseBody
    public String tradeQuery(@RequestParam(required = false, name = "outTradeNo") String outTradeNo,
                             @RequestParam(required = false, name = "tradeNo") String tradeNo) {
        return orderInfoService.tradeQuery(outTradeNo,tradeNo);
    }


    public static void main(String[] args) throws AlipayApiException {
         String privateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCJMok2mAfDZ+G+o/4Al9c8/mGPvVNcQmGT1WxJQ06zU1PX9/CQmIY92owAFJ3hwSni22iZnhgDjRh21aLuDhLgyb3J0hZhNAsw5I75waEqSaWM6RV7tgS26ih5gv0clgaeEJqHFLQTAoxceJgfiOYRlJjgKU3lWMI1q0AIxuxN7ML/mknXWEjPrrnRJ4g114ZAR0UNUa2y+qihyAaOC7uUpHF1anXKJ/IWYhiTkhY/7GZJKnLPT6uNOjXDcTgv7F5T4nnz+rktLRlyLm9Rh+hbQAPs89lDc/350rSnLCiGVq8aQVuoXspAzvP2YIiUWEKbvz7mkxySKKnvQKy7RrJvAgMBAAECggEAHJ+fgvVbAUimD2kVcDg9nqsP3bZJMwFibD5lti9FtyIWZwOCfBhmQXdcKBQlEFmFjJ/sBci3B96jEP/nWzUyeRV99CJRqvv5rbyxm6frIPB2ipyCb+n8t9DImvlaJ2Ghhc6+/pmyLBknAY/jZQjhvx86eeIEL2pCMoJE/M/Ls0H5AgnyTqYic5G9oZiiQRV0Mq8YKS3geigWQwEiOkpIta1ZMjHrdThKVKCYqvDImbmROYvxXR+4q+DSuNnLjhOdr3m8I+aKr5kBziXOHZMVCMIYwkAWGriIeszgmkaGEQzTNo2yo++BogGDZ05hguWJgsSlLU+Fmoc+C1QRaqq/IQKBgQC/Ci4j8i7DPjh6+Gll8dtk4n/EipGtAMVbhkrfBmj7cM3JxiG7zBCyNqooVQL4Ghi4/eIFyj2FoSK/ivw61q4OAOrEkV4hmUVn3cBgRSzL70mCdL5gf9hqG5Lylnxd8LDd2hBcPULDfgnvXyfpB76LGCkk2XOjiHNwlNAI9L70XwKBgQC32W6wGPEbfTMxzyIBWxNqoICxxBLu9vxueFkDvf9ywijLmqFi9xyhpajl+V53F2NlVt6MffH3kk4yWwwm3CbSIRAX1nUAKXhqR5REftMm1bB9myrsjQlwyy6s1JVZF/F9rso9+pmcH6ISthStrg6/NBXeZofy7IWGmASYCod78QKBgG+Fj93oGShdADe5ID4k7EB+gLvJcLUfyBZf1QFRCqQN+/QYN8UboXuHsdmNlwgio+AKRZG/uB50LIbIqennPQBrxKEvg1X11bCGoTER8M60R6NiZ2iJ+Von8qau9rcYNQetrvNJx9m7kC0uzWuZ27/zQMGEN591nn5QQQmZZofdAoGAFHTA+B8OVLytgZFGe1gi5O5O43ipkqUwmSoS3XO6hI5N87mU02ojaWkBTfVdKWsUyCXORVuOHLnOsZOn2nsMZrz/CWf3RLlYiJrGSIjD/cIxDQjC0ecvcblpzBkrNOqRGCbmodbMWK68XdCcpGaFhYjKFiDaZ5EhmkQ33qLtSEECgYEApeVfrEaXVG+XNOD9CPJO5Z8JQMXiLau3DtaSqAxxcdG5vsGPP/WI0s3bE7xYYQEijuNzeoslyB7heyZdnqGI1LezszI0cQb3v37EPP4APO9wQRql3cU3K0nbUdW/C1k8NC7FKkMagHz9JCnKZQdNFRb77apxzksmr7lNqJXTxf4=";
         String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1c1GM8qU43YYIUszkhnFtaB0toLZGVmELVx26HYvGL7EaIWnQbryd0idKz9Q2f6cF+OTgyHwT0r8K9mNCmmU4+kwMDaevKKlJsMdFXQYVqQf3UmbBWxTb3X9zYcMIAwyrvNch+M8usxH4zMwFquJ97XzHrXloXIGQom7FB6RfvrBL4jozOgqZcvLgKXdy1hUyDUlx8LjgdoVyS/ZCrod28HrMifwURlWyCjwxHSQE2B/y8dTw+ga1EX9IIy/bxxiptFhb+rrP9lJAXxn0eh38x6Xz6Gp/i4oFLjGv6I3hGUVXP9/utIVFaiIJ/RCvRniNzUzGcRJdUN0j7w33BCaRwIDAQAB";
         AlipayConfig alipayConfig = new AlipayConfig();
         alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
         alipayConfig.setAppId("2021004122685881");
         alipayConfig.setPrivateKey(privateKey);
         alipayConfig.setFormat("json");
         alipayConfig.setAlipayPublicKey(alipayPublicKey);
         alipayConfig.setCharset("UTF-8");
         alipayConfig.setSignType("RSA2");
         AlipayClient alipayClient = new DefaultAlipayClient(alipayConfig);

        // AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        // AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        // model.setOutTradeNo("20150320010101012");
        // model.setTotalAmount("0.1");
        // model.setSubject("Iphone6 16G");
        // model.setProductCode("FAST_INSTANT_TRADE_PAY");
        // request.setBizModel(model);
        // AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        // System.out.println(response.getBody());
        // if (response.isSuccess()) {
        //     System.out.println("调用成功");
        // } else {
        //     System.out.println("调用失败");
        //     // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
        //     // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
        //     // System.out.println(diagnosisUrl);
        // }



        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
//        model.setTradeNo("2023111622001428211433325843");
        model.setOutTradeNo("20150320010101012");
        request.setBizModel(model);
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        System.out.println(response.getBody());
        if (response.isSuccess()) {
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
            // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
            // String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
            // System.out.println(diagnosisUrl);
        }


//         AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
//         AlipayTradeCloseModel model = new AlipayTradeCloseModel();
//         model.setOutTradeNo("111410511216999");
//         // model.setTradeNo("2023111322001466761439879768");
//         request.setBizModel(model);
//         AlipayTradeCloseResponse response = alipayClient.execute(request);
//         System.out.println(response.getBody());
//         if (response.isSuccess()) {
//             System.out.println("调用成功");
//         } else {
//             System.out.println("调用失败");
//             // sdk版本是"4.38.0.ALL"及以上,可以参考下面的示例获取诊断链接
//             String diagnosisUrl = DiagnosisUtils.getDiagnosisUrl(response);
//             System.out.println(diagnosisUrl);
//         }
    }


    /**
     * 退款
     */
    @RequestMapping(value = "/tradeRefund")
    @ResponseBody
    public String tradeRefund(@RequestParam(required = false, name = "outTradeNo") String outTradeNo, @RequestParam(required = false, name = "tradeNo") String tradeNo) {

        try {
            AlipayTradeRefundModel model = new AlipayTradeRefundModel();
            if (outTradeNo!=null) {
                model.setOutTradeNo(outTradeNo);
            }
            if (tradeNo!=null) {
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
