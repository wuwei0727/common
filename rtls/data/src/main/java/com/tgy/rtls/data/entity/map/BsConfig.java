package com.tgy.rtls.data.entity.map;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/20
 * 分站参数配置类 在地图管理中绑定配置
 */
@Data
public class BsConfig implements Serializable {
    private static final long serialVersionUID = 1761110805284950322L;

    private Integer id;
    private Integer bsid;//分站id
    private Double x;
    private Double y;
    private Double z;
    private Integer type;//分站功能类型
    private Integer leftid;//左端分站id
    private Double leftdis;//左端分站距离
    private Integer rightid;//右端分站id
    private Double rightdis;//右端分站距离
    private String antennadelay;//天线延时*/
    private String disfix;//校正参数
    private Short floor;//楼层
    private String sysnbsid;//天线延时
    private Integer map;
    private String lastTimeMap;
    private Short networkstate;
    private String networkName;
    private String power;
    private String batteryVolt;

    private String num;//分站编号
    private String name;//分站编号
    private String typeName;//分站功能类型名
    private String area;//区域
    private String mapName;//地图名称
    private String lifetimeMonths;//地图名称
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime batteryTime;//电压检测时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime addTime;//电压检测时间
}
