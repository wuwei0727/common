package com.tgy.rtls.data.entity.map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MapPathLabel implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "`name`")
    @ApiModelProperty(value="路口名称")
    private String name;

    @TableField(value = "`interiorName`")
    @ApiModelProperty(value="内部名称")
    private String interiorName;

    @TableField(value = "`map`")
    @ApiModelProperty(value="地图id")
    private Integer map;

    @TableField(value = "x")
    @ApiModelProperty(value="")
    private String x;

    @TableField(value = "y")
    @ApiModelProperty(value="")
    private String y;

    @TableField(value = "`floor`")
    @ApiModelProperty(value="楼层")
    private String floor;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    private String mapName;

    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String floorName;
}