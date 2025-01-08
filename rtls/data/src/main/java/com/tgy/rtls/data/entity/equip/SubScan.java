package com.tgy.rtls.data.entity.equip;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2021/1/19
 * 分站搜寻
 */
@Data
@ToString
@ApiModel("分站搜寻")
public class SubScan implements Serializable {
    private Integer id;
    @ApiModelProperty("分站编号")
    private String num;
    @ApiModelProperty("ip地址")
    private String ipAddress;
    @ApiModelProperty("供电状态 0主电供电  1备用电池供电")
    private int powerstate;
    @ApiModelProperty("电压")
    private Double batteryVolt;
    @ApiModelProperty("分站列表状态 0未有  1已有")
    private int status;
    @ApiModelProperty("识别码1")
    private String code1;
    @ApiModelProperty("识别码2")
    private String code2;

    private String powerName;//供电状态名
}
