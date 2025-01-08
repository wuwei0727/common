package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-05-24 23:24
*@Description: TODO
*@Version: 1.0
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "guidescreendevice")
public class GuideScreenDevice implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 设备编号
     */
    @TableField(value = "device_id")
    private Long deviceId;

    /**
     * 位置名称
     */
    @TableField(value = "location_name")
    private String locationName;

    /**
     * 关联地图
     */
    @TableField(value = "`map`")
    private Long map;

    @TableField(value = "x")
    private String x;

    @TableField(value = "y")
    private String y;

    @TableField(value = "`floor`")
    private String floor;

    /**
     * 网络状态
     */
    @TableField(value = "network_status")
    private Integer networkStatus;
    private String ip;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

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
    private String floorName;

    private static final long serialVersionUID = 1L;
}