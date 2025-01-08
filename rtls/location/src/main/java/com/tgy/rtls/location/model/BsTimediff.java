package com.tgy.rtls.location.model;

import com.tgy.rtls.data.algorithm.Location_highway;
import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.impl.BsConfigServiceImpl;
import com.tgy.rtls.location.netty.MapContainer;

import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;

public class BsTimediff {
    public volatile  double average=-1;
    public int len=60;
    public LinkedBlockingDeque<Double> filterTime=new LinkedBlockingDeque<>();//基站同步偏差队列

    public boolean  addTxTimestamp(Long sourceTimestamp,Long targetTimestamp,String source,String target){
        if(source.equals(target))
            return false;
        BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
        MapContainer mapContainer = SpringContextHolder.getBean(MapContainer.class);
        BsConfig source_inf = bsConfigService.findByNum(source);
        BsConfig target_inf = bsConfigService.findByNum(target);
        Double diff=null;
        if(source_inf!=null &&target_inf!=null){
            double[][] bsPos={{source_inf.getX(),source_inf.getY(),source_inf.getZ()},{target_inf.getX(),target_inf.getY(),target_inf.getZ()}};
            double dis_two_bs = PercentToPosition.getDis(bsPos[0], bsPos[1]);
            diff= Location_highway.getdiff(targetTimestamp,sourceTimestamp) +(dis_two_bs+77.81*2)/0.00469176397861579;
        }else
            return false;
        double diff_original=0;
        while (source_inf.getSysnbsid()!=null) {
            BsInf former = mapContainer.bsInf.get(source_inf.getSysnbsid());
            BsTimediff ss = former.bsTimeDiff.get(source);
            diff_original= ss.average+diff_original;
        }
        System.out.println(targetTimestamp+"::"+sourceTimestamp+":时钟差:");
        System.out.print(source+"::"+target+":时钟差:");
        if(diff!=null) {
            int filterResLen = filterTime.size();

            filterTime.push(diff+diff_original);
            if (filterResLen > len) {
                filterTime.pollLast();
                getAverageTimeDiff();
                return true;
            } else
                return false;
        }
        return false;

    }

    public void  getAverageTimeDiff(){
        int  filterResLen=filterTime.size();
        if(filterResLen>=len) {
            Object[] array = filterTime.toArray();
            Arrays.sort(array);
            average=(double)array[array.length/2];
            System.out.print("平均偏差"+average);
            System.out.println();
        }



    }

}
