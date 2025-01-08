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
public class Mag implements Serializable {
    private Integer id;
    @ApiModelProperty("标签编号")
    private String num;//定位卡编号
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;   //创建时间

    private Float x_fix;
    private Float x;
    private Float y_fix;
    private Float y;
    private Float z_fix;
    private Float z;
    private Float x_diff;
    private Float occupy_x;
    private Float y_diff;
    private Float occupy_y;
    private Float z_diff;
    private Float occupy_z;
    private Float empty_x;
    private Float empty_y;
    private Float empty_z;
    private Short state;

}
