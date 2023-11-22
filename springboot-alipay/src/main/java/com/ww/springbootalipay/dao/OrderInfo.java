package com.ww.springbootalipay.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-15 15:13
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sms_order_info")
public class OrderInfo extends BaseEntity{
    private String title;//订单标题
    private String orderNo;//商户订单编号

    private Integer map;//用户id

    private Integer productId;//支付产品id

    private Double totalFee;//订单金额(分)

    private String codeUrl;//订单二维码连接

    private String orderStatus;//订单状态

    private String paymentType;//支付方式
    private String content;
    private String buyerLogonId;
}
