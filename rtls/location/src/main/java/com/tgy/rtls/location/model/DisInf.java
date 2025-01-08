package com.tgy.rtls.location.model;

import lombok.Data;

@Data
public   class DisInf   implements Comparable<DisInf> {
    private Float dis;//距离
    private Float cl;//遮挡置信度
    private Float diff;//测距误差
    private Double x,y,z;
    private String bs;
    private Integer single;
    private Float r;

  public  DisInf(Float dis,Float cl){
      this.dis=dis;
      this.cl=cl;
  }
    @Override
    public int compareTo(DisInf o) {
        return this.dis.compareTo(o.dis);
    }
}
