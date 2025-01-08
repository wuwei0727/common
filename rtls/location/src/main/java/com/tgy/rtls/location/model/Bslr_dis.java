package com.tgy.rtls.location.model;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.location.netty.MapContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.LinkedBlockingDeque;

public class Bslr_dis {
    private MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
    public LinkedBlockingDeque<DisInf> filterDis=new LinkedBlockingDeque<>();//存储区域判定数据
    private Logger logger = LoggerFactory.getLogger(this.getClass());
/*    int delay=60;
    int percent=8;*/
    Timestamp time=new Timestamp(new Date().getTime());
    public Float[]  addDis(DisInf disInf,int tagFreq,int overtime,int cacheLen){
         Timestamp currentTimestamp=new Timestamp(new Date().getTime());
         Float[] res=new Float[2];
       // logger.info("cache dis"+filterDis.size());
       if(NullUtils.getTimeDifference(time,currentTimestamp)>overtime){
            this.filterDis.clear();
        }
       logger.info("cache dis ss"+filterDis.size());
        time=currentTimestamp;
        this.filterDis.push(disInf);
        int len=this.filterDis.size();
        if(len>= cacheLen){
            if(cacheLen>3) {
              // int filterResLen = this.filterDis.size();
               if(len!=cacheLen) {
                   filterDis.clear();
                   return getAverageDis();
               }
             else
                   filterDis.pollLast();
              //  logger.info("cache dis"+filterDis.size());

                Object[] disArrays = this.filterDis.toArray();
                Arrays.sort(disArrays);
                int disarray_len=disArrays.length;
                int start =  disarray_len/ 3;
                int end = disarray_len*2/3 ;
                double sum2 = 0;
                double sum1 = 0;
                double los1=0;
                double los2=0;
                int count1 = 0;
                int count2 = 0;


                boolean clflag=false;
                if(false) {
                    Arrays.sort(disArrays, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            DisInf disInf1 = (DisInf) o1;
                            DisInf disInf2 = (DisInf) o2;
                       /*     if( disInf1.getCl().compareTo(disInf2.getCl())==0) {
                                return disInf1.getDis().compareTo(disInf2.getDis());
                            }else{*/
                                return  disInf1.getCl().compareTo(disInf2.getCl());
                          //  }
                        }
                    });
                    for (int i = end; i < disarray_len; i++) {
                        DisInf disInf1 = (DisInf) disArrays[i];
                       sum1 = sum1 + disInf1.getDis();
                        los1=los1+disInf1.getCl();
                        count1++;
                    }
                    for (int i = 0; i < start; i++) {
                        DisInf disInf1 = (DisInf) disArrays[i];
                        sum2 = sum2 + disInf1.getDis();
                        los2=los2+disInf1.getCl();
                        count2++;
                    }
                    logger.info("遮挡判定"+los2/count2+":"+los1/count1);
                    if(count1==0||count2==0)
                        return getAverageDis();
                    if(los1/count1>0.7) {
                     logger.info("未遮挡");
                     //   res[0]=(Float.valueOf(String.format("%.2f", (float) (sum1 / count1)))).floatValue();
                        res[1]=0.1f;

                        DisInf disInf1 = (DisInf) disArrays[disarray_len/2];
                        res[0]= disInf1.getDis();
                        return res;
                    }
                    else   if(los2/count2<0.5) {
                        logger.info("遮挡");
                        DisInf disInf1 = (DisInf) disArrays[disarray_len/2];
                        res[0]=disInf1.getDis()-0.3f;
                        res[1]=0.3f;
                        return res;
                    }

                }else {
                    Object[] subList = Arrays.copyOfRange(disArrays, start, end);
                    for (int i = 0; i < end - start; i++) {
                        DisInf disInf1 = (DisInf) subList[i];
                        sum1 = sum1 + disInf1.getDis();
                        count1++;
                    }
                    res[0]=(Float.valueOf(String.format("%.2f", (float) (sum1 / count1)))).floatValue();
                    res[1]=0.1f;
                    return res;
                }
            }else{
                if(len!=cacheLen) {
                    filterDis.clear();
                    return getAverageDis();
                }
                this.filterDis.pollLast();
               return getAverageDis();
            }

        }else{
            return getAverageDis();
        }

           return getAverageDis();
    }


   /* public Float  getDis( ){
        Timestamp currentTimestamp=new Timestamp(new Date().getTime());
        //  System.out.println("time"+ByteUtils.getTimeDifference(time,currentTimestamp));
        if(NullUtils.getTimeDifference(time,currentTimestamp)>delay){
            this.filterDis.clear();
            return null;
        }
        int len=this.filterDis.size();
        if(this.filterDis.size()>=MapContainer.dis_cachelen){
            if(MapContainer.dis_cachelen>3) {
                Object[] disArrays = this.filterDis.toArray();
                Arrays.sort(disArrays);
                int start = disArrays.length / 3;
                int end = disArrays.length * 2 / 3;
                double sum = 0;
                int count = 0;
                Object[] subList = Arrays.copyOfRange(disArrays, start, end);
                *//*   list.sort(Comparator.comparing(DisInf::getCl));*//*
                boolean clflag=false;
                if(clflag) {
                    Arrays.sort(disArrays, new Comparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            DisInf disInf1 = (DisInf) o1;
                            DisInf disInf2 = (DisInf) o2;
                            return disInf1.getCl().compareTo(disInf2.getCl());
                        }
                    });
                    for (int i = 0; i < end - start; i++) {
                        DisInf disInf1 = (DisInf) disArrays[i];
                        sum = sum + disInf1.getDis();
                        count++;
                    }
                    return Float.valueOf(String.format("%.2f", (float) (sum / count)));
                }else {
                    for (int i = 0; i < end - start; i++) {
                        DisInf disInf1 = (DisInf) subList[i];
                        sum = sum + disInf1.getDis();
                        count++;
                    }
                    return Float.valueOf(String.format("%.2f", (float) (sum / count)));
                }
            }else{
                return this.filterDis.getFirst().getDis();

            }

        }else{
            return null;
        }

    }*/

    /**
     * 计算均值
     * @return
     */
    Float[] getAverageDis(){
        Float[] dis=new Float[2];
        Object[] objects = filterDis.toArray();
        double sum=0;
        float r=0;
        int count=0;
        for(Object obj:objects){
            DisInf difInf = (DisInf) obj;
            sum = sum + difInf.getDis();
            count++;
            }
        if(count>0) {
            dis[0] = (float) sum / count;
            double sumRe=0;
            int countRe=0;
            for(Object obj:objects){
                DisInf difInf = (DisInf) obj;
                if(Math.abs(difInf.getDis()-dis[0])<4){
                    sumRe=sumRe+difInf.getDis();
                    countRe++;
                }
            }

            if(countRe>0){
                dis[0]=(float)sumRe/countRe;
                dis[1]=  0.2f;
                return dis;
            }else{
                return null;
            }

        }else{
            return null;
        }

    }

    public static void main(String[] args) {
         LinkedBlockingDeque<DisInf> filterDis=new LinkedBlockingDeque<>();
        filterDis.add(new DisInf(0.8f,0.2f));
        filterDis.add(new DisInf(0.99f,0.24f));
        filterDis.add(new DisInf(0.95f,0.24f));
        filterDis.add(new DisInf(0.7f,0.2f));
        Object[] res = filterDis.toArray();

        Arrays.sort(res);
        Arrays.sort(res, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                DisInf disInf1 = (DisInf) o1;
                DisInf disInf2 = (DisInf) o2;
                if( disInf1.getCl().compareTo(disInf2.getCl())==0) {
                 return disInf1.getDis().compareTo(disInf2.getDis());
                }else{
                  return  disInf1.getCl().compareTo(disInf2.getCl());
                }
            }
        });
        System.out.println(res);
    }



}
