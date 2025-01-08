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

/**
*@Author: wuwei
*@CreateTime: 2023/4/5 16:09
*/
/**
 * 道闸表
 * @author admin
 */
@ApiModel(description = "道闸表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "barrier_gate")
public class BarrierGate implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    @TableField(value = "device_num")
    @ApiModelProperty(value = "设备编号")
    private String deviceNum;

    /**
     * 地图
     */
    @TableField(value = "`map`")
    @ApiModelProperty(value = "地图")
    private Long map;

    /**
     * 绑定区域
     */
    @TableField(value = "binding_area")
    @ApiModelProperty(value = "绑定区域")
    private String bindingArea;

    /**
     * 状态
     */
    @TableField(value = "`state`")
    @ApiModelProperty(value = "状态")
    private Byte state;

    private static final long serialVersionUID = 1L;
    private Integer floor;

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

    private String barrierGateAreaStr;
    //蜂鸟地图相关
    private String mapName;
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
    private String z;
    private String floorName;

}