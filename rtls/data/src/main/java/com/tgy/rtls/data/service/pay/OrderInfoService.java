package com.tgy.rtls.data.service.pay;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.pay.OrderInfo;
import com.tgy.rtls.data.enums.OrderStatus;

import java.text.ParseException;
import java.util.HashMap;
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

    void processOrder(HashMap<String, Object> plainTextMap, Integer productId, Integer map) throws ParseException;

    void checkWxOrderStatus(String orderNo, Integer map, Integer productId) throws Exception;

    void saveCodeUrl(String orderNo, String codeUrl);
}