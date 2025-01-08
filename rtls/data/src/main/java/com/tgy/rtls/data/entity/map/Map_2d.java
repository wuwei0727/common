package com.tgy.rtls.data.entity.map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/19
 * 地图信息
 */
@Data
@ToString
@ApiModel
@TableName("map_2d")
public class Map_2d implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @ApiModelProperty(value = "地图类型(1二维 2蜂鸟 3自制)")
    private Integer type;//地图类型(1二维 2蜂鸟 3自制)
    @ApiModelProperty(value = "地图名")
    private String name;//名称
    private String mapName;//名称
    private int subNum;//分站关联数
    private int baseNum;//微基站关联数
    private int gatewayNum;//网关关联数
    private int placeNum;
    @ApiModelProperty(value = "实际宽度")
    private Double width;//实际宽度
    @ApiModelProperty(value = "实际高度")
    private Double height;//实际高度
    @ApiModelProperty(value = "地图路径")
    private String url;//地图路径
    @ApiModelProperty(value = "原点")
    private String origin;//原点
    @ApiModelProperty(value = "地图描述")
    private String describe;//地图描述
    @ApiModelProperty(value = "是否启用(0否 1是)")
    private Integer enable;//是否启用 0否 1是
    private String instanceid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
    @ApiModelProperty(value = "基站样式")
    private Integer subStyle;//基站样式
    @ApiModelProperty(value = "标签样式")
    private Integer tagStyle;//标签样式
    @ApiModelProperty(value = "网关样式")
    private Integer gatewayStyle;
    @ApiModelProperty(value = "二维码")
    private String qrcode;
    @ApiModelProperty(value = "本地二维码")
    private String qrcodelocal;
    private String welcomePagePath;
    private String welcomePagePathlocal;
    private String defaultFloor;
    private String mapLogo;
    private String mapLogolocal;
    private String jumpAppId;
    private String jumpAppPage;



    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径

    //样式所在路径
    private String subUrl;
    private String tagUrl;
    private String gatewayUrl;
    //样式颜色
    private String subColor;
    private String tagColor;
    private String gatewayColor;

    //地图类型名
    private String typeName;
    private String lng;
    private String lat;
    private String shortLink;

    private String themeName;
    private Byte coordinate;
    private String floorName;
    private String placeName;
    private String companyName;
}
