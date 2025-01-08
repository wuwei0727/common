package com.tgy.rtls.location.netty;

import com.tgy.rtls.data.algorithm.*;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.check.BserrorcodetestEntity;
import com.tgy.rtls.data.entity.common.Point2d;
import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.location.Originaldata;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import com.tgy.rtls.data.kafukaentity.*;
import com.tgy.rtls.data.mapper.check.BserrorcodetestDao;
import com.tgy.rtls.data.mapper.check.TagcheckDao;
import com.tgy.rtls.data.mapper.check.TagcheckbsidDao;
import com.tgy.rtls.data.mapper.check.TagchecklocationDao;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.location.impl.LocationServiceImpl;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.impl.BsConfigServiceImpl;
import com.tgy.rtls.data.service.update.TagFirmwareService;
import com.tgy.rtls.data.snowflake.AutoKey;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.check.ErrorCodeInf;
import com.tgy.rtls.location.check.TagUpdateCheckRun;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.TagParaConfig;
import com.tgy.rtls.location.controller.Test;
import com.tgy.rtls.location.dataprocess.ProcessInterface;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.*;
import com.tgy.rtls.location.struct.*;
import io.netty.channel.Channel;
import javolution.io.Struct.Unsigned32;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.tgy.rtls.location.Utils.Constant.*;

@Component
public  class DataProcess implements ProcessInterface {
    @Autowired
    private MapContainer mapContainer ;
    @Autowired
    private SendData sendData ;
    @Autowired
    private BsParaConfig bsParaConfig;
    @Autowired
    private TagParaConfig tagParaConfig;
    @Autowired
    private KafukaSender kafukaSender;
    @Autowired
    private AutoKey autoKey;
    @Autowired
    private LocationServiceImpl locationService;
    @Autowired
    private SubService subService;
    @Autowired(required = false)
    private TagFirmwareService tagFirmwareService;
    @Autowired(required = false)
    TagchecklocationDao tagchecklocationDao;
    @Autowired(required = false)
    TagcheckbsidDao tagcheckbsidDao;
    @Autowired(required = false)
    TagcheckDao tagcheckDao;
   // public Timer timer=new Timer();

    int updateid;
    @Value("${audiofileurl}")
    String audioUrl;
    @Value("${backupfileurl}")
    String backUrl;
    @Value("${location.debugflag}")
    public  boolean debugFlag;
    @Value("${location.tagfreq}")
    int tagfreq;
    @Autowired(required = false)
    BserrorcodetestDao bserrorcodetestDao;
    @Autowired
    TagService tagService;

    private Logger logger = LoggerFactory.getLogger(DataProcess.class);
    /**
     * 煤炭项目心跳数据，携带充电状态和电压数据
     * @param bsid
     * @param data
     */

    public    void processCoalHeartData(Long bsid, ByteBuffer data){
        BsPower bsPower=new BsPower();
        bsPower.setByteBuffer(data,0);
        BsState bsState=new BsState();
        bsState.setBsid(bsid+"");
        bsState.setErrorCode((short)1);
        try {
            Channel channel = mapContainer.all_channel.get(bsid+"");
            InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
            bsState.setIp(channel != null ? addr.getAddress().getHostAddress() : null);
        }catch (Exception e){
            logger.error(bsid+":disconnect");
        }
        bsState.setState((short)(bsPower.charge_state.get()==1?0:1));
        bsState.setChargeVolt(Float.valueOf(String.format("%.2f",bsPower.charge_volt.get())));
        bsState.setBatteryVolt(Float.valueOf(String.format("%.2f",bsPower.battery_volt.get())));
        bsState.setTime(new Date().getTime());
        kafukaSender.send(KafukaTopics.BS_STATE,bsState.toString());

   //System.out.println(bsid+"---:"+bsPower.pkgId+"充电电压："+bsPower.charge_volt+"电池电压："+bsPower.battery_volt);
        bsParaConfig.sendHeartData(bsid);
    }


    /**
     * 铁科院多基站定位数据处理(4bs)
     * @param bsid
     * @param data
     */

    public    void process2D_4bs(Long bsid,ByteBuffer data) throws IOException {
        RailWay2D railWay2D=readData_4bs(data);

        BigDecimal ft1_1=ByteUtils.readUnsignedLong(railWay2D.group_poll_tx.get());
        long tagId=railWay2D.tagId.get();
        TagInf tagInf = mapContainer.tagInf.get(tagId+"");
        if(tagInf==null&&mapContainer.flag){
            tagInf=new TagInf(tagId+"");
            mapContainer.tagInf.put(tagId+"",tagInf);
        }
        if(tagInf==null)
            return;
        Tag tagfix = tagService.findByNum(tagId + "");

        if(tagfix!=null){
            tagInf.setFreq(tagfix.getFrequency()==0?1:tagfix.getFrequency());
        }

        ArrayList<Double[]> bsposList=new ArrayList();
        ArrayList<Double> bsposDis=new ArrayList();
        String bsList="";
        for(int i=0;i<4;i++){
            Unsigned32 bs = railWay2D.bss[i];
            Range2D rangeData = railWay2D.ts[i];
            BigDecimal ft2_1=ByteUtils.readUnsignedLong(rangeData.group_poll_rx.get());
            BigDecimal ft3_1=ByteUtils.readUnsignedLong(rangeData.group_resp_tx.get());
            BigDecimal ft4_1=ByteUtils.readUnsignedLong(rangeData.group_resp_rx.get());
            BigDecimal[] timeStamp1={ft1_1,ft2_1,ft3_1,ft4_1};
            if(ft1_1.doubleValue()==0||ft2_1.doubleValue()==0||ft3_1.doubleValue()==0||ft4_1.doubleValue()==0)
                return;
            double dis1= Range.getDis(timeStamp1,0,0);

            if(dis1<0||dis1>1000)
                return;
            Bslr_dis rangeInf1 = tagInf.range_bslr_dis.get(bs.get() + "-" +0);
            if (rangeInf1 == null) {
                rangeInf1 = new Bslr_dis();
                tagInf.range_bslr_dis.put(bs.get() + "-" + 0, rangeInf1);
            }
            Float[] filterDis1 = rangeInf1.addDis(new DisInf((float) dis1, 0f),tagInf.getFreq(),mapContainer.timedelay_highfreq,mapContainer.discachelen_highfreq);
           if(filterDis1!=null) {
                BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
                BsConfig bsInf1 = bsConfigService.findByNum(bs + "");
                if (bsInf1 == null || bsInf1.getDisfix() == null)
                    return;
                String[] disfix1 = bsInf1.getDisfix().split(":");
                String[] ab1 = disfix1[0].split(",");
                double dis_fix1 = -1;
                dis_fix1 = Double.valueOf(ab1[0]) * filterDis1[0] + Double.valueOf(ab1[1]);
                Double[] bspos={bsInf1.getX(),bsInf1.getY(),bsInf1.getZ(),(double)bs.get()};
                logger.info(tagId+"二维定位:"+bs+"--距离---"+dis_fix1);
                bsList=bsList+bs;
                bsposList.add(bspos);
                bsposDis.add(dis_fix1);
            }
           if(debugFlag) {
               Originaldata originaldata1 = new Originaldata();
               originaldata1.setTagid(tagId + "");
               originaldata1.setUpbsid(bsid + "");
               originaldata1.setRangid(railWay2D.rangeid + "");
               originaldata1.setRangebsid(bs + "");
               originaldata1.setTimestamp(new Date());
               originaldata1.setLr(railWay2D.cl + "");
               originaldata1.setRx3(railWay2D.group_final_tx.get() + "");
               originaldata1.setFt1c(timeStamp1[0] + "");
               originaldata1.setFt2c(timeStamp1[1] + "");
               originaldata1.setFt3c(timeStamp1[2] + "");
               originaldata1.setFt4c(timeStamp1[3] + "");
               originaldata1.setRssi("");
               originaldata1.setRssifp("");
               originaldata1.setOriginal_dis(dis1 + "");
               originaldata1.setFilter_dis(filterDis1 + "");
               locationService.addOriginaldata(originaldata1);
           }

        }

        int calculateBs = 4;
        if (bsposList.size() >= calculateBs) {
            DisSort res = calculPos(bsposList, bsposDis, calculateBs);
           if(res!=null) {
               LocFiterRes locFiterRes=new LocFiterRes(res.getBsname(), res.getX().floatValue(),res.getY().floatValue(),res.getZ().floatValue(),(short)1);
               tagInf.bsid=bsid;
               int len=bsposDis.size();
               String bsName=res.getBsname().split(":")[0];
               BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
               BsConfig bsInf1 = bsConfigService.findByNum(Integer.valueOf(bsName)+"");
               locFiterRes.floor=bsInf1.getFloor();
               tagInf.date= NullUtils.StringToDate(railWay2D.year.get(),railWay2D.month.get(),railWay2D.day.get(),railWay2D.hour.get(),railWay2D.minute.get(),railWay2D.second.get(),railWay2D.milisecond.get());
               tagInf.setRegion(locFiterRes,mapContainer.timedelay_highfreq,mapContainer.locationcachelen_highfreq);
           }
        }
    }


    /**
     * 铁科院多基站定位数据处理(8bs)
     * @param bsid
     * @param data
     */

