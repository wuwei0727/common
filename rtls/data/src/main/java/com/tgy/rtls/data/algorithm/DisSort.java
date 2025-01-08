package com.tgy.rtls.data.algorithm;

import lombok.Data;

@Data
public class DisSort implements Comparable<DisSort>{
    public String bsname;
    public Double x;
    public Double y;
    public Double z;
    public Double dis;
    public Float r;
    public Float in;
    public DisSort(){

    }
    public DisSort(Double x,Double y,Double z,String bsname,double dis){
        this.bsname=bsname;
        this.x=x;
        this.y=y;
        this.z=z;
        this.dis=dis;
    }
    @Override

    public int compareTo(DisSort o) {

        return this.getDis().compareTo(o.getDis());
    }
}

