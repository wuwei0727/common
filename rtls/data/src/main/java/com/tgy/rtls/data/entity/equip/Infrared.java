package com.tgy.rtls.data.entity.equip;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.config.LocalDateTimeReader;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@Data
//车位检测
public class Infrared implements Serializable {
    private Integer id;
    private Integer infraredId;
    private Integer placeId;
    private String num;
    private Double x;
    private Double y;
    private Short    networkstate;//
    private String networkName;//
    private String mapName;//
    private Short status;
    private Short state;
    private Short    floor;//
    private Short power;
    private Integer place;
    private String fid;
    private String placeName;
    private String license;
    private Integer map;
    private String hardware;
    private String firmware;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @JSONField(deserializeUsing = LocalDateTimeReader.class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime localDateTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    //蜂鸟地图相关
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss ", timezone = "GMT+8")
    private Date batteryTime;//电压检测时间
    private Integer count;
    @TableField("rawProductId")
    private String rawProductId;
    private String infraredName;
    private String[] ids;
    private String floorName;
    public Integer lifetimeMonths;
    public Infrared() {

    }
    public Infrared(LocalDateTime localDateTime, int lifetimeMonths) {
        this.localDateTime = localDateTime;
        this.lifetimeMonths = lifetimeMonths;
    }

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }


}