    public    void process2D_8bs(Long bsid,ByteBuffer data) throws IOException {
        RailWay2D_8bs railWay2D=readData_8bs(data);


        long tagId=railWay2D.src.get();
    /*    if(tagId!=1000)
            return;*/
        TagInf tagInf = mapContainer.tagInf.get(tagId+"");
        if(tagInf==null&&mapContainer.flag){
            tagInf=new TagInf(tagId+"");
            mapContainer.tagInf.put(tagId+"",tagInf);
        }
        if(tagInf==null)
            return;
        Tag tagfix = tagService.findByNum(tagId + "");

        if(tagfix!=null){
            tagInf.setFreq(tagfix.getFrequency()==0?1:tagfix.getFrequency());
        }

        ArrayList<Double[]> bsposList=new ArrayList();//高频定位
        ArrayList<Double> bsposDis=new ArrayList();// 高频定位
     /*   ArrayList<Double[]> allBsposList=new ArrayList();//低频定位
        ArrayList<Double> allBsposDis=new ArrayList();///低频定位*/
        ArrayList<DisInf> allBsList=new ArrayList();

        String bsList="";
        JSONArray jsonArray=new JSONArray();
        for(int i=0;i<8;i++){
            Range2D_8bs bsinf_master = railWay2D.ts_master[i];
            Range2D_8bs bsinf_slave = railWay2D.ts_slave[i];

            double light_speed=299792458;
            double diff= Math.abs(bsinf_master.tof.get()-bsinf_slave.tof.get())*light_speed;
            //double dis= (bsinf_master.tof.get()+bsinf_slave.tof.get())*light_speed/2;
            double master_dis= (bsinf_master.tof.get())*light_speed;
            double slave_dis= (bsinf_slave.tof.get())*light_speed;
            double average_dis=(master_dis+slave_dis)/2;
             double final_dis=0;
            float cl=(bsinf_master.los_poll.get()+bsinf_master.los_resp.get()+bsinf_slave.los_poll.get()+bsinf_slave.los_resp.get())/4;
              if(!(master_dis==0&&slave_dis==0)) {
                  logger.info(tagId + ":rangeid:" + railWay2D.rangeid + ":8bs master:" + bsinf_master.bss.get() + ":dis:" + master_dis + ":los_poll:" + bsinf_master.los_poll + ":los_resp:" + bsinf_master.los_resp);
                  logger.info(tagId + ":rangeid:" + railWay2D.rangeid + ":8bs slave:" + bsinf_slave.bss.get() + ":dis:" + slave_dis + ":los_poll:" + bsinf_slave.los_poll + ":los_resp:" + bsinf_slave.los_resp);
              }else{
                  continue;
              }

            DisInf singleBsRange=new DisInf(0f,0f);
            singleBsRange.setBs(bsinf_master.bss.get()+"");
            if(debugFlag) {
                Originaldata originaldata1 = new Originaldata();
                originaldata1.setTagid(tagId + "");
                originaldata1.setUpbsid(bsid + "");
                originaldata1.setRangid(railWay2D.rangeid + "");
                originaldata1.setRangebsid(bsinf_master.bss.get() + "");
                originaldata1.setTimestamp(new Date());
                originaldata1.setLr(bsinf_master.los_poll.get()+"");
                originaldata1.setRx3(bsinf_master.los_resp.get()+"");
                originaldata1.setFt1c(bsinf_slave.los_poll.get()+"");
                originaldata1.setFt2c(bsinf_slave.los_resp.get()+"");
                originaldata1.setFt3c(master_dis+"");
                originaldata1.setFt4c(slave_dis+"");
                originaldata1.setRssi("");
                originaldata1.setRssifp("");
                originaldata1.setOriginal_dis( "");
                originaldata1.setFilter_dis( "");
                locationService.addOriginaldata(originaldata1);
            }

          if(bsinf_master.bss.get()==0||bsinf_master.tof.get()==0||bsinf_slave.bss.get()==0||bsinf_slave.tof.get()==0) {
               //logger.info(tagId+"==0 8bs :"+bsinf_slave.bss.get()+"--dis error ");
                if(bsinf_master.tof.get()!=0){
                    cl=(bsinf_master.los_resp.get()+bsinf_master.los_poll.get())/2;
                    singleBsRange.setDis((float)master_dis);
                    singleBsRange.setCl(cl);
                    singleBsRange.setDiff(3f);
                    singleBsRange.setSingle(1);
                    allBsList.add(singleBsRange);
                    final_dis=master_dis;
                }
                if(bsinf_slave.tof.get()!=0){
                    cl=(bsinf_slave.los_resp.get()+bsinf_slave.los_poll.get())/2;
                    singleBsRange.setDis((float)slave_dis);
                    singleBsRange.setCl(cl);
                    singleBsRange.setDiff(3f);
                    singleBsRange.setSingle(1);
                    allBsList.add(singleBsRange);
                    final_dis=slave_dis;
                }
               // continue;
            }else{
              final_dis=average_dis;
          }


            Bslr_dis rangeInf1 = tagInf.range_bslr_dis.get(bsinf_master.bss + "-" +0);
            if (rangeInf1 == null) {
                rangeInf1 = new Bslr_dis();
                tagInf.range_bslr_dis.put(bsinf_master.bss+ "-" + 0, rangeInf1);
            }

                if(diff>(mapContainer.two_module_dis2D)){
                    cl=0;
                     if(diff<(mapContainer.two_module_dis2D+5)){
                        singleBsRange.setDiff((float)diff);
                        singleBsRange.setSingle(0);
                        allBsList.add(singleBsRange);
                    }
                    continue;
                }


            Float[] filterDis1 = rangeInf1.addDis(new DisInf((float) final_dis,cl) ,tagInf.getFreq(),mapContainer.timedelay_highfreq,mapContainer.discachelen_highfreq);
            if(filterDis1!=null) {
                JSONObject jsonObject_0=new JSONObject();
                JSONObject jsonObject_1=new JSONObject();

               logger.info(tagId + "8bs final"+bsinf_master.bss+"dis:" +filterDis1[0].floatValue() );
                BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
                BsConfig bsInf1 = bsConfigService.findByNum(bsinf_master.bss + "");
                if (bsInf1 == null || bsInf1.getDisfix() == null)
                    continue;
                String[] disfix1 = bsInf1.getDisfix().split(":");
                String[] ab1 = disfix1[0].split(",");
                double dis_fix1 = -1;
                dis_fix1 = Double.valueOf(ab1[0]) * filterDis1[0] + Double.valueOf(ab1[1]);
                jsonObject_0.put("originalDis",(float)Math.round(master_dis * 100) / 100);
                jsonObject_0.put("filterDis",(float)Math.round(dis_fix1 * 100) / 100);
                jsonObject_0.put("bsid",bsinf_master.bss+"-0");
                jsonObject_1.put("originalDis",(float)Math.round(slave_dis * 100) / 100);
                jsonObject_1.put("filterDis",(float)Math.round(dis_fix1 * 100) / 100);
                jsonObject_1.put("bsid",bsinf_master.bss+"-1");
                Double[] bspos={bsInf1.getX(),bsInf1.getY(),bsInf1.getZ(),(double)bsinf_master.bss.get(),(double)filterDis1[1]};
                jsonArray.add(jsonObject_0);
                jsonArray.add(jsonObject_1);

                bsposList.add(bspos);
                bsposDis.add(dis_fix1);
            }


        }

        DisSort res=null;
        if (bsposList.size() >= 3) {
          //  DisSort res = calculPos(bsposList, bsposDis, calculateBs);
           res=chooseTwoBsFromMutipleBs(bsposList,bsposDis,false);
            int calculateBs = mapContainer.location_bsnum;
            if(res==null)
              res=calculPos_weight(bsposList, bsposDis, 4,0);
            if(res==null) {
                //过滤条件严苛，将原始测距数据重新计算出结果输出
                 res=calculPos_weight(bsposList, bsposDis, 3,0);
                logger.info("no 4 bs  ,use 3");
            }
            if(res==null&&mapContainer.location_strictmode==1){
                logger.info("no strict 4 and  3, use location_strictmode ");
                res= lowFreqFilter(allBsList,3);
            }


        }else{
           // logger.error("same line");
            if(bsposDis.size()==2){

        /*        Double[] near_bs1 = bsposList.get(0);
                Double[] near_bs2=bsposList.get(1);

                List<Double> nameList=new ArrayList();
                nameList.add(near_bs1[3]);
                nameList.add(near_bs2[3]);
                Collections.sort(nameList);*/
               res=chooseTwoBsFromMutipleBs(bsposList,bsposDis,true);
             /*   double dis_two_bs = PercentToPosition.getDis(near_bs1,near_bs2);
                Double[][] bspos = {{0d, 0d, 0d}, {dis_two_bs, 0d, 0d}};
                Double[] near_dis = {bsposDis.get(0),bsposDis.get(1)};
                double[][] pos = Hilen.location1D(near_dis, bspos);
                double percent = pos[0][0] / dis_two_bs;
                logger.info(tagId + "use 2" + percent);
                if (Math.abs(percent) < 1.3) {
                    double[] res1D = PercentToPosition.percentToPosition(near_bs1, near_bs1, percent);
                    res = new DisSort(res1D[0], res1D[1], res1D[2], Double.valueOf(nameList.get(0)).longValue() + ":" + Double.valueOf(nameList.get(1)).longValue(), 0d);
                    res.setR(0f);
                } else {
                    res = null;
                }*/
            }

        }
        if(res!=null) {
            LocFiterRes locFiterRes = new LocFiterRes(res.getBsname(), res.getX().floatValue(), res.getY().floatValue(), res.getZ().floatValue(), (short) 1);
            locFiterRes.r=res.getR();
            //  logger.error("8bs res"+"x:"+res.getX()+"y:"+res.getY());
            tagInf.bsid = bsid;
            BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
            if(jsonArray.size()>0)
                tagInf.jsonArray=jsonArray;
            String bsName=res.getBsname().split(":")[1];
            BsConfig bsInf1 = bsConfigService.findByNum(Integer.valueOf(bsName)+"");
            locFiterRes.floor=bsInf1.getFloor();
            tagInf.date = NullUtils.StringToDate(railWay2D.year.get(), railWay2D.month.get(), railWay2D.day.get(), railWay2D.hour.get(), railWay2D.minute.get(), railWay2D.second.get(), railWay2D.milisecond.get());
            tagInf.setRegion(locFiterRes,mapContainer.timedelay_highfreq,mapContainer.locationcachelen_highfreq);
        }
    }

