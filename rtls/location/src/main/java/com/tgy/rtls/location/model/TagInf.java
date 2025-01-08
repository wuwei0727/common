package com.tgy.rtls.location.model;

import com.tgy.rtls.data.algorithm.Hilen;
import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.impl.BsConfigServiceImpl;
import com.tgy.rtls.data.service.user.PersonService;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfigImp;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.mqtt.HaoXiangMqtt;
import com.tgy.rtls.location.mqtt.PushCallback;
import com.tgy.rtls.location.netty.DataProcess;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.tdoa.BsTimestamps;
import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
@Data
public class TagInf implements  Comparable<TagInf> {
    public volatile float x;
    public volatile float y;
    public volatile float z;
    public long bsid;
    public JSONArray jsonArray=new JSONArray();
    public volatile long bsid_formercache;//上个基站的缓存
    public Timestamp locationTime=new Timestamp(new Date().getTime());
    public String tagId;
    public String firmwareVersion;
    public long date;
    public float volt;
    public short move;
    public volatile  short firstMove=0;
    public String area=null;
    public LinkedBlockingDeque<LocFiterRes> filterRes=new LinkedBlockingDeque<>();//存储区域判定数据
    public ConcurrentHashMap<String, Bslr_dis> range_bslr_dis = new ConcurrentHashMap<String, Bslr_dis>();
    public ConcurrentHashMap<Long, Bs_tagDis> screenCache = new ConcurrentHashMap<Long, Bs_tagDis>();
    private MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
   private KafukaSender kafukaSender = SpringContextHolder.getBean("kafukaSender");
    private  KafkaTemplate<String, String> kafkaTemplate=SpringContextHolder.getBean("kafkaTemplate");
    private BsParaConfig bsParaConfig = SpringContextHolder.getBean(BsParaConfigImp.class);
     volatile int freq=1;
     public volatile int steps;
    private ConcurrentHashMap<Long, BsTimestamps> tdoaTimestamp=new ConcurrentHashMap<>();//id timestamp
 //   volatile int percent=8;
 /*  int percent_delay=30;*/
/*    float left_right=0.01f;*/
       int dir=-1;
    double dis=-1;
    /*int delay=(int)(60);*/
    private Logger logger = LoggerFactory.getLogger(TagInf.class);
    //进入数据判定
    public   void setRegion(LocFiterRes locFiterRes,int overtime,int cacheLen){
    logger.error(tagId+":setRegion"+locFiterRes.bsname+"       "+locFiterRes.x+"   :  "+locFiterRes.y+"   :  "+locFiterRes.z);
           filterRes.push(locFiterRes);

           Timestamp currentTimestamp = new Timestamp(new Date().getTime());
        long diff = NullUtils.getTimeDifference(locationTime, currentTimestamp);
        logger.error("timdiff"+NullUtils.getTimeDifference(locationTime, currentTimestamp));
           if (diff > overtime) {
               this.filterRes.clear();
           }
           locationTime = currentTimestamp;
            int filterResLen = filterRes.size();
            LocFiterRes resPosition=null;
              if (filterResLen >(cacheLen)) {
                  if(firstMove<=0)
                  filterRes.pollLast();
                  resPosition = getLocation(cacheLen);

              }
            if(resPosition==null){
                resPosition=locFiterRes;
            }


           if (resPosition != null) {
               logger.error(tagId + "::" + resPosition.x + "y" + resPosition.y + ":" + resPosition.z);
               // logger.debug(tagId + "::" + res.bsname + "比例" + res.percent);
               TagLocation tagLocation = new TagLocation();
               tagLocation.setArea(resPosition.bsname);
               tagLocation.setTagid(tagId);
               tagLocation.setTime(new Date().getTime());
               tagLocation.setType((short) 0);
               tagLocation.setBsid(this.bsid);
               tagLocation.setFloor(resPosition.floor == null ? "" : resPosition.floor + "");
               tagLocation.setR(resPosition.r);
               DataProcess dataProcess = SpringContextHolder.getBean(DataProcess.class);
               if (dataProcess.debugFlag)
                   tagLocation.setDebugData(jsonArray);
               double[] former={this.x,this.y};
               double[] current={resPosition.x,resPosition.y};
               double[] filteRes = PushCallback.getWeightRes(former, current,3);
               this.x =(float) filteRes[0];
               this.y = (float) filteRes[1];
               this.z = resPosition.z;
               tagLocation.setX((float) (Math.round(this.x * 100)) / 100f);
               tagLocation.setY((float) (Math.round(this.y * 100)) / 100f);
               tagLocation.setZ((float) (Math.round(this.z * 100)) / 100f);
               logger.info(tagId+"taglocation::"+tagLocation.toString());
               kafkaTemplate.send(KafukaTopics.TAG_LOCATION, tagLocation.toString());
               HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
              // kafukaSender.send(KafukaTopics.TAG_LOCATION, tagLocation.toString());
               if(haoXiangMqtt!=null)
               haoXiangMqtt.publishLocationData(tagId,tagLocation);

           }
    }

