package com.tgy.rtls.data.entity.pay;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.pay
*@Author: wuwei
*@CreateTime: 2023-11-13 14:52
*@Description: TODO
*@Version: 1.0
*/
@EqualsAndHashCode(callSuper = true)
@ApiModel(description="sms_quota")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsQuota extends BaseEntity{
    private Integer map;
    private Integer count;
    private String mapName;
    private Integer price;
}