    /**
     * 低频率时根据双模块距离差，距离远近，单分站，遮挡等因素进行定位
     * @return
     */
  public static DisSort lowFreqFilter(ArrayList<DisInf> rangeInfs, int   calculateBs){
      Object[] sdsa=rangeInfs.toArray();
        Arrays.sort(sdsa, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                DisInf disInf1 = (DisInf) o1;
                DisInf disInf2 = (DisInf) o2;
                if (disInf1.getSingle().compareTo(disInf2.getSingle()) == 0) {
                    if (disInf1.getDiff().compareTo(disInf2.getDiff()) == 0) {
                        if (disInf1.getDis().compareTo(disInf2.getDis()) == 0) {
                            return disInf2.getCl().compareTo(disInf1.getCl());
                        } else {
                            return disInf1.getDis().compareTo(disInf2.getDis());
                        }
                    } else {
                        return disInf1.getDiff().compareTo(disInf2.getDiff());
                    }

                } else {
                    return disInf1.getSingle().compareTo(disInf2.getSingle());
                }
            }
        });

      DisSort res=null;
      ArrayList<DisInf> location_data=new ArrayList();
      int i=0;
      for (Object single:sdsa
           ) {
          DisInf disInf=(DisInf)single;
          i++;
          if((disInf.getDiff()>10||disInf.getSingle()==1))
              continue;

          DisInf newDisInf=new DisInf(0f,0f);
          BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
          BsConfig bsInf1 = bsConfigService.findByNum(disInf.getBs() + "");
          if (bsInf1 == null || bsInf1.getDisfix() == null)
              continue;
          String[] disfix1 = bsInf1.getDisfix().split(":");
          String[] ab1 = disfix1[0].split(",");
          double dis_fix1 = -1;
          dis_fix1 = Double.valueOf(ab1[0]) * disInf.getDis() + Double.valueOf(ab1[1]);
          newDisInf.setDis((float)dis_fix1);
          newDisInf.setX(bsInf1.getX());
          newDisInf.setY(bsInf1.getY());
          newDisInf.setZ(bsInf1.getZ());
          newDisInf.setBs(disInf.getBs());
          float r=0;
          if(disInf.getSingle()>0)
              r=r+0.2f;
              r=r+disInf.getDiff();
          newDisInf.setR(r);


          location_data.add(newDisInf);
              if(location_data.size()==calculateBs){
                      Double[][] same_linearray = new Double[calculateBs][3];
                      Double[]   same_linedis=new Double[calculateBs];
                      int k = 0;
                      List<String> names=new ArrayList<>();
                      float bigR=0;
                      for (DisInf obj : location_data
                      ) {
                          same_linearray[k][0] = obj.getX();
                          same_linearray[k][1] = obj.getY();
                          same_linearray[k][2] = obj.getZ();
                          same_linedis[k]=(double)obj.getDis();
                          k++;
                          names.add(obj.getBs());
                          if(bigR<obj.getR())
                              bigR=obj.getR();
                      }
                      Collections.sort(names);
                      String area="";
                      for (String name:names
                      ) {
                          area=area+":"+Double.valueOf(name).longValue();
                      }

                      double[] forEach = M3D.location_Minum(same_linearray, same_linedis);
                      //   double squre=M3D.
                      if(forEach!=null){
                       //   logger.info(" user  near 4 bs location"+area);
                          res= new DisSort(forEach[0],forEach[1],forEach[2],area,0);
                          res.setR(bigR*1.2f);
                          break;
                      }else{
                          location_data.remove(newDisInf);
                      }
              }
      }
      if(res==null&&location_data.size()==3){

          float bigR=0;
          Double[][] same_linearray = new Double[3][3];
          Double[]   same_linedis=new Double[3];
          int k = 0;
          List<String> names=new ArrayList<>();
          for (DisInf obj : location_data
          ) {
              same_linearray[k][0] = obj.getX();
              same_linearray[k][1] = obj.getY();
              same_linearray[k][2] = obj.getZ();
              same_linedis[k]=(double)obj.getDis();
              if(bigR<obj.getR())
                  bigR=obj.getR();
              names.add(obj.getBs());
              k++;
          }

          Collections.sort(names);
          String area="";
          for (String name:names
          ) {
              area=area+":"+Double.valueOf(name).longValue();
          }
          //logger.info("get near 3 bs location");
          double[] forEach = M3D.location_Minum(same_linearray, same_linedis);
          double angle_small=30;
          double angle_big=150;
          for(int m=0;m<3;m++) {
              double[] x_array = {same_linearray[m%3][0],same_linearray[(m+1)%3][0],same_linearray[(m+2)%3][0]};
              double[] y_array = {same_linearray[m%3][1],same_linearray[(m+1)%3][1],same_linearray[(m+2)%3][1]};
              double angle1 = M3D.getAngleByThreeP(x_array, y_array);
              if(angle_small<angle1)
                  angle_small=angle1;
              if(angle_big>angle1)
                  angle_big=angle1;
          }

          if(forEach!=null&&angle_small==30&&angle_big==150){
              res= new DisSort(forEach[0],forEach[1],forEach[2],area,0);
              res.setR(bigR*1.2f);
          }

      }
      return res;

     }

    public static void main(String[] args) {

/*        List<ResSort> resList=new ArrayList();

        resList.add( new ResSort(0d,0d,0d,null,1f,8d));
        resList.add( new ResSort(0d,0d,0d,null,1f,5d));
        resList.add( new ResSort(0d,0d,0d,null,1f,7d));
        Collections.sort(resList);
        System.out.println(resList);*/
/*
       Double[] ss ={8d,8d,0.0,103d};
        Double[] ss1 ={-10d,-10d,0.0,100d};
        Double[] ss2 ={8d,0d,0.0,101d};
        Double[] ss3 ={0d,8d,0.0,102d};
        Double[] ss4 ={4d,4d,0.0,104d};
        ArrayList<Double> bsposDis=new ArrayList<>();
        ArrayList<Double[]> bsposList=new ArrayList<>();
        bsposList.add(ss);
        bsposList.add(ss1);
        bsposList.add(ss2);
        bsposList.add(ss3);
        bsposList.add(ss4);
        bsposDis.add(9.2d);
        bsposDis.add(16.27d);
        bsposDis.add(6.08d);
        bsposDis.add(7.28d);
        bsposDis.add(3.6d);

        DataProcess.calculPos_weight(bsposList, bsposDis, 4,0);*/

String sdasd="aaa\0bbb\0";
        System.out.println(sdasd);
        byte[] sss=sdasd.getBytes();
        System.out.println(ByteUtils.printHexString(sss));
        String[] sdasdds=sdasd.split("\0");
        System.out.println(sdasdds[0]);
        System.out.println(sdasdds[1]);

    }
    /**
     * 根据距离计算位置
     * @param bsposList
     * @param bsposDis
     * @param calculateBs
     * @return
     */
    public DisSort calculPos(ArrayList<Double[]> bsposList, ArrayList<Double> bsposDis, int calculateBs){

    Double[][] bsPos = bsposList.toArray(new Double[0][0]);
    Double[] bsDis = bsposDis.toArray(new Double[0]);
    ArrayList<DisSort> list = new ArrayList();
    int bsCount = bsDis.length;
    for (int i = 0; i < bsCount; i++) {
        DisSort disSort = new DisSort(bsPos[i][0], bsPos[i][1], bsPos[i][2],bsPos[i][3]+"", bsDis[i]);
        list.add(disSort);
    }
    Collections.sort(list);
    bsPos = new Double[calculateBs][3];
    bsDis = new Double[calculateBs];
    ArrayList<DisSort> calcul_list = new ArrayList();

    for (int i = 0; i < list.size(); i++) {
        DisSort newObject = list.get(i);
        calcul_list.add(newObject);
        if(i>=(calculateBs-1)){
            double[][] same_linearray = new double[calcul_list.size()][3];
            int k = 0;
            for (DisSort obj : calcul_list
            ) {
                same_linearray[k][0] = obj.getX();
                same_linearray[k][1] = obj.getY();
                same_linearray[k][2] = obj.getZ();
                k++;
            }

           Boolean sameLine = M3D.sameLine(same_linearray);
            if (sameLine) {
                calcul_list.remove(newObject);
            }
            if (calcul_list.size() >= calculateBs)
                break;
        }
    }
    double[] res = null;
    bsPos=new Double[calcul_list.size()][3];
    bsDis=new Double[calcul_list.size()];
    if (calcul_list.size() >= calculateBs) {
        int k = 0;
      List<String> names=new ArrayList<>();
        for (DisSort obj : calcul_list
        ) {
            bsPos[k][0] = obj.getX();
            bsPos[k][1] = obj.getY();
            bsPos[k][2] = obj.getZ();
            bsDis[k] = obj.getDis();
            names.add(obj.getBsname());
           logger.info("sortbsid:"+obj.getBsname()+":dis:"+bsDis[k]+"-----x:"+bsPos[k][0]+":y:"+bsPos[k][1]+":z:"+bsPos[k][2]);
            k++;
        }
        Collections.sort(names);
        String area="";
        for (String name:names
             ) {
            area=area+name;
        }
        res = M3D.location_Minum(bsPos, bsDis);
        System.out.println(res);
        if(res!=null) {
            return new DisSort(res[0],res[1],res[2],area,0);
        }else
            return null;
    }
    else
        return null;
}


    /**
     * 根据距离计算位置
     * @param bsposList
     * @param bsposDis
     * @param calculateBs
     * @return
     */
    public   static DisSort calculPos_weight( ArrayList<Double[]> bsposList, ArrayList<Double> bsposDis,int calculateBs,int strict){
         if(bsposDis.size()<calculateBs)
             return null;
        Double[][] bsPos = bsposList.toArray(new Double[0][0]);
        Double[] bsDis = bsposDis.toArray(new Double[0]);
        ArrayList<DisSort> list = new ArrayList();
        int bsCount = bsDis.length;
        for (int i = 0; i < bsCount; i++) {
            DisSort disSort = new DisSort(bsPos[i][0], bsPos[i][1], bsPos[i][2],bsPos[i][3]+"", bsDis[i]);
            disSort.setR(0f);
            list.add(disSort);
        }
        Collections.sort(list);
        ArrayList<DisSort> calcul_list = new ArrayList();
        ArrayList<ResSort> res_list = new ArrayList();
         int incomingBscount=list.size();
      //   int calculateBscount=0;
       /*     if(incomingBscount>=(calculateBs+1)){
                calculateBscount=calculateBs+1;
            }else{
                    calculateBscount=incomingBscount;
            }*/



        for (int i = 0; i < incomingBscount; i++) {
            DisSort newObject = list.get(i);
            calcul_list.add(newObject);

            if(i>=(calculateBs-1)){
                Double[][] same_linearray = new Double[calculateBs][3];
                Double[]   same_linedis=new Double[calculateBs];
                int k = 0;
                List<String> names=new ArrayList<>();
                float r=0;
                for (DisSort obj : calcul_list
                ) {
                    same_linearray[k][0] = obj.getX();
                    same_linearray[k][1] = obj.getY();
                    same_linearray[k][2] = obj.getZ();
                    same_linedis[k]=obj.getDis();
                    names.add(obj.getBsname());
                    if(r<obj.getR()){
                        r=obj.getR();
                    }
                    k++;
                }
                Collections.sort(names);
                String area="";
                for (String name:names
                ) {
                    area=area+":"+Double.valueOf(name).longValue();
                }

               // double[] forEach = M3D.location_Minum(same_linearray, same_linedis);
                double[] forEach = M3D.location_WeightMinum(same_linearray, same_linedis);
                boolean three_isok=false;
                if(calculateBs==3) {
                    double angle_small = 15;
                    double angle_big = 150;
                    for (int m = 0; m < 3; m++) {
                        double[] x_array = {same_linearray[m % 3][0], same_linearray[(m + 1) % 3][0], same_linearray[(m + 2) % 3][0]};
                        double[] y_array = {same_linearray[m % 3][1], same_linearray[(m + 1) % 3][1], same_linearray[(m + 2) % 3][1]};
                        double angle1 = M3D.getAngleByThreeP(x_array, y_array);
                        if (angle_small > angle1)
                            angle_small = angle1;
                        if (angle1>angle_big )
                            angle_big = angle1;
                    }
                    if(angle_small==15&&angle_big==150){
                        three_isok=true;
                    }

                }

             //   大于三个基站定位，且有定位结果，三级站定位，且构型良好
                if(calculateBs>3&&forEach!=null|| calculateBs==3&&three_isok){

                    List boundry = ConvexReg.isInPolygon(same_linearray);
                    Float in=   ArithmeticlUtil.isInPolygon(new Point2d(forEach[0],forEach[1]),boundry)?1f:0f;
                  double sqre= Hilen.getSqre(boundry,forEach);
                    //logger.info("dis range:"+area+":in area:"+in+":sqre:"+sqre+":x:"+forEach[0]+":y:"+forEach[1]);
                   ResSort resSort=new  ResSort(forEach[0],forEach[1],forEach[2],area,in,sqre);
                   if(in>0){
                       resSort.setR(r+0.1f);
                   }else{
                       resSort.setR(r+0.3f);
                   }
               /*    if(strict==1){
                       if(in.floatValue()>0.5f)
                           res_list.add(resSort);
                   }else*/
                         res_list.add(resSort);


                   break;
                }
                calcul_list.remove(newObject);
            }else{
               // logger.info("dis range:"+newObject.getBsname());
            }
        }
        Collections.sort(res_list);
        int len=res_list.size();
        ResSort finalResSort=null;
        for (int k=0;k<len;k++){
            ResSort resSort=res_list.get(k);
            if(resSort.getIner()==1){
                finalResSort=resSort;
                break;
            }
        }
        if(finalResSort==null){
            if(len>=1)
           finalResSort=res_list.get(len-1);



        /*  double[] total_res = M3D.location_Minum(bsPos,bsDis);
          if(total_res!=null) {
              List<Double> bsnames = new ArrayList<>();
              for (Double[] bsinf : bsPos
              ) {
                  bsnames.add(bsinf[3]);
              }
              Collections.sort(bsnames);
              String area = "";
              for (Double name : bsnames
              ) {
                  area = area + ":" + Double.valueOf(name).longValue();
              }
              finalResSort = new ResSort(total_res[0], total_res[1], total_res[2], area, 0f, 0d);
          }*/

        }

            if(finalResSort!=null) {
                //logger.info("final judge area:"+finalResSort.getBsname()+":x:"+finalResSort.getX()+"y"+finalResSort.getY());
                DisSort finalRes = new DisSort(finalResSort.getX(), finalResSort.getY(), finalResSort.getZ(), finalResSort.getBsname(), 0);
                finalRes.setR(finalResSort.getR());
                finalRes.setIn(finalResSort.iner);

                return finalRes;

            }
            return null;
    }


    /**
     * 四基站二维定位
     * @param data
     * @return
     */
  public   RailWay2D readData_4bs(ByteBuffer data){
        RailWay2D railWay2D=new RailWay2D();
        railWay2D.tagId.set(data.getInt());
        railWay2D.rangeid.set(data.getInt());
        railWay2D.bss[0].set(data.getInt());
        railWay2D.bss[1].set(data.getInt());
        railWay2D.bss[2].set(data.getInt());
        railWay2D.bss[3].set(data.getInt());
        railWay2D.group_poll_tx.set(data.getLong());
        railWay2D.group_final_tx.set(data.getLong());

        for(int i=0;i<4;i++){
            Range2D range2D=new Range2D();
            range2D.group_poll_rx.set(data.getLong());
            range2D.group_resp_tx.set(data.getLong());
            range2D.group_resp_rx.set(data.getLong());
            railWay2D.ts[i]=range2D;
        }
        railWay2D.cl.set(data.getFloat());
        railWay2D.year.set(data.getShort());
        railWay2D.month.set(data.get());
        railWay2D.day.set(data.get());
        railWay2D.hour.set(data.get());
        railWay2D.minute.set(data.get());
        railWay2D.second.set(data.get());
        railWay2D.milisecond.set(data.getShort());
        return railWay2D;
    }

    /**
     * 八基站二维定位
     * @param data
     * @return
     */
   public RailWay2D_8bs readData_8bs(ByteBuffer data){
        RailWay2D_8bs railWay2D=new RailWay2D_8bs();
        railWay2D.year.set(data.getShort());
        railWay2D.month.set(data.get());
        railWay2D.day.set(data.get());
        railWay2D.hour.set(data.get());
        railWay2D.minute.set(data.get());
        railWay2D.second.set(data.get());
        railWay2D.milisecond.set(data.getShort());
        railWay2D.type.set(data.get());
        railWay2D.src.set(data.getInt());
        railWay2D.dst.set(data.getInt());
        railWay2D.rangeid.set(data.getInt());

        for(int i=0;i<8;i++){
            Range2D_8bs range2D=new Range2D_8bs();
            range2D.bss.set(data.getInt());
            range2D.tof.set(data.getDouble());
            range2D.los_poll.set(data.getFloat());
            range2D.los_resp.set(data.getFloat());
            railWay2D.ts_master[i]=range2D;
        }
        for(int i=0;i<8;i++){
            Range2D_8bs range2D=new Range2D_8bs();
            range2D.bss.set(data.getInt());
            range2D.tof.set(data.getDouble());
            range2D.los_poll.set(data.getFloat());
            range2D.los_resp.set(data.getFloat());
            railWay2D.ts_slave[i]=range2D;
        }
      //  railWay2D.rangeid.set(data.getShort());

        return railWay2D;
    }

    /**
     * 单基站定位数据处理
     * @param bsid
     * @param data
     */

    public    void processSingleBsRange(Long bsid,ByteBuffer data) throws IOException {
        SingleBsRangeRes singleRange=new SingleBsRangeRes();
        singleRange.setByteBuffer(data,0);
        BigDecimal ft1_1=ByteUtils.readUnsignedLong(singleRange.ft1_1.get());
        BigDecimal ft2_1=ByteUtils.readUnsignedLong(singleRange.ft2_1.get());
        BigDecimal ft3_1=ByteUtils.readUnsignedLong(singleRange.ft3_1.get());
        BigDecimal ft4_1=ByteUtils.readUnsignedLong(singleRange.ft4_1.get());
        BigDecimal ft1_2=ByteUtils.readUnsignedLong(singleRange.ft1_2.get());
        BigDecimal ft2_2=ByteUtils.readUnsignedLong(singleRange.ft2_2.get());
        BigDecimal ft3_2=ByteUtils.readUnsignedLong(singleRange.ft3_2.get());
        BigDecimal ft4_2=ByteUtils.readUnsignedLong(singleRange.ft4_2.get());

        long tagId=singleRange.tagId.get();
    /*    if(tagId!=1000)
            return;*/
        TagInf tagInf = mapContainer.tagInf.get(tagId+"");
        if(tagInf==null){
            tagInf=new TagInf(tagId+"");
            mapContainer.tagInf.put(tagId+"",tagInf);
        }

        if(tagInf==null)
            return;

       // tagCheck(tagId,bsid);


        BigDecimal[] timeStamp1={ft1_1,ft2_1,ft3_1,ft4_1};
        BigDecimal[] timeStamp2={ft1_2,ft2_2,ft3_2,ft4_2};


/*

      Tag tagfix = tagService.findByNum(tagId+"");
        float fixValue=0;
          if(tagfix!=null){
                fixValue=tagfix.getFix();
              tagInf.setFreq(tagfix.getFrequency()==0?1:tagfix.getFrequency());
            }
*/

        float fixValue=0;

        double dis1= Range.getDis(timeStamp1,0,0)+fixValue;
        double dis2= Range.getDis(timeStamp2,0,0)+fixValue;

        logger.info("bsid---"+bsid+"---taid:"+tagId+":"+singleRange.rangeid+"::"+dis1+":"+dis2+"---rssi:"+singleRange.rssi.get()+":rssifp:"+singleRange.rssifp.get()+":置信度:"+singleRange.cl.get());


        // if(dis1<=10&&dis2<=10)
    /*  { TagLocation location=new TagLocation();
            location.setBsid(bsid);
            location.setX(tagInf.x);
            location.setY(tagInf.y);
            location.setZ(tagInf.z);
            location.setArea(tagInf.getArea());
            location.setTime(date);
            location.setType((short) 0);
            location.setTagid(tagId);
        logger.info("bsid---"+bsid+"---taid:"+tagId+":"+singleRange.rangeid+"::"+dis1+":"+dis2+"---rssi:"+singleRange.rssi.get()+":rssifp:"+singleRange.rssifp.get()+":置信度:"+singleRange.cl.get());


            if(debugFlag)
            location.setDebugData(tagInf.jsonArray);
            kafukaSender.send(KafukaTopics.TAG_LOCATION,location.toString());
        }*/
        if(debugFlag) {
            Originaldata originaldata1 = new Originaldata();
            Originaldata originaldata2 = new Originaldata();
            originaldata1.setTagid(tagId + "");
            originaldata1.setUpbsid(bsid + "");
            originaldata1.setRangid(singleRange.rangeid.get() + "");
            originaldata1.setRangebsid(singleRange.bsid1.get() + "");
            originaldata1.setTimestamp(new Date());
            originaldata1.setLr(singleRange.lr1.get() + "");
            originaldata1.setRx3(singleRange.rx3.get() + "");
            originaldata1.setFt1c(singleRange.ft1_1.get() + "");
            originaldata1.setFt2c(singleRange.ft2_1.get() + "");
            originaldata1.setFt3c(singleRange.ft3_1.get() + "");
            originaldata1.setFt4c(singleRange.ft4_1.get() + "");
            originaldata1.setRssi(singleRange.rssi.get() + "");
            originaldata1.setRssifp(singleRange.rssifp.get() + "");
            originaldata1.setOriginal_dis(dis1 + "");
            originaldata1.setFilter_dis(  "");

            originaldata2.setTagid(tagId + "");
            originaldata2.setUpbsid(bsid + "");
            originaldata2.setRangid(singleRange.rangeid.get() + "");
            originaldata2.setRangebsid(singleRange.bsid2.get() + "");
            originaldata2.setTimestamp(new Date());
            originaldata2.setLr(singleRange.lr2.get() + "");
            originaldata2.setRx3(singleRange.rx3.get() + "");
            originaldata2.setFt1c(singleRange.ft1_2.get() + "");
            originaldata2.setFt2c(singleRange.ft2_2.get() + "");
            originaldata2.setFt3c(singleRange.ft3_2.get() + "");
            originaldata2.setFt4c(singleRange.ft4_2.get() + "");
            originaldata2.setRssi(singleRange.rssi.get() + "");
            originaldata2.setRssifp(singleRange.rssifp.get() + "");
            originaldata2.setOriginal_dis(dis2 + "");
            originaldata2.setFilter_dis(  "");
            originaldata1.setX(mapContainer.d1);
            originaldata1.setY(mapContainer.d2);
            originaldata2.setX(mapContainer.d1);
            originaldata2.setY(mapContainer.d2);

            locationService.addOriginaldata(originaldata1);
            locationService.addOriginaldata(originaldata2);
            return;
        }

        long date=new Date().getTime();
        try {
            if (singleRange.year != null)
                date = NullUtils.StringToDate(singleRange.year.get(), singleRange.month.get(), singleRange.day.get(), singleRange.hour.get(), singleRange.minute.get(), singleRange.second.get(), singleRange.milisecond.get());
        }catch (Exception e){
            logger.error(e.getMessage(), e.getStackTrace());
        }

        if(mapContainer.testFlag)
        {
            dis2=dis1;
            singleRange.bsid2.set( singleRange.bsid1.get());

        }


        logger.info("bsid---"+bsid+"---taid:"+tagId+":"+singleRange.rangeid+"::"+dis1+":"+dis2+"---rssi:"+singleRange.rssi.get()+":rssifp:"+singleRange.rssifp.get()+":置信度:"+singleRange.cl.get());



        //  tagInf.bsid=bsid;

        Float[] filterDis1=null;
        Float[] filterDis2=null;

        if(singleRange.bsid1.get()!=0){
            Bslr_dis rangeInf1 = tagInf.range_bslr_dis.get(singleRange.bsid1.get() + "-" + singleRange.lr1.get());
            if (rangeInf1 == null) {
                rangeInf1 = new Bslr_dis();
                tagInf.range_bslr_dis.put(singleRange.bsid1.get() + "-" + singleRange.lr1.get(), rangeInf1);
            }
            filterDis1 = rangeInf1.addDis(new DisInf((float)dis1,singleRange.cl.get()),tagInf.getFreq(),mapContainer.timedelay_highfreq,mapContainer.discachelen_highfreq);
        }
        if(singleRange.bsid2.get()!=0) {
            Bslr_dis rangeInf2 = tagInf.range_bslr_dis.get(singleRange.bsid2.get() + "-" + singleRange.lr2.get());
            if (rangeInf2 == null) {
                rangeInf2 = new Bslr_dis();
                tagInf.range_bslr_dis.put(singleRange.bsid2.get() + "-" + singleRange.lr2.get(), rangeInf2);
            }

            filterDis2 = rangeInf2.addDis(new DisInf((float)dis2,singleRange.cl.get()),tagInf.getFreq(),mapContainer.timedelay_highfreq,mapContainer.discachelen_highfreq);
        }
        //  logger.error(singleRange.bsid1.get() + "- rangeid" +singleRange.rangeid+":"+singleRange.lr1.get()+"dis----"+filterDis1.floatValue()+"置信度:"+singleRange.cl.get());
        //      System.out.println(singleRange.bsid2.get() + "- rangeid" +singleRange.rangeid+":"+singleRange.lr2.get()+"dis----"+filterDis2.floatValue());




        if(singleRange.bsid1.get()*singleRange.bsid2.get()!=0||mapContainer.testFlag)
        {
            if(filterDis1==null|| filterDis2==null || Math.abs(filterDis1[0]-filterDis2[0])>4)
                return;
            long[] bsname=null;
            if(mapContainer.testFlag) {
                long[]  bsname1 = {singleRange.bsid1.get(), singleRange.bsid1.get()};
                bsname=bsname1;
            }else{
                long[]  bsname2 = {singleRange.bsid1.get(), singleRange.bsid2.get()};
                bsname=bsname2;
            }



            double[] dis = {filterDis1[0], filterDis2[0]};
            short[] lr=null;
            if(mapContainer.testFlag) {
                short[] lr1 = {0, 1};
                lr=lr1;
            }else{
                short[] lr2 = {singleRange.lr1.get(), singleRange.lr2.get()};
                lr=lr2;
            }

            double[] originalDis={dis1,dis2};
            tagInf.bsid=bsid;
            tagInf.date=date;
            tagInf.startLocation1D(bsname,  originalDis, dis, lr, singleRange.rssi.get(), singleRange.rssifp.get(),date);

        }else{
            Long singleBs=null;
            Float dis=null;
            Double orignal_dis=null;
            int lr=-1;
            if(filterDis1!=null){
                singleBs=singleRange.bsid1.get();
                dis=filterDis1[0];
                lr=singleRange.lr1.get();
                orignal_dis=dis1;
            }
            if(filterDis2!=null){
                singleBs=singleRange.bsid2.get();
                dis=filterDis2[0];
                lr=singleRange.lr2.get();
                orignal_dis=dis2;
            }
            tagInf.singleDisLocation(singleBs,orignal_dis,dis,lr,date);

        }



      /*  System.out.println(bsid
                + "上传测距：标签编号:" +singleRange.tagId+":"
                + "测距基站:" +singleRange.bsid1+"--"+singleRange.lr1+":"
                + "测距id:" +singleRange.rangeid+":"
                + "rx3:" +singleRange.rx3+":"
                + "信号强度:" +singleRange.rssi
                + "距离:" +dis1
                + "滤波后距离:" +filterDis1

        );
        System.out.println(bsid
                + "上传测距：标签编号:" +singleRange.tagId+":"
                + "测距基站:" +singleRange.bsid2+"--"+singleRange.lr2+":"
                + "测距id:" +singleRange.rangeid+":"
                + "rx3:" +singleRange.rx3+":"
                + "信号强度:" +singleRange.rssi
                + "距离:" +dis2
                  + "滤波后距离:" +filterDis2
        );*/
    }


    /**
     * 基站异常码处理
     * @param bsid
     * @param data
     */
    public void processBsError(Long bsid,ByteBuffer data){
        BsError error=new BsError();
        error.setByteBuffer(data,0);
        System.out.println(bsid+"异常码:"+error.errorCode+"异常状态:"+error.erroeState);

    }











    /**
     * 标签上传的传感器数据
     * @param bsid
     * @param data
     */
    public    void processTagDataUplink(Long bsid,int tagid,ByteBuffer data,short len) throws IOException {


          byte cmd=data.get();

        TagInf tagInf = mapContainer.tagInf.get((long)tagid+"");
        if(tagInf==null&&mapContainer.flag){
            tagInf=new TagInf(tagid+"");
            mapContainer.tagInf.put(tagid+"",tagInf);
        }
      //  System.out.println("tag config::::"+tagid);
      //  String or=ByteUtils.printHexString(data.array());
       // System.out.println("cmd"+"-------------------"+cmd);

        switch (cmd){
            case 0x04://升级
                byte[] sensor=new byte[(int)len-1];
                data.get(sensor,0,(int)len-1);
                ByteBuffer updateRes = ByteBuffer.wrap(sensor);
                updateRes.order(ByteOrder.LITTLE_ENDIAN);
                byte updateCmd=updateRes.get();
                switch (updateCmd){
                    case 0x01:
                        TagFirmware tagFirmware=new TagFirmware();
                        tagFirmware.setByteBuffer(updateRes,0);
                        try {
                            tagParaConfig.processTagUpdate(bsid,(long)tagid,tagFirmware,null,null,null,null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x02:

                        int pkgid=updateRes.getInt();
                        int pkgsize=updateRes.getInt();
                        System.out.println("update tagid:"+tagid+"pkgid:"+pkgid);
                        TagInf taginf = mapContainer.tagInf.get(tagid+"");
                        if(taginf.firmwareVersion!=null&&mapContainer.tagFirmWare.containsKey(taginf.firmwareVersion)) {
                            byte[] firmware=mapContainer.tagFirmWare.get(taginf.firmwareVersion);
                            tagParaConfig.sendOtaData(bsid,tagid, firmware, pkgid, pkgsize);
                        }
                        break;
                    case 0x03: tagParaConfig.processTagUpdateFinish(bsid,tagid,updateRes.get());
                        break;
                }
                break;
            case 0x05://标签参数配置结果
                byte configCmd=data.get();
                TagPara tagPara=new TagPara();
                tagPara.setTagid(tagid+"");
                switch (configCmd){
                    case 0x01://修改id
                        tagPara.setKeyOrder("id");
                        tagPara.setTagid(data.getInt()+"");
                        break;
                    case 0x02://蜂鸣器
                        tagPara.setKeyOrder("beep");
                        tagPara.setBeepState(data.get());
                        tagPara.setBeepInterval(data.getShort());
                        break;
                    case 0x03://指定测距
                        break;
                    case 0x04://循环测距
                        break;
                    case 0x05://定位参数
                        tagPara.setKeyOrder("locpara");
                        tagPara.setLoc_inval(data.getInt());
                        tagPara.setRx_inval(data.getInt());
                        break;
                    case 0x06://功率
                        tagPara.setKeyOrder("power");
                        tagPara.setPa(data.get());
                        tagPara.setPowerLevel(data.get());
                        Test.res=tagid+":pa:"+tagPara.getPa()+":gain:"+tagPara.getPowerLevel();
                        System.out.println("标签反馈功率结果"+tagid+":pa:"+tagPara.getPa()+":gain:"+tagPara.getPowerLevel());
                        break;
                    case 0x07://重启
                        tagPara.setKeyOrder("reboot");
                        tagPara.setReboot(data.get());
                        break;
                    case 0x08://低功耗
                        tagPara.setKeyOrder("lowpower");
                        tagPara.setLowPowerMode(data.get());
                        break;
                    case 0x09://传感器周期
                        tagPara.setKeyOrder("sensorperiod");
                        tagPara.setSensorInterval(data.getInt());
                        break;
                    case 0x10://运动阈值检测
                        tagPara.setKeyOrder("movelevel");
                        tagPara.setMoveLevel(data.getShort());
                        break;
                    case 0x11://心跳周期
                        tagPara.setKeyOrder("heartperiod");
                        tagPara.setHeartInterval(data.getInt());
                        break;
                    case  0x12://标签工作模式
                        tagPara.setKeyOrder("mode");
                        tagPara.setMode(data.get());
                        tagPara.setPeriod(data.get());
                        if(Test.tagstate.containsKey(tagid+""))
                        {
                            Short setMode = Test.tagstate.get(tagid + "");
                            if(setMode.shortValue()==tagPara.getMode()){
                                Test.tagstate.replace(tagid+"",(short) 10);
                            }
                        }

                        break;
                    case 0x13://获取标签版本号
                        int  bootloader_h=data.get();
                        int  bootloader_m=data.get();
                        int  bootloader_l=data.get();
                        int  firmware_h=data.get();
                        int  firmware_m=data.get();
                        int  firmware_l=data.get();
                        int  hardware_h=data.get();
                        int  hardware_m=data.get();
                        int  hardware_l=data.get();
                        TagfirmwareEntity tagFirmware=tagFirmwareService.findByTagid((long)tagid);
                        Boolean exist=false;
                        if(tagFirmware==null) {
                          tagFirmware=new TagfirmwareEntity();
                        }else{
                            exist=true;
                        }
                        tagFirmware.setUtc(new Date().getTime());
                        tagFirmware.setBootloader(bootloader_h+"."+bootloader_m+"."+bootloader_l);
                        tagFirmware.setFirmware(firmware_h+"."+firmware_m+"."+firmware_l);
                        tagFirmware.setHardware(hardware_h+"."+hardware_m+"."+hardware_l);
                        tagFirmware.setUpdatestate(100);
                        if(exist){
                            tagFirmwareService.updateById(tagFirmware);
                        }else
                            tagFirmwareService.insert(tagFirmware);
                        tagPara.setKeyOrder("versioninf");
                        tagPara.setBootloader(tagFirmware.getBootloader());
                        tagPara.setFirmware(tagFirmware.getFirmware());
                        tagPara.setHardware(tagFirmware.getHardware());
                        break;
                    case 0x14://标签时间配置响应
                        break;
                    case 0x15:
                        tagPara.setKeyOrder("groupbslist");
                        String bslists="";
                        for (int i=0;i<8;i++){
                            if(i<7)
                            bslists=bslists+data.getInt()+",";
                            else
                                bslists=bslists+data.getInt();
                        }
                        tagPara.setGroupbslist(bslists);
                        break;
                    case 0x16:
                        tagPara.setKeyOrder("grouprangetime");
                        tagPara.setGrouprangetime(data.getInt());
                        break;

                }
                kafukaSender.send(KafukaTopics.TAG_CONTROLRES,tagPara.toString());
                break;
            case 0x06://标签开机发送的信息
                byte[] initdata=new byte[(int)len-1];
                data.get(initdata,0,(int)len-1);
                ByteBuffer initbuf = ByteBuffer.wrap(initdata);
                initbuf.order(ByteOrder.LITTLE_ENDIAN);
                TagCurrentInf tagCurrentInf=new TagCurrentInf();
                tagCurrentInf.setByteBuffer(initbuf,0);
                TagInitInf tagInitInf=new TagInitInf();
                tagInitInf.tagid=tagid;
                tagInitInf.bootLoaderVersion=tagCurrentInf.boot_ver_h.get()+"."+tagCurrentInf.boot_ver_m.get()+"."+tagCurrentInf.boot_ver_l.get();
                tagInitInf.firmWareVersion=tagCurrentInf.fm_ver_h.get()+"."+tagCurrentInf.fm_ver_m.get()+"."+tagCurrentInf.fm_ver_l.get();
                tagInitInf.hardWareVersion=tagCurrentInf.hd_ver_h.get()+"."+tagCurrentInf.hd_ver_m.get()+"."+tagCurrentInf.hd_ver_l.get();
                tagInitInf.gain=tagCurrentInf.gain.get();
                tagInitInf.locationInterval=tagCurrentInf.loc_period.get();
                tagInitInf.moveLevel=tagCurrentInf.move_level.get();
                tagInitInf.hr_on_ms=tagCurrentInf.heart_on_ms.get();
                tagInitInf.hr_off_ms=tagCurrentInf.heart_off_ms.get();
                kafukaSender.send(KafukaTopics.TAG_INITPARA,tagInitInf.toString());
                TagfirmwareEntity tagfirmwareEntity=tagFirmwareService.findByTagid((long)tagid);;
                boolean insert=false;
                if(tagfirmwareEntity==null) {
                    insert=true;
                    tagfirmwareEntity = new TagfirmwareEntity();
                }
                tagParaConfig.setTagTimestamp(bsid,tagid,null);
                tagfirmwareEntity.setHardware(tagInitInf.hardWareVersion);
                tagfirmwareEntity.setBootloader(tagInitInf.bootLoaderVersion);
                tagfirmwareEntity.setFirmware(tagInitInf.firmWareVersion);
                tagfirmwareEntity.setTagid(tagid);
                tagfirmwareEntity.setUpdatestate(100);
                if(insert)
                    tagFirmwareService.insert(tagfirmwareEntity);
                else
                    tagFirmwareService.updateById(tagfirmwareEntity);
                break;
            case 0x07://标签上传的传感器信息
                byte[] sensordata=new byte[(int)len-1];
                data.get(sensordata,0,(int)len-1);
                ByteBuffer sensorbuf = ByteBuffer.wrap(sensordata);
                sensorbuf.order(ByteOrder.LITTLE_ENDIAN);
                TagSensorData tagSensorData=new TagSensorData();
                tagSensorData.setByteBuffer(sensorbuf,0);
                TagSensor tagSensor=new TagSensor();
                try {
                    int len1 = tagSensorData.size();
                    int heartNum=tagSensorData.hr_num.get();
                    byte[] heart = new byte[heartNum];
                    int sum=0;
                    for(int i=len1;i<(len-1);i++){
                        sum=heart[i];
                    }
                    tagSensor.setHeart((short)(sum/heartNum));

                }catch (Exception e){

                }
             /*  System.out.println(
                         "标签传感数据"+
                                 "电压"+tagSensorData.volt+
                                 "SOS"+tagSensorData.sos
                         );*/
                tagSensor.setTime(new Date().getTime());
                tagSensor.setTagid(tagid+"");
                tagSensor.setSos(tagSensorData.sos.get());
                tagSensor.setPower(tagSensorData.volt.get());
                tagSensor.setBroken(tagSensorData.alarm.get());
                tagSensor.setTemper(tagSensorData.temper.get());
                tagSensor.setMoveState(tagSensorData.move.get());
                tagSensor.setBsid(bsid+"");
                tagInf.volt=(Math.round(tagSensorData.volt.get() * 100)) / 100;
                tagInf.move=tagSensorData.move.get();
                kafukaSender.send(KafukaTopics.TAG_SENSOR,tagSensor.toString());
            /*    System.out.println(tagid+"fixtime"+new Date().getTime()/1000+"---tag time"+tagSensorData.time.get());
                if(Math.abs(new Date().getTime()/1000-tagSensorData.time.get())>600){
                    System.out.println(tagid+"start fixtime"+new Date().getTime()/1000+"---tag time"+tagSensorData.time.get());
                    tagParaConfig.setTagTimestamp(bsid,tagid,new Date().getTime()/1000);
                }
*/
                break;
            case 0x08://标签上传的文字语音等确认信息
                int fileid=data.getInt();
                int type=data.get();
                int state=data.get();
                switch (type){
                    case 0:
                        FilePara filePara=new FilePara();
                        filePara.setTarget((long)tagid);
                        filePara.setState((short)(state));
                        filePara.setMessageid(fileid);
                        filePara.setTime(data.getInt()*1000);
                        kafukaSender.send(KafukaTopics.FIlE_RES,filePara.toString());
                        break;
                    default:
                        TextPara textPara=new TextPara();
                        textPara.setMessageid(fileid);
                        textPara.setState((short)(state));
                        textPara.setBsid(bsid);
                        textPara.setTarget((long)tagid);
                        textPara.setType((short)(type));
                        textPara.setTime((long)(data.getInt()*1000));
                        kafukaSender.send(KafukaTopics.TEXT_RES,textPara.toString());
                        break;
                }

            default:

                break;
        }


    }


    /**
     *
     * @param bsid
     * @param data
     */
    public    void processFileSendRes(Long bsid,ByteBuffer data){
                  int target= data.getInt();//文件推送目标，0为基站，其他为标签
                  int fileId= data.getInt();//文件id

        System.out.println(bsid
                + "文件推送反馈目标:" +target+":"
                + "文件id:" +fileId
        );
    }

    /**
     * 文件传输进度
     * @param bsid
     * @param data
     * @param type  0: 传输中  1： 传输结束
     */
    public void processFilePushStatus(Long bsid,ByteBuffer data,int type){
        FilePara filePara =new FilePara();
        int tagid=data.getInt();
        int fileid=data.getInt();
        int process=0;
        switch (type){
            case 0://文件传输中
                process=data.getInt();
                filePara.setState((short)0);
                break;
            case 1://文件传输结束
                process=100;
                filePara.setState((short)1);
                break;
        }
        filePara.setBsid(bsid);
        filePara.setTarget((long)tagid);
        filePara.setMessageid(fileid);
        filePara.setDirection((short) 1);
        filePara.setProcess((short)process);
        filePara.setState((short)0);
        kafukaSender.send(KafukaTopics.FIlE_RES,filePara.toString());

    }

    /**
     *
     * @param bsid
     * @param data
     * @param type  0文件上传请求   1：文件上传结束
     */
   public void processFileUpload(Long bsid,ByteBuffer data,int type){
       int tagid=data.getInt();
       FilePara audioPara=new FilePara();
       audioPara.setBsid(bsid);
       audioPara.setDirection((short) 0);
       int fileid=0;
       switch (type){
           case 0://文件上传第一次请求
               int pkgid=data.getInt();
               int  fileType=data.get();//0 固件  1：音频
               processFileUpResponse(bsid,tagid,pkgid,0,fileType,1);
               break;
           case 1://文件上传结束
                fileid=data.getInt();
               processFileUpResponse(bsid,tagid,0,1,0,fileid);
              // kafukaSender.send(KafukaTopics.FIlE_RES,audioPara.toString());
               break;
       }



    }

  public   void processFileUpResponse(long bsid,long tagid,int pkgid,int type,int fileType,int finishFileId){

       switch (type){
           case 0:
               String url=null;
               String fileNameKafka=null;
               String fileNameBs=null;
               int fileid=(int)autoKey.getAutoId(null);
               SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
               String str = sdf.format(new Date());
               switch (fileType){
                   case 1:
                       fileNameBs="UP_TAGID_"+tagid+"_"+str+"\0";
                       fileNameKafka="UP_TAGID_"+tagid+"_"+str;
                       url=audioUrl+"\0";
                       break;
                   case 2://备份文件
                       fileNameBs="UP_BSID_"+bsid+"_"+str+"backup \0";
                       fileNameKafka="UP_BSID_"+bsid+"_"+str+"backup \0";
                       url=backUrl+"\0";
                       break;

               }
               byte[] fileUrlBytes=url.getBytes();
               byte[] fileNameBytes=fileNameBs.getBytes();
               int len=4+4+4+fileUrlBytes.length+fileNameBytes.length;
               ByteBuffer buffer= ByteBuffer.allocate(len);
               buffer.order(ByteOrder.LITTLE_ENDIAN);
               buffer.putInt((int)tagid);//tagid
               buffer.putInt((int)pkgid);//packid
               buffer.putInt((int)fileid);//fileid
               buffer.put(fileUrlBytes);//url
               buffer.put(fileNameBytes);//name
               sendData.sendDate(bsid,CMD_COAL_FILEUPLINK_ACK,buffer.array());
               if(fileType==1) {
                   FilePara audioPara = new FilePara();
                   audioPara.setMessageid(fileid);
                   audioPara.setState((short) -1);
                   audioPara.setDirection((short) 0);
                   audioPara.setProcess((short) 0);
                   audioPara.setTarget(tagid);
                   audioPara.setFileType((short)1);
                   audioPara.setBsid(bsid);
                   audioPara.setFileName(fileNameKafka);
                   kafukaSender.send(KafukaTopics.FIlE_RES, audioPara.toString());
               }
               break;
           case 1:
         /*      if(fileType==1) {
                   FilePara audioPara = new FilePara();
                   audioPara.setMessageid(finishFileId);
                   audioPara.setState((short) 0);
                   audioPara.setDirection((short) 0);
                   audioPara.setProcess((short) 0);
                   audioPara.setTarget(tagid);
                   audioPara.setFileType((short)1);
                   audioPara.setBsid(bsid);
                   audioPara.setFileName("");
                   kafukaSender.send(KafukaTopics.FIlE_RES, audioPara.toString());
               }*/
               ByteBuffer bufferfinish= ByteBuffer.allocate(8);
               bufferfinish.order(ByteOrder.LITTLE_ENDIAN);
               bufferfinish.putInt((int)tagid);
               bufferfinish.putInt(finishFileId);
               sendData.sendDate(bsid,CMD_COAL_FILEUPLINKFINISH_ACK,bufferfinish.array());
               break;
       }

    }


    /**
     * 基站功率配置
     * @param bsid
     * @param data
     */
    public void processBsPowerRes(Long bsid,ByteBuffer data){
        int type= data.getInt();//配置结果，0为读，1为写
        int gain= data.getInt();//功率
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setKeyOrder("power");
        bsPara.setType((short) type);
        bsPara.setPowerLevel((short)gain);
        Test.res=bsid+":gain"+gain;
        System.out.println(bsid + "基站返回当前power:"+gain);
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());

    }
    public void processBsRange(Long bsid,ByteBuffer data){
        BsRangeRes bsRangeRes=new BsRangeRes();
        bsRangeRes.setByteBuffer(data,0);
        if(bsRangeRes.error_code.get()==0){
            if(bsRangeRes.target_type.get()==0){
                long targetId=bsRangeRes.target_id.get();
                BsInf bsInf = mapContainer.bsInf.get(bsid+"");
                BsRangeInfo targetBsRangeInf = bsInf.bsrange.get(targetId);
                BigDecimal[] array={new BigDecimal(bsRangeRes.host_tx.get()),
                        new BigDecimal(bsRangeRes.client_rx.get()),
                        new BigDecimal(bsRangeRes.client_tx.get()),
                        new BigDecimal(bsRangeRes.host_tx.get())};
                double dis=Range.getDis(array,0,0);
                if(targetBsRangeInf==null){
                    targetBsRangeInf=new BsRangeInfo();
                    bsInf.bsrange.put(targetId+"",targetBsRangeInf);
                }
                boolean hasRes = targetBsRangeInf.addRangeDis(dis);
                if(hasRes){
                    BsRange bsRange=new BsRange();
                    bsRange.setSource(bsid);
                    bsRange.setType((short)bsRangeRes.target_type.get());
                    bsRange.setTarget(bsRangeRes.target_id.get());
                    bsRange.setDis((float) targetBsRangeInf.average);
                    kafukaSender.send(KafukaTopics.BS_RANGE_RES,bsRange.toString());
                }
            }

        }
    }

    public void processBsparaConfig(BsPara bsPara){

        String keyorder=bsPara.getKeyOrder();
        Long bsid=bsPara.getBsid();
        short type=bsPara.getType();
        System.out.println("配置基站参数:"+"keyorder:"+keyorder+"bsid:"+bsid);
        switch (keyorder){
            case "power":
                bsParaConfig.setBsPower(bsid,bsPara.getPowerLevel(),type);
                break;
            case "beep":
                bsParaConfig.setBsBeep(bsid,bsPara.getBeepState(),bsPara.getBeepInterval());
                break;
            case "locpara":
                break;
            case "general":
                bsParaConfig.setBsGeneral(bsid,bsPara.getGeneral());
                break;
            case "backgroundurl":
                if(bsid!=-1)
                bsParaConfig.setBsCompanyImg(bsid,bsPara.getBackgroundUrl(),null);
                else{
                    List<BsSyn> allInstanceBs = subService.findByAll(null,null,null, null, null, null, null, null,null,null,bsPara.getInstanceId());
                    for (BsSyn bsSyn :allInstanceBs) {
                        bsParaConfig.setBsCompanyImg(Long.valueOf(bsSyn.getNum()),bsPara.getBackgroundUrl(),null);
                    }
                }
                break;
            case "word":
                if(bsid!=-1)
                bsParaConfig.setBsCompanyText(bsid,bsPara.getWord());
                  else{
                    List<BsSyn> allInstanceBs = subService.findByAll(null,null,null, null, null, null, null, null,null,null,bsPara.getInstanceId());
                    for (BsSyn bsSyn :allInstanceBs) {
                    bsParaConfig.setBsCompanyText(Long.valueOf(bsSyn.getNum()),bsPara.getWord());
                }
            }
                break;
            case "warning":
                bsParaConfig.setBsWarning(bsid,bsPara);
                break;
            case "bsold":
                bsParaConfig.setBsOldTimeAndDis(bsid,bsPara.getOld_dis(),bsPara.getOld_time());
                break;
            case "errorcodetest":
                bsParaConfig.setRandomKey(bsid,bsPara);
                break;
            case "locationword":
                bsParaConfig.setBsLocationText(bsid,bsPara.getWord());
                break;
            case "bsslotinf":
                bsParaConfig.setTagModeViaBs(bsid,bsPara.getMode(),bsPara.getSuperFrame_interval(),bsPara.getSlot_duration(),bsPara.getBsrssi(),bsPara.getBsrange());
               break;
            case "versioninf":
                bsParaConfig.getBsVersionInf(bsid);
                break;
            case "net":
                bsParaConfig.setBsNetInf(bsid,bsPara.getAddress(),bsPara.getNetmask(),bsPara.getNetwork(),bsPara.getGateway(),bsPara.getIp_type());
                break;


                default:
                    break;
        }




    }
    public void processBsBackGround(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setKeyOrder("backgroundurl");
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }

    public void processBsOldTimeAndDis(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        bsPara.setKeyOrder("bsold");
        bsPara.setBsid(bsid);
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }
    public void processBsRelay(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        byte state=data.get();
        byte relayid=data.get();
        bsPara.setKeyOrder("warning");
        bsPara.setBsid(bsid);
        bsPara.setRelay_id(relayid);
        bsPara.setWarningState(state);
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }




    public void processBsErrorCodeTest(Long bsid,ByteBuffer data){
        long res=0;
        long first_res=0l;
        long error_res=0l;
        boolean same=true;
        long  tagcheckid=data.getLong();
      for(int i=1;i<16;i++) {
       res=data.getLong();
       if(i==1){
           first_res=res;
       }else if(first_res!=res){
           error_res=res;
           same=false;
       }

          logger.error(bsid + "误码测试返回值:" +tagcheckid+":time:"+ res);
      }
  /*      for (ErrorCodeInf error:mapContainer.testid_inf.values())
              {
                 // if(!error.stopFlag)
                  {
                      if(error.messageid==tagcheckid) {
                          BserrorcodetestEntity bsErrorEntity = bserrorcodetestDao.getByCode(res);
                          if(bsErrorEntity!=null&&same&&bsErrorEntity.getTagcheckid().longValue()==tagcheckid){
                              bsErrorEntity.setReceive(res);
                              bsErrorEntity.setPass(0);
                              bserrorcodetestDao.updateById(bsErrorEntity);
                          }else {
                              BserrorcodetestEntity recentBsErrorEntity = bserrorcodetestDao.getRecentSend(tagcheckid);
                              recentBsErrorEntity.setReceive(error_res);
                              recentBsErrorEntity.setPass(1);
                              bserrorcodetestDao.updateById(recentBsErrorEntity);
                          }

                        //  error.th.interrupt();
                          break;
                      }
                  }

        }*/

    }

    public void processBsWord(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        bsPara.setKeyOrder("word");
        bsPara.setBsid(bsid);
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }
    public void processBsLocationWord(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setKeyOrder("locationword");
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }

    public void processBsLocpara(Long bsid,ByteBuffer data){
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setKeyOrder("bsslotinf");
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }


    public void processBsWarning(Long bsid,ByteBuffer data){
        byte warning = data.get();
        byte relayid = data.get();
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setWarningState(warning);
        bsPara.setRelay_id(relayid);
        bsPara.setKeyOrder("warning");
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }




    public void processBsBeep(Long bsid,ByteBuffer data){
        int  isBeep = data.getInt();
        int  period = data.getInt();
        BsPara bsPara=new BsPara();
        bsPara.setBeepState((short)isBeep);
        bsPara.setBeepInterval(period);
        bsPara.setKeyOrder("beep");
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }

    public void processTagparaConfig(TagPara tagPara){

        String keyorder=tagPara.getKeyOrder();
        String tagid_string=tagPara.getTagid();
        long tagid;
        try {
            tagid= Integer.valueOf(tagid_string);
        }catch (Exception e){
            return;
        }
        long bsid=tagPara.getBsid();
        System.out.println("配置标签参数:"+"keyorder:"+keyorder+"tagid:"+tagid);
        switch (keyorder){
            case "id":
                tagParaConfig.setTagId(bsid,tagid,tagPara.getNewId());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
           case "beep":
               tagParaConfig.setTagBeep(bsid,tagid,tagPara.getBeepState(),tagPara.getBeepInterval());
                // bs beep set
                break;
            case "locpara":
                //bs locpara
                tagParaConfig.setTagLocaPara(bsid,tagid,tagPara.getLoc_inval(),tagPara.getRx_inval());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "power":
                //tag power set
                tagParaConfig.setTagPower(bsid,tagid,(byte) tagPara.getPa(),(byte)tagPara.getPowerLevel());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "reboot":
                tagParaConfig.setTagReboot(bsid,tagid,(byte) tagPara.getReboot());
                break;
            case "lowpower"://低功耗模式
                tagParaConfig.setTagLowPowerMode(bsid,tagid,tagPara.getLowPowerMode());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "sensorperiod"://传感器周期
                tagParaConfig.setTagSensorPeriod(bsid,tagid,tagPara.getSensorInterval());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "movelevel"://运动阈值
                tagParaConfig.setTagMoveLevel(bsid,tagid,tagPara.getMoveLevel());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "heartperiod"://心跳周期
                tagParaConfig.setTagHeartPeriod(tagid,bsid,tagPara.getHeartInterval());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                tagParaConfig.setTagReboot(bsid,tagid,(byte)0x01);
                break;
            case "update":
                tagPara.setTime(new Date().getTime());
                new Thread(new TagUpdateCheckRun(tagPara)).start();

                break;
            case "mode":
                logger.error("mode"+bsid+"::"+tagid);
                tagParaConfig.setTagMode(bsid,tagid,(byte)tagPara.getMode(),(byte)tagPara.getPeriod());
                break;
            case "versioninf":
                tagParaConfig.getTagVersionInf(bsid,tagid);
                break;
            case "groupbslist":
                tagParaConfig.setTagGroupBslist(bsid,tagid,Integer.valueOf(tagPara.getType()).byteValue(),tagPara.getGroupbslist());
                break;
            case "grouprangetime":
                tagParaConfig.setTagGroupPeriod(bsid,tagid,Integer.valueOf(tagPara.getType()).byteValue(),tagPara.getGrouprangetime());
                break;

            default:
                break;
        }




    }

  public   void tagCheck(long tagid,long bsid){
       /* List<TagcheckbsidEntity> notEndList= tagcheckbsidDao.getNotEnd((int)tagid,(int) bsid);
        int m=0;
        for (TagcheckbsidEntity notEnd:notEndList
        ) {*/
          if(bsid==820)
            {
                if (mapContainer.tagCapacities.containsKey(tagid+"")) {
                    HashSet<String> set=new LinkedHashSet<>();
                  /*  TagcheckEntity tagcheck = tagcheckDao.selectById(notEnd.getTagcheckid().intValue());
                    String[] totalTaglist = tagcheck.getTaglist().split(",");*/
                    Iterator<Map.Entry<String, Boolean>> iter = mapContainer.tagCapacities.entrySet().iterator();
                    while (iter.hasNext()){
                        Map.Entry entry = (Map.Entry) iter.next();
                        String tagidd=(String) entry.getKey();
                        set.add(tagidd);
                    }

                    ArrayList<String> arrayList = new ArrayList<String>(80);
                  /*  int[] list={100,
                            101,
                            109,
                            115,
                            117,
                            123,
                            130,
                            131,
                            134,
                            137,
                            139,
                            149,
                            153,
                            157,
                            160,
                            164,
                            171,
                            173,
                            176,
                            178

                    };*/
                  //  Collections.addAll(arrayList, Arrays.copyOfRange(totalTaglist, 1, totalTaglist.length));
                    for(int i=1;i<80;i++){
                        arrayList.add(i+"");
                    }
                    HashSet<String> allset = new LinkedHashSet<>(arrayList);
                    allset.removeAll(set);

                    String lack = ",";
                    for (String ee : allset
                    ) {
                        lack = lack + ee + ",";
                    }
                    logger.error(new Timestamp(new Date().getTime()).toString()+"缺的标签号：" + lack);
                    //System.out.println(new Timestamp(new Date().getTime()).toString()+"缺的标签号：" + lack);
                    mapContainer.tagCapacities.clear();
                } else {
                    mapContainer.tagCapacities.put(tagid+"", true);
                }
            }
           // m++;
      //  }
    }

   public double[]	getWeightRes(double[] former,double[] current){
        int len=former.length;
        int weight=10;
        double[] res=new double[len];
        for(int i=0;i<len;i++){
            res[i]=(weight*former[i]+current[i])/(weight+1);
        }
        return res;

    }

    @Override
    public DisSort chooseTwoBsFromMutipleBs(ArrayList<Double[]> bsPos, ArrayList<Double> Dis,boolean onTwoBs) {
        int size=bsPos.size();
        DisSort res=null;
        double percent=1000;
        for (int i=0;i<size;i++){
            for (int j=i;j<size-1;j++) {
                Double[] bs1 = bsPos.get(i);
                Double[] bs2 = bsPos.get(j+1);
                Double dis1 = Dis.get(i);
                Double dis2 = Dis.get(j+1);
             //   System.out.println("i:"+bs1[3]+":dis:"+dis1+"j:"+bs2[3]+":dis:"+dis2);
                double twbDis = PercentToPosition.getDis(bs1, bs2);
             System.out.println(bs1[3]+":dis:"+dis1+"::::"+bs2[3]+":dis:"+dis2+"bsdis"+twbDis);
                if ((bs1[0].doubleValue() == bs2[0].doubleValue() || bs1[1].doubleValue() == bs2[1].doubleValue()) && bs1[2].doubleValue() == bs2[2].doubleValue() /*&& twbDis > mapContainer.channeldis*/) {
                    double vertical = Hilen.getVerticalDis(dis1, dis2, twbDis);
                    double percentbs;
                    if (Double.isNaN(vertical))
                        percentbs = 0;
                    else
                        percentbs = vertical / twbDis;
                    //System.out.println("vertical:"+bs1[3]+":dis:"+dis1+"------"+bs2[3]+":dis:"+dis2+"percentbs"+percentbs);
                    if ((onTwoBs||percentbs < mapContainer.channelpercent) && (percentbs < percent)) {
                        percent = percentbs;
                        //  res.setBsname(Double.valueOf(bs1[3]).longValue() + ":" + Double.valueOf(bs2[3]).longValue());

                        Double[][] bspos = {{0d, 0d, 0d}, {twbDis, 0d, 0d}};
                        Double[] near_dis = {dis1, dis2};
                        double[][] pos = Hilen.location1D(near_dis, bspos);
                        double percentHorizon = pos[0][0] / twbDis;
                        logger.info("use 2::" + percentHorizon);
                        if (Math.abs(percentHorizon) < 1.3) {
                            double[] res1D = PercentToPosition.percentToPosition(bs1, bs2, percentHorizon);
                            res = new DisSort(res1D[0], res1D[1], res1D[2], Double.valueOf(bs1[3]).longValue() + ":" + Double.valueOf(bs2[3]).longValue(), 0d);
                            res.setR(0f);
                            res.setBsname(Double.valueOf(bs1[3]).longValue() + ":" + Double.valueOf(bs2[3]).longValue());
                        }
                    }
                }
            }
        }

        return res;
    }




}
