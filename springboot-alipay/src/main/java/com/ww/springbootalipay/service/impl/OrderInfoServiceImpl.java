package com.ww.springbootalipay.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.ijpay.alipay.AliPayApi;
import com.ww.springbootalipay.dao.OrderInfo;
import com.ww.springbootalipay.dao.Product;
import com.ww.springbootalipay.enums.OrderStatus;
import com.ww.springbootalipay.enums.AliPayTradeState;
import com.ww.springbootalipay.mapper.OrderInfoMapper;
import com.ww.springbootalipay.mapper.ProductMapper;
import com.ww.springbootalipay.service.OrderInfoService;
import com.ww.springbootalipay.util.OrderNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
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
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {
    @Resource
    private ProductMapper productMapper;
    @Resource
    private ServiceImpl smsQuotaService;
    private final ReentrantLock lock = new ReentrantLock();
    private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public OrderInfo createOrderByProductId(Integer productId, int map,String paymentType) {
        //查找已存在但未支付的订单
        OrderInfo orderInfo = this.getNoPayOrderByProductId(productId, paymentType);
        if( orderInfo != null){
            return orderInfo;
        }
        //获取商品信息
        Product product = productMapper.selectById(productId);
        //生成订单
        orderInfo = new OrderInfo();
        orderInfo.setTitle("国内短信套餐包");
        orderInfo.setMap(map);
        orderInfo.setOrderNo(OrderNoUtils.getOrderNo()); //订单号
        orderInfo.setProductId(productId);
        orderInfo.setTotalFee(product.getPrice()); //分
        orderInfo.setOrderStatus(OrderStatus.NOTPAY.getType()); //未支付
        orderInfo.setPaymentType(paymentType);
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
    public int updateStatusByOrderNo(String orderNo, String buyerLoginId, String content, OrderStatus orderStatus) {
        log.info("更新订单状态 ===> {}", orderStatus.getType());
        QueryWrapper<OrderInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderStatus(orderStatus.getType());
        orderInfo.setBuyerLogonId(buyerLoginId);
        orderInfo.setContent(content);
        return baseMapper.update(orderInfo, queryWrapper);
    }

    @Override
    public void checkOrderStatus(String orderNo, Integer map, Integer productId) {
        log.info("定时任务执行支付宝查单");
        String result = this.tradeQuery(orderNo,null);
        Gson gson = new Gson();
        HashMap<String, LinkedTreeMap<String,Object>> resultMap;
        LinkedTreeMap<String,Object> alipayTradeQueryResponse;
        //更新本地订单状态
        if(result == null){
            //更新本地订单状态
            this.updateStatusByOrderNo(orderNo,null,null, OrderStatus.CLOSED);
        }
        //解析查单响应结果
         resultMap = gson.fromJson(result, HashMap.class);
         alipayTradeQueryResponse = resultMap.get("alipay_trade_query_response");

        String tradeStatus = (String)alipayTradeQueryResponse.get("trade_status");
        String tradePatTime = (String)alipayTradeQueryResponse.get("send_pay_date");
        log.info(tradePatTime);
        log.info("checkOrderStatus");
        String buyerLogonId = (String) alipayTradeQueryResponse.get("buyer_logon_id");

        if(AliPayTradeState.NOTPAY.getType().equals(tradeStatus)){
            log.warn("核实订单未支付 ===> {}", orderNo);
            //如果订单未支付，则调用关单接口关闭订单
            this.closeOrder(orderNo);
            // 并更新商户端订单状态
            this.updateStatusByOrderNo(orderNo,null,null,OrderStatus.CLOSED);
        }

        if(AliPayTradeState.SUCCESS.getType().equals(tradeStatus)){
            log.warn("核实订单已支付 ===> {}", orderNo);
            //如果订单已支付，则更新商户端订单状态
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
             Integer count = smsQuotaService.getSmsQuotaByMap(map, null).getCount();
             Product product = productMapper.selectById(productId);
             Integer[] countArray = {count, product.getCount()};
             int sum = Stream.of(countArray).reduce(0, Integer::sum);
             SmsQuota quota = new SmsQuota();
             quota.setMap(map);
             quota.setCount(sum);
             Date date = format.parse(tradePatTime);
             System.out.println("date = " + date);
             quota.setCreateTime(date);
             System.out.println("quota.getCreateTime(date).toString() = " +quota.getCreateTime());
             smsQuotaService.updateByPrimaryKeySelective(quota);
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
           if (orderNo!=null){
               model.setOutTradeNo(orderNo);
           }
            AlipayTradeQueryResponse response = AliPayApi.tradeQueryToResponse(model);
            if(response.isSuccess()){
                log.info("调用成功，返回结果 ===> " + response.getBody());
                Date sendPayDate = response.getSendPayDate();
                return response.getBody();
            } else {
                log.info("调用失败，返回码 ===> " + response.getCode() + ", 返回描述 ===> " + response.getMsg());
                //throw new RuntimeException("查单接口的调用失败");
                return null;//订单不存在
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new RuntimeException("查单接口的调用失败");
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
                //处理重复通知
                //接口调用的幂等性：无论接口被调用多少次，以下业务执行一次
                String orderStatus = this.getOrderStatus(orderNo);
                if (!OrderStatus.NOTPAY.getType().equals(orderStatus)) {
                    return;
                }
                //更新订单状态
                updateOrderInfoAndSmsQuota(orderNo, content,map,productId,tradePatTime, buyerLogonId);
            }finally {
                //要主动释放锁
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
            if (orderNo!=null) {
                model.setOutTradeNo(orderNo);
            }
            AliPayApi.tradeCloseToResponse(model);
        } catch (AlipayApiException e) {
            throw new RuntimeException(e);
        }
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        Integer[] a1 = {10000, 12};
        int sum = Stream.of(a1).reduce(0, Integer::sum);
        System.out.println("sum = " + sum);

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(date);
        System.out.println("date.getTime() = " + date.getTime()/1000);
    }

}
