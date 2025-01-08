package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.user.Person;
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
@ApiModel("标签")
public class Tag implements Serializable {
    private Integer id;
    @ApiModelProperty("标签编号")
    private String num;//定位卡编号
    @ApiModelProperty("类型")
    private Integer type;//卡类型
    @ApiModelProperty("定位频率")
    private int frequency=1;//定位频率 默认1
    @ApiModelProperty("定位功率")
    private int power=33;//定位功率 默认33
    @ApiModelProperty("状态 0离线 1在线")
    private int status;//状态 0离线 1在线
    private Integer instanceid;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;   //创建时间
    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private float fix;
    private String batteryVolt;//电池电压
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date batteryTime;//电压检测时间
    //卡类型名
    private String typeName;

    private Person person;
    /*
     * 开发者模式的字段
     * */
    private String appVersion;//应用程序版本
    private String hardwareVersion;//硬件版本
    private int updatestate;//升级进度 -1失败
}
