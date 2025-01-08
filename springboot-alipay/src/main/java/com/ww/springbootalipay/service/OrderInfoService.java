package com.ww.springbootalipay.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ww.springbootalipay.dao.OrderInfo;
import com.ww.springbootalipay.enums.OrderStatus;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-15 15:18
 * @Description: TODO
 * @Version: 1.0
 */
public interface OrderInfoService extends IService<OrderInfo> {
    OrderInfo createOrderByProductId(Integer productId, int map, String paymentType);

    OrderInfo getOrderByOrderNo(String outTradeNo);

    void processOrder(Map<String, String> params, int productId, Integer map);

    String getOrderStatus(String orderNo);

    int updateStatusByOrderNo(String orderNo,String buyerLoginId, String content, OrderStatus orderStatus);

    void checkOrderStatus(String orderNo, Integer map, Integer productId);

    List<OrderInfo> getNoPayOrderByDuration(int minutes, String paymentType);
    String tradeQuery(String outTradeNo,String tradeNo);

}