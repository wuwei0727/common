package com.tgy.rtls.location.dataprocess;

import com.tgy.rtls.data.algorithm.Hilen;
import com.tgy.rtls.data.algorithm.Location_highway;
import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.location.TdoaData;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.impl.BsConfigServiceImpl;
import com.tgy.rtls.location.model.*;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.tdoa.BsCoef;
import com.tgy.rtls.location.tdoa.BsTimestamp;
import com.tgy.rtls.location.tdoa.BsTimestamps;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

@Component
public class TdoaInterfaceImp implements TdoaInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void tdoaDataProcess(TagInf tagInf) {

    }

    @Override
    public void storageTimestamp(BsTimestamps bsTimestamps, BsTimestamp bsTimestamp) {
        String bsid=bsTimestamp.bid;
        MapContainer mapContainer = SpringContextHolder.getBean(MapContainer.class);
        BsInf bsinf = mapContainer.bsInf.get(bsid);
        if(bsinf==null||bsinf.beacon==null)
        {
            //System.out.println(bsTimestamp.bid+": null" );
           return;
        }

        bsTimestamp.sync_tx0=new BigDecimal(bsinf.beacon.beacon_txTs.get());
        bsTimestamp.sync_rx0=new BigDecimal(bsinf.beacon.beacon_rxTs.get());
        bsTimestamp.sync_id0=bsinf.beacon.beacon_id.get();
        if(bsinf.beacon.beacon_rxTs.get()==0){//该基站为主基站，将该基站的beacon信息同步到tdoa数据里
            bsTimestamps.mbs=bsinf.beacon.beacon_src.get()+"";
            bsTimestamps.synid=bsinf.beacon.beacon_id.get();
        }
        //   logger.error(bsTimestamps.rangeid+" :bsTimestamps put:"+bsTimestamp.bid+":timestamp");
            bsTimestamps.bsinf.put(bsTimestamp.bid,bsTimestamp);
         //  logger.error("bsTimestamps.bsinf:size:"+bsTimestamps.bsinf.size());

    }

    @Override
    public void tdoaLocation(TagInf tagInf) {
        if(tagInf.getTdoaTimestamp().size()>=4)
            {
            //获取id最小的一组时间戳进行定位
            Set<Map.Entry<Long, BsTimestamps>> set = tagInf.getTdoaTimestamp().entrySet();
            List<BsTimestamps> sortId=new ArrayList<>();
                for (Map.Entry entry:set
                     ) {
                    BsTimestamps bsTimestamps=(BsTimestamps)  entry.getValue();
                    sortId.add(bsTimestamps);
                }
                Collections.sort(sortId);
            BsTimestamps bsTimestamps = sortId.get(0);
           // logger.error(tagInf.tagId+":rangeid:"+sortId.get(0).rangeid+":bsTimestamps.bsinf.size():"+bsTimestamps.bsinf.size()+":bsTimestamps.mbs.length():"+bsTimestamps.mbs.length());
            if (bsTimestamps!=null&&bsTimestamps.bsinf.size() >= 2&&bsTimestamps.mbs.length()>0) {//包含主基站，基站数量大于3，可以进行定位
                //
                int bsSize = 0;
                Set<Map.Entry<String, BsTimestamp>> bsTimestampsData = bsTimestamps.bsinf.entrySet();
                for (Map.Entry entry:bsTimestampsData
                ) {
                    String bsid = (String)entry.getKey();
                    BsTimestamp bsTimestamp = (BsTimestamp)entry.getValue();
                    if(bsTimestamp.sync_id0!=bsTimestamps.synid){
                        continue;
                    }

                //    logger.error("bsTimestamps.synid:"+bsTimestamps.synid);
                    //logger.error("bsTimestamp.sync_id0:"+bsTimestamp.sync_id0);

                    BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
                    BsConfig bsInf = bsConfigService.findByNum(bsid);
                    if (bsInf != null)
                        bsSize++;
                }

                double[][] bsPos = new double[bsSize][3];
                BigDecimal[][] bsT_t = new BigDecimal[bsSize][2];
                int[] state = new int[bsSize];
                double zum = 0.3;
                double antennadelay = 77.81;
                double[] corr = new double[bsSize];
                double[] coefs = new double[bsSize];
                int i = 1;
                int j = 0;
                String[] names=new String[bsSize];
                MapContainer mapContainer = SpringContextHolder.getBean(MapContainer.class);
                BsInf bsinf_coef = mapContainer.bsInf.get(bsTimestamps.mbs);
                for (Map.Entry entry:bsTimestampsData){
                    String bsid = (String)entry.getKey();
                    BsTimestamp bsTimestamp = (BsTimestamp)entry.getValue();
                    BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
                    BsConfig bsInf = bsConfigService.findByNum(bsid);
                    if (bsInf == null||bsTimestamp.sync_id0!=bsTimestamps.synid)
                        continue;
                    BsCoef bs_coef = bsinf_coef.bsCoef.get(bsid);
                    if (bsid.equals(bsTimestamps.mbs)) {
                        bsPos[0][0] = bsInf.getX();
                        bsPos[0][1] = bsInf.getY();
                        bsPos[0][2] = bsInf.getZ();
                        bsT_t[0][0] = bsTimestamp.sync_tx0;
                        bsT_t[0][1] = bsTimestamp.ping_ts;
                        if ((bsT_t[0][1].doubleValue() -bsT_t[0][0].doubleValue())<0)
                            state[0] = 0;
                        else
                            state[0] = 1;
                        names[0]=bsid;
                 //  if(bs_coef==null)
                            coefs[0]=0.00469176397861579;
                    /*  else
                           coefs[0]=bsinf_coef.bsCoef.get(bsid).coef;*/
                    } else {
                        bsPos[i][0] = bsInf.getX();
                        bsPos[i][1] = bsInf.getY();
                        bsPos[i][2] = bsInf.getZ();
                        bsT_t[i][0] = bsTimestamp.sync_rx0;
                        bsT_t[i][1] = bsTimestamp.ping_ts;
                        if ((bsT_t[i][1].doubleValue() -bsT_t[i][0].doubleValue())<0)
                            state[i] = 0;
                        else
                            state[i] = 1;
                        names[i]=bsid;
                  //   if(bs_coef==null)
                            coefs[i]=0.00469176397861579;
                      /*  else
                          coefs[i]=bsinf_coef.bsCoef.get(bsid).coef;*/
                        i++;
                    }
                    corr[j] = 0;
                    j++;
                }

                tdoaDataFilter(tagInf,names,bsT_t,state);
                double[] res = null;
                if (!bsTimestamps.mbs.trim().isEmpty()&&bsSize>=2) {

                    for(int k=0;k<bsSize;k++) {
             //           logger.error(tagInf.tagId+":tdoaData:"+bsTimestamps.rangeid +":bsid:"+names[k]+/* ":x:"+bsPos[k][0]+"y:"+bsPos[k][1]+"z:"+bsPos[k][2]+*/":T:"+bsT_t[k][0]+":t:"+bsT_t[k][1]+":coef:"+coefs[k]);
                     /*   if(k==1){
                            logger.error(tagInf.tagId+":tdoaData:"+bsTimestamps.rangeid +":bs:"+names[0]+"-"+names[1]+":diff:"+(bsT_t[1][1]-bsT_t[1][0]-(bsT_t[0][1]-bsT_t[0][0]))*//*+"coef"+coef[k]*//*);
                        }*/
                    }
               //     logger.error(tagInf.tagId+":tdoaData:"+bsTimestamps.rangeid +":bsid:"+":T:"+(bsT_t[0][0]-bsT_t[1][0]));
                //    logger.error(tagInf.tagId+":tdoaData:"+bsTimestamps.rangeid +":bsid:"+":t:"+(bsT_t[0][1]-bsT_t[1][1]));
               //     logger.error(tagInf.tagId+":tdoaData:"+bsTimestamps.rangeid +":bsid:"+":T-t:"+(bsT_t[0][0]-bsT_t[1][0]-bsT_t[0][1]+bsT_t[1][1]));


                    if(bsSize==2){
                        //一维定位
                       System.out.println(names[0]+"-"+names[1]);
                        res = Location_highway.location(bsT_t,names,0, bsPos, antennadelay, coefs, state, zum, corr);
                         if(res!=null) {
                             tagInf.jsonArray.clear();
                             //  logger.error(tagInf.tagId+":master:"+bsTimestamps.mbs+":diff:"+res[0]);
                             double dis_two_bs = PercentToPosition.getDis(bsPos[0], bsPos[1]);
                             Double[][] bspos = {{0d, 0d, 0d}, {dis_two_bs, 0d, 0d}};
                             Double[] near_dis = {(dis_two_bs - res[0]) / 2, (dis_two_bs + res[0]) / 2};
                             JSONObject jsonObject_0 = new JSONObject();
                             JSONObject jsonObject_1 = new JSONObject();
                             jsonObject_0.put("originalDis", (float) Math.round((dis_two_bs - res[0]) / 2 * 100) / 100);
                             jsonObject_0.put("filterDis", (float) Math.round((dis_two_bs - res[0]) / 2 * 100) / 100);
                             jsonObject_0.put("bsid", names[0] + "-0");
                             jsonObject_1.put("originalDis", (float) Math.round((dis_two_bs + res[0]) / 2 * 100) / 100);
                             jsonObject_1.put("filterDis", (float) Math.round((dis_two_bs + res[0]) / 2 * 100) / 100);
                             jsonObject_1.put("bsid", names[1] + "-0");
                             tagInf.jsonArray.add(jsonObject_0);
                             tagInf.jsonArray.add(jsonObject_1);
                             double[][] pos = Hilen.location1D(near_dis, bspos);
                             double percent = pos[0][0] / dis_two_bs;
                             if (Math.abs(percent) < 1.3)
                                 res = PercentToPosition.percentToPosition(bsPos[0], bsPos[1], percent);
                             else
                                 res = null;
                         }
                    }else {
                        //二维定位
                       res = Location_highway.location(bsT_t, names,0, bsPos, antennadelay, coefs, state, zum, corr);
                    }
                }
                if (res != null) {
                    List<String> strsToList1= Arrays.asList(names);
                    Collections.sort(strsToList1);
                    String nameList = "";
                    for (String name : strsToList1
                    ) {
                        nameList = nameList + ":" + name;
                    }
               //     logger.error(tagInf.tagId+":master:"+bsTimestamps.mbs+":rangeid:"+bsTimestamps.rangeid+":::res:x:"+res[0]+":y:"+res[1]);
                    LocFiterRes fin_res = new LocFiterRes(nameList, (float) res[0], (float) res[1], (float) res[2], (short) 2);
                    BsConfigService bsConfigService = SpringContextHolder.getBean(BsConfigServiceImpl.class);
                    BsConfig bsInf = bsConfigService.findByNum(strsToList1.get(0));
                    if(bsInf!=null)
                    fin_res.floor=bsInf.getFloor();
                    tagInf.bsid=Long.valueOf(strsToList1.get(0));
                    tagInf.setRegion(fin_res,mapContainer.timedelay_highfreq,mapContainer.locationcachelen_highfreq);
                }

            }
                tagInf.getTdoaTimestamp().remove(bsTimestamps.rangeid,bsTimestamps);

        }
       // logger.error(tagInf.tagId+":tagInf.getTdoaTimestamp():"+tagInf.getTdoaTimestamp().size());

    }

    public static void main(String[] args) {
        BigDecimal[][] bsT_t = new BigDecimal[2][2];
  /*      bsT_t[0][0]=new BigDecimal(440662925312l);
        bsT_t[0][1]=new BigDecimal(445898780878l);
        bsT_t[1][0]=new BigDecimal(1090357808850l);//0,0
        bsT_t[1][1]=new BigDecimal(1095593629216l);*/


        bsT_t[0][0]=new BigDecimal(383349108224l);
        bsT_t[0][1]=new BigDecimal(388548598053l);
        bsT_t[1][0]=new BigDecimal(1033035052098l);   //48,0
        bsT_t[1][1]=new BigDecimal(1038234485702l);
        double[][] bsPos = new double[2][3];
        bsPos[0][0]=0d;
        bsPos[0][1]=0d;
        bsPos[0][2]=0d;
        bsPos[1][0]=50d;
        bsPos[1][1]=0d;
        bsPos[1][2]=0d;
        double[] coefs = {0.00469176397861579,0.00469176397861579};
        int[] state = {1,1};
        double zum = 0.3;
        double antennadelay = 82.5;
        double[] corr = {0,0};
        double[] res = Location_highway.location(bsT_t, null, 0, bsPos, 33, coefs, state, zum, corr);
        double[] res1 = Location_highway.location(bsT_t, null, 0, bsPos, 82.5, coefs, state, zum, corr);
    }

    /**
     * 过滤距离差数据
     * @param tagInf  标签
     * @param bsnames  基站列表
     * @param bsT_t   时间戳
     * @param state   是否采用该数据
     */
    public void tdoaDataFilter(TagInf tagInf,String[] bsnames, BigDecimal[][] bsT_t , int[] state){
        int len=bsnames.length;
       
        for (int i = 1; i<len; i++){
            Bslr_dis rangeInf1=tagInf.range_bslr_dis.get(bsnames[0]+"-"+bsnames[i]);
            if (rangeInf1 == null) {
                rangeInf1 = new Bslr_dis();
                tagInf.range_bslr_dis.put(bsnames[0]+"-"+bsnames[i], rangeInf1);
            }
            double ticDiff = bsT_t[i][1].doubleValue() - bsT_t[i][0].doubleValue() - bsT_t[0][1].doubleValue() + bsT_t[0][0].doubleValue();
            MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
            LocationMapper locationMapper= SpringContextHolder.getBean("locationMapper");
            TdoaData tdoaData=new TdoaData();
            tdoaData.setTagid(tagInf.tagId);
            tdoaData.setSrc(bsnames[0]);
            tdoaData.setTarget(bsnames[i]);
            tdoaData.setSrc_tx(bsT_t[0][0].toPlainString());
            tdoaData.setTarget_rx(bsT_t[i][0].toPlainString());
            tdoaData.setSrc_t(bsT_t[0][1].toPlainString());
            tdoaData.setTarget_t(bsT_t[i][1].toPlainString());
            tdoaData.setX(mapContainer.d1);
            tdoaData.setY(mapContainer.d2);
            tdoaData.setDiff((float)(ticDiff*0.00469176397861579));
            locationMapper.addTdoaData(tdoaData);
            Float[] res = rangeInf1.addDis(new DisInf((float) ticDiff, 0f), 1,mapContainer.timedelay_highfreq,mapContainer.discachelen_highfreq);
            if(res!=null) {
                logger.error(bsnames[0] + "-" + bsnames[i] + "diff:" + ticDiff + "filter diff:" + res[0]);
            }
            if(res!=null){
                if((res[0]-ticDiff)>10000){
                    state[i]=0;
                }
            }else {
                state[i]=1;
            }
            
        }
       
    }
}
