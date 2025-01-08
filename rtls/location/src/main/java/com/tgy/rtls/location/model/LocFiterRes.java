package com.tgy.rtls.location.model;

import net.sf.json.JSONArray;

import java.util.List;

public class LocFiterRes   implements Comparable<LocFiterRes>{
    public String bsname;
    public double percent;
    public int count;
    public int z_count;
    public Float x;
    public Float y;
    public Float z;
    public Short floor ;
    public float r;//精度因子
    public short type=-1;// 1 一维定位 2二维定位  3三维定位
    public float sumx,sumy,sumz;
    public int sumcount;
    public List<Float> x_list=null;
    public List<Float> y_list=null;
    public JSONArray disArray=new JSONArray();
    public LocFiterRes(String bsname,double percent,short type ) {
        this.bsname=bsname;
        this.percent=percent;
        this.type=type;
        // TODO Auto-generated constructor stub
    }

    public LocFiterRes(String bsname,float x,float y,float z,short type) {
        this.x=x;
        this.bsname=bsname;
        this.y=y;
        this.z=z;
        this.type=type;
        // TODO Auto-generated constructor stub
    }


    public LocFiterRes(String bsname,double percent,int count) {
        this.bsname=bsname;
        this.percent=percent;
        this.count=count;
        // TODO Auto-generated constructor stub
    }
    @Override

    public int compareTo(LocFiterRes o) {

        return this.getCount().compareTo(o.getCount());
    }
    private String getBsname() {
        // TODO Auto-generated method stub
        return this.bsname;
    }

    public Integer getCount() {
        // TODO Auto-generated method stub
        return count;
    }
}
