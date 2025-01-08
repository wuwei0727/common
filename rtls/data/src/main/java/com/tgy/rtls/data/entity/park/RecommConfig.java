package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-05-12 10:02
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommConfig implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 地图名称
     */
    @TableField(value = "`map`")
    private String map;
    private String mapName;
    private String placeList;
    private String x;
    private String y;
    private  String floor;
    private Long areaid;
    private Long placeid;
    private Integer areaPlaceId;
    private List<RecommConfigArea> vertexInfo;
    private String areaQuFen;

    /**
     * 区域名称
     */
    @TableField(value = "areaName")
    private String areaname;

    /**
     * 推荐级别：1高2中3低
     */
    @TableField(value = "recommelevel")
    private Integer recommelevel;

    /**
     * 开始时间
     */
    @TableField(value = "starttime")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime starttime;

    /**
     * 结束时间
     */
    @TableField(value = "endtime")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endtime;

    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径

    private static final long serialVersionUID = 1L;

}