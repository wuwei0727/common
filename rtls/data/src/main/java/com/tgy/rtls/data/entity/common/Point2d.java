package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

/**
 * @author 许强
 * @Package com.example.bean
 * @date 2019/8/23
 * 点类型
 */
@Data
@ToString
public class Point2d {
    public double x;
    public double y;
    public int count;
    private String name;
    private String floor;
    private String map;
    private String tagid;


    public Point2d(double x, double y) { super(); this.x = x; this.y = y; }
    public Point2d(double x, double y,String name) { super(); this.x = x; this.y = y;this.name=name; }
    public Point2d(double x, double y,String name,String floor) { super(); this.x = x; this.y = y;this.name=name;this.floor=floor; }
    public Point2d(double x, double y,String name,String floor,String map) { super(); this.x = x; this.y = y;this.name=name;this.floor=floor;this.map=map; }
}
