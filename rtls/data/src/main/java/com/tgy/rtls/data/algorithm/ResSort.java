package com.tgy.rtls.data.algorithm;

import lombok.Data;

@Data
public class ResSort implements Comparable<ResSort>{
    public String bsname;
    public Double x;
    public Double y;
    public Double z;
    public Float iner;// 内部 0 外部1
    public Double squre;//面积
    public Float r;
    public ResSort(Double x, Double y, Double z, String bsname, Float iner, Double squre){
        this.bsname=bsname;
        this.x=x;
        this.y=y;
        this.z=z;
        this.iner=iner;
        this.squre=squre;
    }
    @Override

    public int compareTo(ResSort o) {


        if(this.getIner().compareTo(o.getIner()) == 0) {
            return this.getSqure().compareTo(o.getSqure());
        }else{
            return this.getIner().compareTo(o.getIner());
        }
    }
}

