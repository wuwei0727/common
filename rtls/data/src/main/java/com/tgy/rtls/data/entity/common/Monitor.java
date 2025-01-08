package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/5
 * 检测信息类（项目和地图）
 */
@Data
@ToString
public class Monitor implements Serializable {
    private Integer id;//项目id，地图id
    private String name;//项目名/地图名
    private int manTime;//进出人数
    private int manNumber;//进出人次
    private String totalHours;//停留总时长
    private String percapitaHours;//人均时长
    private String timeHours;//次均时长

}