    public static LocFiterRes changePercentToLocation( LocFiterRes res){
        if(res!=null){
            //  System.out.println(tagId+"定位结果区域"+res.bsname+"---percent"+res.percent);
            BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);

            //   BsInf bsInf1 = mapContainer.bsInf.get(bs1);
            //  BsInf bsInf2 = mapContainer.bsInf.get(bs2);
            String area[]=res.bsname.split(":");
            BsConfig bsInf1 = bsConfigService.findByNum((area[0]));
            BsConfig bsInf2 = bsConfigService.findByNum((area[1]));
            if(bsInf1==null||bsInf1.getX()==null)
                return null;
            if(bsInf2==null||bsInf2.getX()==null)
                return null;
            Double[] bs1Pos={bsInf1.getX(),bsInf1.getY(),bsInf1.getZ()};
            Double[] bs2Pos={bsInf2.getX(),bsInf2.getY(),bsInf2.getZ()};
            double[] resPosition= PercentToPosition.percentToPosition(bs1Pos,bs2Pos,res.percent);
                if(resPosition!=null){
                return
                        new LocFiterRes(res.bsname,(float) resPosition[0],(float)resPosition[1],(float)resPosition[2],(short)2);
                }else{
                    return null;
                }

        }else
            return null;
    }
   public TagInf(String tagId){
      this.tagId=tagId;

   }
    public TagInf( ){
        //this.tagId=tagId;

    }
  public   LocFiterRes getLocation(int cacheLen){
          int  filterResLen=filterRes.size();
            LocFiterRes finalRes=null;

        if(filterResLen>=cacheLen) {
            Object[] array = filterRes.toArray();
            ConcurrentHashMap<String,LocFiterRes> map = new ConcurrentHashMap<>();
            if(filterResLen!=cacheLen) {
                filterRes.clear();
                return null;
            }
            for(int i=0;i<filterResLen;i++){
                LocFiterRes locFiterRes = (LocFiterRes) array[i];
                String area=locFiterRes.bsname;
                float x=locFiterRes.x;
                float y=locFiterRes.y;
                float z=locFiterRes.z;
                LocFiterRes count_percent=map.get(area);

                if(count_percent!=null){
                    count_percent.count++;
                    count_percent.x=count_percent.x+x;
                    count_percent.y=count_percent.y+y;
                    count_percent.z = count_percent.z + z;
                    count_percent.r=count_percent.r+locFiterRes.r;
                    count_percent.z_count++;
                    count_percent.x_list.add(x);
                    count_percent.y_list.add(y);
               /*     if(z!=0) {
                        count_percent.z = count_percent.z + z;
                        count_percent.z_count++;
                    }*/
                }else{
                   count_percent= new LocFiterRes(area,x,y,z,(short) 1);
                    //count_percent.floor=count_percent.floor;
                    count_percent.count=1;
                    count_percent.x_list=new ArrayList<>();
                    count_percent.y_list=new ArrayList<>();
                  map.put(area,count_percent);
                }
            }

            for(int i=0;i<filterResLen;i++){
                LocFiterRes locFiterRes = (LocFiterRes) array[i];
                String area=locFiterRes.bsname;
                float x=locFiterRes.x;
                float y=locFiterRes.y;
                float z=locFiterRes.z;
                LocFiterRes count_percent=map.get(area);
                if(count_percent!=null){
                    if((Math.abs(x-count_percent.x/count_percent.count)+Math.abs(y-count_percent.y/count_percent.count))<4){
                        count_percent.sumx=count_percent.sumx+x;
                        count_percent.sumy=count_percent.sumy+y;
                        count_percent.sumz=count_percent.sumz+z;
                        count_percent.sumcount++;
                    }

                }
            }




            Iterator<Map.Entry<String, LocFiterRes>> iter = map.entrySet().iterator();
             List list=new ArrayList();
            while (iter.hasNext()){
                list.add(iter.next().getValue());
            }
            Collections.sort(list);
            int listLen=list.size();

             boolean containFormer=false;
          //  logger.info("location cache former::"+area);
            for(int k=0;k<listLen;k++){
                LocFiterRes eachRes = (LocFiterRes) list.get(k);
               // logger.info("location cache contain::"+eachRes.bsname);
                if(eachRes.sumcount>0&&eachRes.bsname.equals(area)) {
                    finalRes=new LocFiterRes(eachRes.bsname,0,0);
                    //均值取法
                    //finalRes.x = eachRes.sumx / eachRes.sumcount;
                    //finalRes.y = eachRes.sumy / eachRes.sumcount;
                    //中位数取法
                    Collections.sort(eachRes.x_list);
                    Collections.sort(eachRes.y_list);
                    finalRes.x = eachRes.x_list.get(eachRes.sumcount/2);
                    finalRes.y = eachRes.y_list.get(eachRes.sumcount/2);
                    finalRes.r=eachRes.r/eachRes.sumcount;
                   // finalRes.floor=eachRes.floor;
                  //  if (eachRes.z_count != 0)
                        finalRes.z = eachRes.sumz / eachRes.sumcount;
                    area=eachRes.bsname;
                    containFormer=true;
                }else{
                 //   System.out.println("count==0");
                }
            }

            if(area==null||!containFormer) {
                LocFiterRes topCount = (LocFiterRes) list.get(listLen - 1);
                if (topCount.sumcount > 0) {
                    finalRes = new LocFiterRes(topCount.bsname, 0, 0);
                 /*   finalRes.x = topCount.sumx / topCount.sumcount;
                    finalRes.y = topCount.sumy / topCount.sumcount;*/
                    //均值取法
                    //finalRes.x = eachRes.sumx / eachRes.sumcount;
                    //finalRes.y = eachRes.sumy / eachRes.sumcount;
                    //中位数取法
                    Collections.sort(topCount.x_list);
                    Collections.sort(topCount.y_list);
                    finalRes.x = topCount.x_list.get(topCount.sumcount/2);
                    finalRes.x = topCount.y_list.get(topCount.sumcount/2);
                    finalRes.r=topCount.r/topCount.sumcount;
                  //  if (topCount.z_count != 0)
                        finalRes.z = topCount.z / topCount.sumcount;
                    logger.info("location cache  no former::" + area + "::change to mostcount:" + topCount.bsname);
                    area = topCount.bsname;
                   // finalRes.floor=topCount.floor;
                }else{
                    /**
                     * 判断计次为1 时返回空值
                     */
                    return null;
            }
            }

        }

        return finalRes;

    }

   public void startLocation1D(long[] bsname, double[] originalDis,double[] dis,short[] lr,float rssi,float rssiFp,long date){
           long bs1=bsname[0];
           long bs2=bsname[1];
          // this.date=date;
       BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
       BsConfig bsInf1 = bsConfigService.findByNum( bs1+"");
       BsConfig bsInf2 = bsConfigService.findByNum( bs2+"");
       if(bsInf1==null||bsInf2==null||bsInf1.getDisfix()==null||bsInf2.getDisfix()==null)
           return;
           String[]  disfix1=bsInf1.getDisfix().split(":");
           String[]  disfix2=bsInf2.getDisfix().split(":");
           String lrdisfix1="";
           if(lr[0]==0)
               lrdisfix1=disfix1[0];
           else
               lrdisfix1=disfix1[1];

          String lrdisfix2="";

           if(lr[1]==0)
               lrdisfix2=disfix2[0];
           else
               lrdisfix2=disfix2[1];



       String[] ab1=lrdisfix1.split(",");
       String[] ab2=lrdisfix2.split(",");

/**
 *   计算校正距离
 */
       double  dis_fix1=-1;
           dis_fix1=Double.valueOf(ab1[0])*dis[0]+Double.valueOf(ab1[1]) ;
       double  dis_fix2=-1;
           dis_fix2=Double.valueOf(ab2[0])*dis[1]+Double.valueOf(ab2[1]) ;
     //  System.out.println(tagId+"bsname"+bsname[0]+ab1[0]+":"+ab1[1]+"距离"+dis_fix1);
     //logger.error(tagId+"bsname"+bsname[1]+":"+lr[1]+"距离"+dis_fix2);
       JSONObject jsonObject_0 = new JSONObject();
       JSONObject jsonObject_1=new JSONObject();
       jsonObject_0.put("originalDis",(float)Math.round(originalDis[0] * 100) / 100);
       jsonObject_0.put("filterDis",(float)Math.round(dis_fix1 * 100) / 100);
       jsonObject_0.put("bsid",bs1+ "-" + lr[0]);
       jsonObject_1.put("originalDis",(float)Math.round(originalDis[1] * 100) / 100);
       jsonObject_1.put("filterDis",(float)Math.round(dis_fix2 * 100) / 100);
       jsonObject_1.put("bsid",bs2+ "-" + lr[1]);
       JSONArray jsonArray=new JSONArray();
       jsonArray.add(jsonObject_0);
       jsonArray.add(jsonObject_1);
       if(jsonArray.size()>0)
       this.jsonArray=jsonArray;

           if(bs1!=0&&bs2!=0){
               int dir=-1;
               if(mapContainer.testFlag)
               dir=1;
               if(bs1==bs2){    //单基站两个模块测距结果数据处理

                   if(lr[0]==0) {
                       if(dis_fix2-dis_fix1>mapContainer.two_module_dis1D)
                           dir=0;
                       if(dis_fix1-dis_fix2>mapContainer.two_module_dis1D)
                           dir=1;
                   }
                   if(lr[1]==0) {
                       if(dis_fix1-dis_fix2>mapContainer.two_module_dis1D)
                           dir=0;
                       if(dis_fix2-dis_fix1>mapContainer.two_module_dis1D)
                           dir=1;
                   }

                   if(dir!=-1) {
                       Integer leftBs=bsInf1.getLeftid();
                       Integer  rightBs=bsInf1.getRightid();
                       int[] areabs=new int[2];
                       double dis_two_bs=0;
                       if(leftBs!=null&&rightBs!=null){
                           if(dir==0){
                               areabs[0]=leftBs;
                               dis_two_bs=bsInf1.getLeftdis();
                           }else{
                               areabs[0]=rightBs;
                               dis_two_bs=bsInf1.getRightdis();
                           }

                       }else{
                           if(leftBs!=null){
                               areabs[0]=leftBs;
                               dis_two_bs=bsInf1.getLeftdis();
                           }
                           if(rightBs!=null){
                               areabs[0]=rightBs;
                               dis_two_bs=bsInf1.getRightdis();
                           }

                       }
                       areabs[1]=(int)bs1;

                       if(bsid_formercache==0)
                           bsid_formercache=bsid;

                           //发送标签位置到基站




                       if(dis_fix1!=-1&&dis_fix2!=-1){

                        /*   if(bsid_formercache!=bs1){
                              bsParaConfig.setCoalBs_TagDis(bsid_formercache,tagId,(float)(dis_fix1+dis_fix2)/2,(byte) dir,rssi,rssiFp,(byte)0,"",(byte)0x00,0);
                           }*/
                           String name=tagId+"";
                            PersonService personService=SpringContextHolder.getBean(PersonService.class);
                            if(personService!=null) {
                                Person person = personService.findByTagNum(name);
                                if(person!=null){
                                    name=person.getName();
                                }
                            }
                           bsParaConfig.setCoalBs_TagDis(bs1,tagId,(float)(dis_fix1+dis_fix2)/2,(byte) dir,rssi,rssiFp,(byte)1, name,(byte)move,volt,(long)bs1);

                           if(screenCache.containsKey(bs1)){
                               screenCache.replace(bs1,new Bs_tagDis(name,tagId,bs1,(float)(dis_fix1+dis_fix2)/2,(byte)move,volt,(byte) dir));
                           }else {
                               screenCache.put(bs1,new Bs_tagDis(name,tagId,bs1,(float)(dis_fix1+dis_fix2)/2,(byte)move,volt,(byte) dir));
                           }
                            if( screenCache.size()>1){
                                Iterator<Map.Entry<Long, Bs_tagDis>> iter = screenCache.entrySet().iterator();
                                while (iter.hasNext()){
                                    Map.Entry<Long, Bs_tagDis> entry = iter.next();
                                    Long bsid_cache = entry.getKey();
                                    if(bsid_cache!=bs1){
                                        bsParaConfig.setCoalBs_TagDis(bsid_cache,tagId,(float)(dis_fix1+dis_fix2)/2,(byte) dir,rssi,rssiFp,(byte)0,"",(byte)0x00,0,null);
                                    }
                                }
                            }
                       }

                       Arrays.sort(areabs);
                      String area=areabs[0]+":"+areabs[1];

                       double percent=0;
                       if(areabs[0]==bs1) {
                           percent=(dis_fix1+dis_fix2)/(2*dis_two_bs);
                       }else {
                           percent=1-(dis_fix1+dis_fix2)/(2*dis_two_bs);
                       }
                       LocFiterRes res = changePercentToLocation(new LocFiterRes(area, percent, (short) 1));
                       if(res!=null) {
                          res.floor= bsInf1.getFloor();
                           setRegion(res,mapContainer.timedelay_highfreq,mapContainer.locationcachelen_highfreq);

                       }

                   }
                   this.bsid=bs1;


               }else{
                   //双基站两个模块测距结果数据处理
                   long[] bsarray= {bs1,bs2};
                   Arrays.sort(bsarray);
                   double dis_two_bs=0;
                   boolean neighbor=false;
                   if(bsInf1.getLeftid()==bs2){
                       neighbor=true;
                       dis_two_bs=bsInf1.getLeftdis();
                   }
                   if(bsInf1.getRightid()==bs2){
                       neighbor=true;
                       dis_two_bs=bsInf1.getRightdis();
                   }
                   if(bsInf2.getLeftid()==bs1){
                       neighbor=true;
                       dis_two_bs=bsInf2.getLeftdis();
                   }
                   if(bsInf2.getRightid()==bs1){
                       neighbor=true;
                       dis_two_bs=bsInf2.getRightdis();
                   }
                      if(neighbor) {
                          Double[][] bspos = {{0d, 0d, 0d}, {dis_two_bs, 0d, 0d}};
                          Double[] disArray = new Double[2];
                          if (bsarray[0] == bs1) {
                              disArray[0] = dis_fix1;
                              disArray[1] = dis_fix2;
                          } else {
                              disArray[0] = dis_fix2;
                              disArray[1] = dis_fix1;
                          }

                          double[][] pos = Hilen.location1D(disArray, bspos);
                          double percent = pos[0][0] / dis_two_bs;
                          LocFiterRes res = changePercentToLocation(new LocFiterRes(bsarray[0] + "-" + bsarray[1], percent, (short) 1));
                          if(res!=null) {
                              res.floor= bsInf1.getFloor();
                              setRegion(res,mapContainer.timedelay_highfreq,mapContainer.locationcachelen_highfreq);
                          }
                      }
               }

           }
    }
    public void singleDisLocation(Long bsid,Double originalDis,Float dis,int lr,long date){
         if(bsid==null)
             return;
        this.bsid=bsid;
      //  this.date=date;
    //   logger.error("单距离定位"+bsid+"dis:"+dis+":lr:"+lr);
        BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
        BsConfig bsInf1 = bsConfigService.findByNum(bsid+"");
        if(bsInf1==null)
            return;
        String[]  disfix1=bsInf1.getDisfix().split(":");
        String lrdisfix1="";
        if(lr==0)
            lrdisfix1=disfix1[0];
        else
            lrdisfix1=disfix1[1];
        String[] ab1=lrdisfix1.split(",");

/**
 *   计算校正距离
 */
        double  dis_fix1=-1;
        dis_fix1=Double.valueOf(ab1[0])*dis+Double.valueOf(ab1[1]) ;
        JSONObject jsonObject_0 = new JSONObject();
        jsonObject_0.put("originalDis",(float)Math.round(originalDis * 100) / 100);
        jsonObject_0.put("filterDis",(float)Math.round(dis_fix1 * 100) / 100);
        jsonObject_0.put("bsid",bsid+ "-" + lr);
        JSONArray jsonArray=new JSONArray();
        jsonArray.add(jsonObject_0);
        if(jsonArray.size()>0)
        this.jsonArray=jsonArray;
        LocFiterRes res = getLocation(4);
        if(res!=null){
            String area=res.bsname;
            String[] areas=res.bsname.split(":");
            BsConfig areaBsInf1 = bsConfigService.findByNum(areas[0]);
            BsConfig areaBsInf2 = bsConfigService.findByNum(areas[1]);
            if(areaBsInf1.getLeftid()==null&&areaBsInf1.getRightid()==null)
                return;
            double two_bs=0;
            Double percent=null;
            if(areas[0].equals(bsid+"")){
                if(areaBsInf1.getLeftid().intValue()==Integer.valueOf(areas[1])){
                    two_bs=areaBsInf1.getLeftdis();
                }else if(areaBsInf1.getRightid().intValue()==Integer.valueOf(areas[1])){
                    two_bs=areaBsInf1.getRightdis();
                }
                 percent=dis_fix1/two_bs;
            }else if(areas[1].equals(bsid+"")) {
                if(areaBsInf2.getLeftid().intValue()==Integer.valueOf(areas[0])){
                    two_bs=areaBsInf2.getLeftdis();
                }else if(areaBsInf2.getRightid().intValue()==Integer.valueOf(areas[0])){
                    two_bs=areaBsInf2.getRightdis();
                }
                 percent=1-dis_fix1/two_bs;
            }
            if(percent!=null) {
                res.percent = percent;
               // logger.error("单距离定位");
                changePercentToLocation(res);
            }
        }

    }





    public int compareTo(TagInf o) {

        return this.getTagId().compareTo(o.getTagId());
    }


}
