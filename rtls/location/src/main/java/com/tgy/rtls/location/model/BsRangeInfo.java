package com.tgy.rtls.location.model;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

public class BsRangeInfo {
    public double average=-1;
    public int len=60;
    public LinkedBlockingDeque<Double> filterDis=new LinkedBlockingDeque<>();//基站测距队列

    public boolean  addRangeDis(Double dis){
        int  filterResLen=filterDis.size();
        filterDis.push(dis);
        if(filterResLen>len){
            filterDis.pollLast();
            getAverageDis();
            return true;
        }else
            return false;


    }

    public void  getAverageDis(){
        int  filterResLen=filterDis.size();
        LocFiterRes finalRes=null;
        if(filterResLen>=len) {
            Object[] array = filterDis.toArray();
            Arrays.sort(array);
            average=(double)array[array.length/2];
            System.out.println("平均距离"+average);
        }



    }

}
