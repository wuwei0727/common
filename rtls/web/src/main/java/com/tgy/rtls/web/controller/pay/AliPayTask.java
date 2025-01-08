package com.tgy.rtls.web.controller.pay;

import com.tgy.rtls.data.entity.pay.OrderInfo;
import com.tgy.rtls.data.enums.PayType;
import com.tgy.rtls.data.service.pay.OrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-16 09:42
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Component
public class AliPayTask {
    @Resource
    private OrderInfoService orderInfoService;
     @Scheduled(cron = "0/30 * * * * ?")
     public void orderConfirm(){
         log.info("orderConfirm 被执行......");
         List<OrderInfo> aliOrderInfoList = orderInfoService.getNoPayOrderByDuration(12, PayType.ALIPAY.getType());
         for (OrderInfo orderInfo : aliOrderInfoList) {
             String orderNo = orderInfo.getOrderNo();
             Integer map = orderInfo.getMap();
             Integer productId = orderInfo.getProductId();
             log.warn("超时订单 ===> {}", orderNo);
             orderInfoService.checkOrderStatus(orderNo,map,productId);
         }
     }

    @Scheduled(cron = "0/30 * * * * ?")
    public void wxOrderConfirm() throws Exception {
        log.info("orderConfirm 被执行......");
        List<OrderInfo> orderInfoList = orderInfoService.getNoPayOrderByDuration(12, PayType.WXPAY.getType());
        for (OrderInfo orderInfo : orderInfoList) {
            String orderNo = orderInfo.getOrderNo();
            log.warn("超时订单 ===> {}", orderNo);
            Integer map = orderInfo.getMap();
            Integer productId = orderInfo.getProductId();
            orderInfoService.checkWxOrderStatus(orderNo,map,productId);
        }
    }

}
