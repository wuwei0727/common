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
@TableName("showscreenconfig")
public class ShowScreenConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long guideScreenId;//导引屏设备ID
    private Long deviceNum;

    /**
     * 设备编号
     */
    @TableField(value = "devicenum")
    public String devicenum;

    /**
     * 屏幕编号
     */
    @TableField(value = "screennum")
    private String screennum;

    /**
     * 屏幕方位
     */
    @TableField(value = "screenposition")
    private String screenposition;

    @TableField(value = "`map`")
    private String map;
    private String mapName;

    /**
     * 绑定区域
     */
    @TableField(value = "bindarea")
    private String bindarea;

    /**
     * 网络状态
     */
    @TableField(value = "networkstatus")
    private Integer networkstatus;

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

    private String x;
    private String y;
    private  String floor;
    private Long areaId;
    private Long placeId;
    private Integer areaPlaceId;
    private List<ShowScreenConfigArea> vertexInfo;
    private String areaQuFen;

    /**
     * 车位
     */
    private String placeList;
    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    private String floorName;
}