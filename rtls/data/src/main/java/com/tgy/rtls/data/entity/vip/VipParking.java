package com.tgy.rtls.data.entity.vip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * vip车位管理
 */
@ApiModel(description = "vip车位管理")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "vip_parking")
public class VipParking extends BaseEntitys implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long vipPlaceId;
    private Integer place;

    /**
     * 地图Id
     */
    @TableField(value = "`map`")
    @ApiModelProperty(value = "地图Id")
    private Long map;

    /**
     * 车位名称
     */
    @TableField(value = "`name`")
    @ApiModelProperty(value = "车位名称")
    private String name;

    /**
     * 楼层
     */
    @TableField(value = "`floor`")
    @ApiModelProperty(value = "楼层")
    private Integer floor;

    /**
     * 车牌号
     */
    @TableField(value = "license")
    @ApiModelProperty(value = "车牌号")
    private String license;

    /**
     * 预约人
     */
    @TableField(value = "reservation_person")
    @ApiModelProperty(value = "预约人")
    private String reservationPerson;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    @ApiModelProperty(value = "手机号")
    private String phone;
    private Integer source;

    /**
     * 类型
     */
    @TableField(value = "`type`")
    @ApiModelProperty(value = "类型")
    private Integer type;

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
    private Integer state;

    @TableField(value = "`status`")
    private Integer status;

    private String appointmentSlot;
    private String mapName;
    private String parkingName;

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
    private String fid;
    private Integer floorLockId;
    private Integer mapId;


    private static final long serialVersionUID = 1L;
    private String deviceId;
    private Integer placeElevatorId;
    private Integer userid;
    private String floorName;
    @TableField(exist = false)
    private String vipType="place";
}