package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParkingExit implements Serializable {
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
    private Integer accessStatus;
    private String roadName;
    @TableField(value = "`floor`")
    private String floor;
    @TableField(value = "`type`")
    @ApiModelProperty(value = "//0 入口 1 出口 2 出入口 3地库出入口")
    private Short type=0;
    @TableField(value = "z")
    private String z;
    @TableField(value = "doorx")
    private String doorx;
    @TableField(value = "doory")

    private String doory;
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
}