package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/19
 * 分站信息简介
 */
@Data
@ToString
public class BsSyn implements Serializable {
    private Integer id;
    private String num;//分站编号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private String networkName;//网络状态名
    private String powerName;//供电状态名
    private String errorName;//错误码名称
    private String mapName;//关联地图名
    private String typeName;//分站类型 1普通分站 2出入口分站
    private String batteryVolt;//电池电压
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date batteryTime;//电压检测时间
    private Integer power;

    private Double x;
    private Double y;

    private Short floor;//楼层
    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    /*
     * 开发者模式的字段
     * */
    private String release;//软件版本
    private String product;//硬件版本
    private String ucb1;//uwb版本号
    private int armupdatestate;//软件升级进度 -1失败 0正常 100成功 1-99升级中
    private int uwbupdatestate;//uwb升级进度
    private Integer map;
    private String floorName;

}
