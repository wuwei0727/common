package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2024-01-04 14:53
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "cross_level_corridor")
public class CrossLevelCorridor implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "x")
    private String x;

    @TableField(value = "y")
    private String y;

    @TableField(value = "fid")
    private String fid;

    @TableField(value = "`name`")
    private String name;

    @TableField(value = "`map`")
    private Integer map;

    @TableField(value = "access_status")
    private Integer accessStatus;

    @TableField(value = "`floor`")
    private String floor;

    @TableField(value = "`type`")
    private Integer type;

    private String mapName;

    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    //地图路径
    private String mapImg;
    //主题路径
    private String themeImg;
    private String floorName;

    private static final long serialVersionUID = 1L;
}