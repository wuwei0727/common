package com.ww.springbootalipay.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sms_product")
public class Product extends BaseEntity{
    private String title; //商品名称
    private Integer count; //价格（分）
    private Double price; //价格（分）
    private Integer purchaseDuration; //价格（分）
}
