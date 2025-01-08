package com.tgy.rtls.data.service.pay;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.ijpay.alipay.AliPayApi;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.AuthTypeEnum;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.OrderNoUtils;
import com.tgy.rtls.data.entity.pay.OrderInfo;
import com.tgy.rtls.data.entity.pay.Product;
import com.tgy.rtls.data.entity.pay.SmsQuota;
import com.tgy.rtls.data.entity.pay.WxPayBean;
import com.tgy.rtls.data.enums.AliPayTradeState;
import com.tgy.rtls.data.enums.OrderStatus;
import com.tgy.rtls.data.enums.PayType;
import com.tgy.rtls.data.enums.WxTradeState;
import com.tgy.rtls.data.mapper.pay.OrderInfoMapper;
import com.tgy.rtls.data.mapper.pay.ProductMapper;
import com.tgy.rtls.data.service.park.SmsQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-15 15:12
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "module.web.enabled", havingValue = "true", matchIfMissing = true)
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    private final static int OK = 200;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private SmsQuotaService smsQuotaService;
    private final ReentrantLock lock = new ReentrantLock();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Resource
    private WxPayBean wxPayBean;

    @Override
    public OrderInfo createOrderByProductId(Integer productId, int map,String paymentType) {
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId, paymentType);
        if( orderInfo != null){
            return orderInfo;
        }
        Product product = productMapper.selectById(productId);
        orderInfo = new OrderInfo();
        orderInfo.setTitle(product.getTitle());
        orderInfo.setMap(map);
        if(paymentType.equals(PayType.WXPAY.getType())){
            orderInfo.setOrderNo(PayKit.generateStr());
        }else {
            orderInfo.setOrderNo(OrderNoUtils.getOrderNo());
        }
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice());
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType());
        orderInfo.setPaymentType(paymentType);
        orderInfo.setCreateTime(new Date());
        baseMapper.insert(orderInfo);
        return orderInfo;
    }


    @Override
    public String getOrderStatus(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = baseMapper.selectOne(queryWrapper);
        if(orderInfo == null){
            return null;
        }
        return orderInfo.getOrderStatus();
    }

    @Override
    public int updateStatusByOrderNo(String orderNo,String buyerLoginId,String content,OrderStatus orderStatus) {
        log.info("更新订单状态 ===> {}", orderStatus.getType());
        LocalDateTime expiryDate = LocalDateTime.now().plus(24, ChronoUnit.MONTHS);
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        orderInfo.setBuyerLogonId(buyerLoginId);
        orderInfo.setContent(content);
        orderInfo.setExpiryDate(expiryDate);
        return baseMapper.update(orderInfo, queryWrapper);
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        LocalDateTime expiryDate = LocalDateTime.now().plus(24, ChronoUnit.MONTHS);
        System.out.println("expiryDate = " + expiryDate);
    }

    @Override
    public void checkOrderStatus(String orderNo, Integer map, Integer productId) {
        log.info("定时任务执行支付宝查单");
        if(orderNo==null){
            return;
        }
        String result = this.tradeQuery(orderNo,null);
        Gson gson = new Gson();
        HashMap<String, LinkedTreeMap<String,Object>> resultMap;
        LinkedTreeMap<String,Object> alipayTradeQueryResponse;

         resultMap = gson.fromJson(result, HashMap.class);
         alipayTradeQueryResponse = resultMap.get("alipay_trade_query_response");

        String tradeStatus = (String)alipayTradeQueryResponse.get("trade_status");
        String tradePatTime = (String)alipayTradeQueryResponse.get("send_pay_date");
        String TRADE_NOT_EXIST = (String)alipayTradeQueryResponse.get("sub_code");
        if(result == null || AliPayTradeState.CLOSED.getType().equals(tradeStatus)||AliPayTradeState.TRADE_NOT_EXIST.getType().equals(TRADE_NOT_EXIST)){
            this.updateStatusByOrderNo(orderNo,null,null, OrderStatus.CLOSED);
        }
        log.info(tradePatTime);
        log.info("checkOrderStatus");
        String buyerLogonId = (String) alipayTradeQueryResponse.get("buyer_logon_id");

        if(AliPayTradeState.NOTPAY.getType().equals(tradeStatus)){
            log.warn("核实订单未支付 ===> {}", orderNo);
            this.closeOrder(orderNo);
            this.updateStatusByOrderNo(orderNo,null,null,OrderStatus.CLOSED);
        }

        if(AliPayTradeState.SUCCESS.getType().equals(tradeStatus)){
            log.warn("核实订单已支付 ===> {}", orderNo);
            this.updateOrderInfoAndSmsQuota(orderNo, result,map,productId,tradePatTime,buyerLogonId);
        }
    }
     private void updateOrderInfoAndSmsQuota(String orderNo, String result, Integer map, Integer productId, String tradePatTime, String buyerLogonId){
         if (this.updateStatusByOrderNo(orderNo,buyerLogonId,result,OrderStatus.SUCCESS)>0){
             addSmsQuota(map,productId,tradePatTime);
         }
     }

     private void addSmsQuota(Integer map, Integer productId, String tradePatTime){
         try {
             SmsQuota smsQuota = smsQuotaService.getSmsQuotaByMap(map, null);
             Product product = productMapper.selectById(productId);
             Integer[] countArray = {smsQuota.getCount(), product.getCount()};
             int sum = Stream.of(countArray).reduce(0, Integer::sum);
             Date date = format.parse(tradePatTime);
             System.out.println("date = " + date);
             smsQuota.setCount(sum);
             smsQuota.setCreateTime(date);
             log.info("quota.getCreateTime(date).toString() = " +smsQuota.getCreateTime());
             smsQuotaService.updateByPrimaryKeySelective(smsQuota);
         } catch (ParseException e) {
             throw new RuntimeException(e);
         }
     };
    @Override
    public List<OrderInfo> getNoPayOrderByDuration(int minutes, String paymentType) {
        Instant instant = Instant.now().minus(Duration.ofMinutes(minutes));

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        queryWrapper.le("create_time", instant);
        queryWrapper.eq("payment_type", paymentType);

        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public String tradeQuery(String orderNo,String tradeNo) {
        try {
            AlipayTradeQueryModel model = new AlipayTradeQueryModel();
           if (!NullUtils.isEmpty(orderNo)){
               model.setOutTradeNo(orderNo);
               return AliPayApi.tradeQueryToResponse(model).getBody();
           }
           return null;
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("查单接口的调用失败");
        }
    }

    @Override
    public void processOrder(HashMap<String, Object> plainTextMap, Integer productId, Integer map) throws ParseException {
        String content =(String) plainTextMap.get("content");
        String orderNo = (String) plainTextMap.get("out_trade_no");
        String buyerLoginId =(String) new JSONObject(plainTextMap).getJSONObject("payer").get("openid");
        String successTime = (String) plainTextMap.get("success_time");
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Date date = inputDateFormat.parse(successTime);
        String tradePatTime = format.format(date);

        if(lock.tryLock()){
            try {
                String orderStatus = this.getOrderStatus(orderNo);
                if(!OrderStatus.NOTPAY.getType().equals(orderStatus)){
                    return;
                }
                updateOrderInfoAndSmsQuota(orderNo, content,map,productId,tradePatTime, buyerLoginId);
                this.updateStatusByOrderNo(orderNo,buyerLoginId,content,OrderStatus.SUCCESS);
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public void checkWxOrderStatus(String orderNo, Integer map, Integer productId) throws Exception {
        log.warn("根据订单号核实订单状态 ===> {}", orderNo);
        String result = this.queryOrder(orderNo);
        Gson gson = new Gson();
        Map<String, String> resultMap = gson.fromJson(result, HashMap.class);

        String tradeState = resultMap.get("trade_state");

        JsonObject resultJson = gson.fromJson(result, JsonObject.class);

        if (resultJson.has("status") && resultJson.get("status").getAsInt() == 404) {
            JsonObject bodyJson = gson.fromJson(resultJson.get("body").getAsString(), JsonObject.class);
            if ("ORDER_NOT_EXIST".equals(bodyJson.get("code").getAsString())) {
                log.warn("核实订单不存在 ===> {}", orderNo);
                this.closeWxOrder(orderNo);
                this.updateStatusByOrderNo(orderNo, null, null, OrderStatus.CLOSED);
                return;
            }
        }

        if(WxTradeState.SUCCESS.getType().equals(tradeState)){
            log.warn("核实订单已支付 ===> {}", orderNo);
            String buyerLoginId =(String) new JSONObject(resultMap).getJSONObject("payer").get("openid");
            String successTime = resultMap.get("success_time");
            SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            Date date = inputDateFormat.parse(successTime);
            String tradePatTime = format.format(date);
            this.updateOrderInfoAndSmsQuota(orderNo, result,map,productId,tradePatTime,buyerLoginId);
            this.updateStatusByOrderNo(orderNo,buyerLoginId,result, OrderStatus.SUCCESS);
        }

        if(WxTradeState.NOTPAY.getType().equals(tradeState)){
            log.warn("核实订单未支付 ===> {}", orderNo);
            this.closeWxOrder(orderNo);
            this.updateStatusByOrderNo(orderNo,null,null, OrderStatus.CLOSED);
        }
        if(WxTradeState.CLOSED.getType().equals(tradeState)){
            log.warn("核实订单未支付 ===> {}", orderNo);
            this.closeWxOrder(orderNo);
            this.updateStatusByOrderNo(orderNo,null,null, OrderStatus.CLOSED);
        }

    }

    @Override
    public void saveCodeUrl(String orderNo, String codeUrl) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCodeUrl(codeUrl);
        baseMapper.update(orderInfo, queryWrapper);
    }

    public String closeWxOrder(String orderNo) throws Exception {
        String url = String.format(BasePayApiEnum.CLOSE_ORDER_BY_OUT_TRADE_NO.getUrl(), orderNo);
        log.info("关闭订单url:{}", url);
        Map<String, String> paramsMap = new HashMap<>();
        paramsMap.put("mchid", wxPayBean.getMchId());

        IJPayHttpResponse response = WxPayApi.v3(
                RequestMethodEnum.POST,
                WxDomainEnum.CHINA.toString(),
                url,
                wxPayBean.getMchId(),
                OrderNoUtils.SerialNumberUtils.getSerialNumber(wxPayBean.getPrivateCertPath(), wxPayBean.getMchId()),
                null,
                wxPayBean.getPrivateKeyPath(),
                JSONUtil.toJsonStr(paramsMap)
        );
        if (response.getStatus() == 204) {
            boolean verifySignature = WxPayKit.verifySignature(response, wxPayBean.getPlatformCertPath());
            log.info("verifySignature: {}", verifySignature);
            if (verifySignature) {
                return response.getBody();
            }
        }
        return null;

    }
    private String queryOrder(String orderNo) {
        try {
            Map<String, String> params = new HashMap<>(16);
            params.put("mchid", wxPayBean.getMchId());

            log.info("统一下单参数 {}", JSONUtil.toJsonStr(params));
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.GET,
                    WxDomainEnum.CHINA.toString(),
                    String.format(BasePayApiEnum.ORDER_QUERY_BY_OUT_TRADE_NO.toString(), orderNo),
                    wxPayBean.getMchId(),
                    OrderNoUtils.SerialNumberUtils.getSerialNumber(wxPayBean.getPrivateCertPath(), wxPayBean.getMchId()),
                    null,
                    wxPayBean.getPrivateKeyPath(),
                    params,
                    AuthTypeEnum.RSA.getCode()
            );
            log.info("查询响应 {}", response);
            if (response.getStatus() == OK) {
                boolean verifySignature = WxPayKit.verifySignature(response, wxPayBean.getPlatformCertPath());
                log.info("verifySignature: {}", verifySignature);
                return response.getBody();
            }
            return JSONUtil.toJsonStr(response);
        } catch (Exception e) {
            log.error("系统异常", e);
            return e.getMessage();
        }
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void processOrder(Map<String, String> params, int productId, Integer map){
        log.info("处理订单");
        //获取订单号
        String orderNo = params.get("out_trade_no");
        String tradePatTime = params.get("gmt_payment");
        String buyerLogonId = params.get("buyer_logon_id");

        String content = JSON.toJSONString(params);
        if(lock.tryLock()) {
            try {
                String orderStatus = this.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                updateOrderInfoAndSmsQuota(orderNo, content,map,productId,tradePatTime, buyerLogonId);
            }finally {
                lock.unlock();
            }
        }
    }

    @Override
    public OrderInfo getOrderByOrderNo(String orderNo) {
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        return baseMapper.selectOne(queryWrapper);
    }

    private OrderInfo getNoPayOrderByProductId(Integer productId, String paymentType) {

        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productId);
        queryWrapper.eq("order_status", OrderStatus.NOTPAY.getType());
        queryWrapper.eq("payment_type", paymentType);
//        queryWrapper.eq("user_id", userId);
        return baseMapper.selectOne(queryWrapper);
    }

    private void closeOrder(String orderNo) {
        try {
            AlipayTradeCloseModel model = new AlipayTradeCloseModel();
            if (!NullUtils.isEmpty(orderNo)) {
                model.setOutTradeNo(orderNo);
            }
            AliPayApi.tradeCloseToResponse(model);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }
}
