package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/13
 * 标签
 */
@Data
@ToString
@ApiModel("标签电压")
public class TagVolt implements Serializable {
    private Integer id;
    @ApiModelProperty("标签编号")
    private String num;//定位卡编号
    @ApiModelProperty("adc")
    private Integer adc;//电压
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;   //创建时间

    private Integer rssi;

}
