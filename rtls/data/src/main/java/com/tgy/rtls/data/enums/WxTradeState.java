package com.tgy.rtls.data.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.enums
 * @Author: wuwei
 * @CreateTime: 2023-11-27 14:30
 * @Description: TODO
 * @Version: 1.0
 */
@AllArgsConstructor
@Getter
public enum WxTradeState {

    /**
     * 支付成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 未支付
     */
    NOTPAY("NOTPAY"),

    /**
     * 已关闭
     */
    CLOSED("CLOSED"),

    /**
     * 转入退款
     */
    REFUND("REFUND");

    /**
     * 类型
     */
    private final String type;
}
