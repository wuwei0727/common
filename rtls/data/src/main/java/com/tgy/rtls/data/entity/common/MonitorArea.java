package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/5
 * 检测信息类（区域）
 */
@Data
@ToString
public class MonitorArea implements Serializable {
    private Integer id;//区域id
    private String name;//区域名
    private String typeName;//类别名
    private String mapName;//地图名
    private int manTime;//进出人次
    private int manNumber;//进出人数
    private String totalHours;//停留总时长
    private String percapitaHours;//人均时长
    private String timeHours;//次均时长
    private Integer mapId;//地图id
}
