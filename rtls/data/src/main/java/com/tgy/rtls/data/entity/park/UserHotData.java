package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
public class UserHotData implements Serializable,Comparable<UserHotData> {
    private Long id;
    private String name;//名字
    private String type;// 0:车位  1：地图内地点  2： 室外地点  3商家
    private String types;
    private String x;//坐标x  经度
    private String y;//坐标y  纬度
    private  String z;//
    private  Integer map;//地图
    private  String icon;//图标
    private  String ename;//图标
    private Integer floor;//
    private String fid;// 蜂鸟地图fid
    private String desc;
    private String  outdoorType;
    private String databaseId;
    private Double score;

    @Override
    public int compareTo(UserHotData o) {
        return (int)(this.id-o.id);
    }
}
