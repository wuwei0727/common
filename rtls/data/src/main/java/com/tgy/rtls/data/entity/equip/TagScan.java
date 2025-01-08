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
 */
@ApiModel("搜寻定位卡")
@Data
@ToString
public class TagScan implements Serializable {
    private Integer id;
    @ApiModelProperty("标签编号")
    private String num;//定位卡编号
    @ApiModelProperty("类型")
    private Integer type;//卡类型
    @ApiModelProperty("定位频率")
    private int frequency;//定位频率
    @ApiModelProperty("定位功率")
    private int power;//定位功率
    @ApiModelProperty("电池电压")
    private double batteryVolt;//电池电压
    @ApiModelProperty("识别码1")
    private String code1;
    @ApiModelProperty("识别码2")
    private String code2;
    @ApiModelProperty("标签列表状态 0未有 1已有")
    private int status;

    //卡类型名
    private String typeName;

}
