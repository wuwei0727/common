package com.tgy.rtls.data.algorithm;

import lombok.Data;

@Data
public class Floor implements Comparable<Floor>{
    private Integer id;
    private Integer floor;//楼层
    private Double height;//高度
    private String name;//楼层名
    private Double upper;//上限
    private Double lower;//下限
    public int compareTo(Floor o) {

        return this.getHeight().compareTo(o.getHeight());
    }


}
