package com.tgy.rtls.data.entity.vip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
*@author: wuwei
*@CreateTime: 2023/4/5 16:10
*/
@ApiModel(description = "VIP区域表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "vip_area")
public class VipArea implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 车牌号
     */
    @TableField(value = "license")
    @ApiModelProperty(value = "车牌号")
    private String license;

    /**
     * 地图
     */
    @TableField(value = "`map`")
    @ApiModelProperty(value = "地图")
    private Long map;
    private Long mapId;

    /**
     * 楼层
     */
    @TableField(value = "`floor`")
    @ApiModelProperty(value = "楼层")
    private Byte floor;

    /**
     * vip区域
     */
    @TableField(value = "vip_area")
    @ApiModelProperty(value = "vip区域")
    private String vipArea;

    /**
     * 道闸编号
     */
    @TableField(value = "barrier_gate_num")
    @ApiModelProperty(value = "道闸编号")
    private String barrierGateNum;

    /**
     * vip客户
     */
    @TableField(value = "vip_customers")
    @ApiModelProperty(value = "vip客户")
    private String vipCustomers;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 开始时间
     */
    @TableField(value = "start_time")
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField(value = "end_time")
    @ApiModelProperty(value = "结束时间")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 预约时段
     */
    @TableField(value = "appointment_slot")
    @ApiModelProperty(value = "预约时段")
    private String appointmentSlot;

    /**
     * 时长
     */
    @TableField(value = "duration")
    @ApiModelProperty(value = "时长")
    private String duration;

    /**
     * 状态
     */
    @TableField(value = "`state`")
    @ApiModelProperty(value = "状态")
    private Byte state;

    private String mapName;
    private List<VipArea> areaList;

    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    private String x;
    private String y;
    private String z;
    private String fid;

    private static final long serialVersionUID = 1L;
    private Integer barrierGateId;

    private String barrierGateArea;
    private String barrierGateAreaStr;
    private String text;
    private String floorName;
    private String vipType="area";


}