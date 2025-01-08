package com.tgy.rtls.location.netty;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.Utils.Constant;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfigImp;
import com.tgy.rtls.location.config.deviceconfig.ScreenConfig;
import com.tgy.rtls.location.config.deviceconfig.ScreenConfigImp;
import com.tgy.rtls.location.dataprocess.TdoaInterfaceImp;
import com.tgy.rtls.location.model.Cmd;
import com.tgy.rtls.location.model.Header;
import com.tgy.rtls.location.model.Message;
import com.tgy.rtls.location.model.TagInf;
import com.tgy.rtls.location.tdoa.BsTimestamp;
import com.tgy.rtls.location.tdoa.BsTimestamps;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;

public class MessageReadTask /*extends Thread*/ {

/*    private MapContainer mapContainer = SpringContextHolder.getBean("mapContainer");
    private BsConfigService bsConfigService = SpringContextHolder.getBean("bsConfigService");;*/



    private Message message;
    private Channel channel;
    MessageReadTask(){

    }
    public static void  MessageReadTask1(Message message, Channel channel) {
        //this.message = message;
      //  this.channel=channel;
 /* }

    @Override
    public void run() {*/
         SendData sendData = SpringContextHolder.getBean("sendData");;
         DataProcess dataProcess = SpringContextHolder.getBean("dataProcess");;
         Logger logger = LoggerFactory.getLogger(MessageReadTask.class);
         BsParaConfig bsParaConfig=SpringContextHolder.getBean(BsParaConfigImp.class);
         ScreenConfig ScreenConfig=SpringContextHolder.getBean(ScreenConfigImp.class);
         TdoaInterfaceImp tdoaInterfaceImp=SpringContextHolder.getBean(TdoaInterfaceImp.class);
         MapContainer mapContainer=SpringContextHolder.getBean(MapContainer.class);
        if (message == null)
            return;
        Header header = message.getHeader();
        if (header == null)
            return;
        long bsid = message.getBsId();
        byte[] cmd=header.getCmd();
        String cmd_type= new Cmd(cmd).getCmd();
        byte[] data = message.getData();
        if (data == null)
            return;
        long len= ByteUtils.byte2short(header.getLength());
       // if(cmd_type.equals(Constant.CMD_COAL))
       // logger.error(bsid+"beacon:"+ByteUtils.printHexString(data));;
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
     // System.out.println(bsid+"CMD"+cmd_type);
     //  logger.error(bsid+"::cmd"+cmd_type);
        try{
       switch (cmd_type) {

           case Constant.COAL_HEART://  心跳数据包
               // System.out.println(bsid+"心跳包");
               dataProcess.processCoalHeartData(bsid, buf);
               break;
           case Constant.COAL_C3://  定位数据包

               break;
           case Constant.COAL_BSERROR_UPLINK://  基站异常上报
               dataProcess.processBsError(bsid, buf);
               break;

           case Constant.COAL_FILEPUSH_REQ_PROCESS://文件传输进度
              // System.out.println(bsid + "文件推送进度");
               dataProcess.processFilePushStatus(bsid, buf, 0);
               break;
           case Constant.COAL_FILEPUSH_END://文件下传结束
               System.out.println(bsid + "文件推送结束");
               dataProcess.processFilePushStatus(bsid, buf, 1);
               break;
           case Constant.COAL_FILEUPLINK_REQ://文件上传请求
               try {

                   dataProcess.processFileUpload(bsid, buf, 0);
               } catch (Exception e) {

               }
               break;
           case Constant.COAL_FILEUPLINK_END://文件上传结束
               System.out.println(bsid + "文件上传结束");
               dataProcess.processFileUpload(bsid, buf, 1);
               break;
           case Constant.COAL_SINGLE_RANGE://单基站定位
            //   logger.error(bsid + "基站单站测距");
            // String ss=  ByteUtils.printHexString(data);
              // System.out.println("singgle"+ss);
             try {
                 dataProcess.processSingleBsRange(bsid, buf);
               } catch (Exception e) {
                   e.printStackTrace();
               }
               break;
           case Constant.TAGDATA_UPLINK:
               try {
                  int tagid=buf.getInt();
                  int messageid=buf.getShort();
                  short sensorLen=buf.getShort();
                 //  System.out.println("TAGDATA_UPLINK");
                dataProcess.processTagDataUplink(bsid,tagid,buf,sensorLen);
               } catch (Exception e) {
                //   logger.error(e.getMessage()+e.getStackTrace());
               }
               break;
           case Constant.COAL_TAG_SENSOR:
                   int tagid=buf.getInt();
                   int rangeid=buf.getInt();
                   long pollRx=buf.getLong();
                   float	rssi=buf.getFloat();			//信号强度
                   float 	fp=buf.getFloat();			//首达径信号强度
                   short sensorLen=buf.getShort();			//标签上行数据长度
                  // System.out.println("COAL");
                   dataProcess.processTagDataUplink(bsid,tagid,buf,sensorLen);

               TagInf taginf = mapContainer.tagInf.get(tagid + "");
               if(taginf==null){
                   taginf=new TagInf((long)tagid+"");
                   mapContainer.tagInf.put((long)tagid+"",taginf);
               }

               synchronized (taginf){
                   BsTimestamps bsTimestamps = taginf.getTdoaTimestamp().get((long)rangeid);
                   if(bsTimestamps==null){
                       bsTimestamps=new BsTimestamps();
                       taginf.getTdoaTimestamp().put((long)rangeid,bsTimestamps);
                       bsTimestamps.rangeid=(long )rangeid;
                       bsTimestamps.time=new Date().getTime();
                   }

                   BsTimestamp bsTimestamp = new BsTimestamp();
                   bsTimestamp.bid = bsid + "";
                   bsTimestamp.ping_ts =new BigDecimal( pollRx);

                  tdoaInterfaceImp.storageTimestamp(bsTimestamps, bsTimestamp);
                   logger.error(tagid + ":ping up ss bsid:" + bsid + ":rangeid:" + rangeid + ":pingts:" + pollRx);
                  tdoaInterfaceImp.tdoaLocation(taginf);
               }

               break;
           case Constant.COAL_BSRANGE_RES:
               System.out.println(bsid + "基站测距");
               dataProcess.processBsRange(bsid, buf);
               break;
           case Constant.BS_POWER_RES:

               dataProcess.processBsPowerRes(bsid, buf);
               break;
           case Constant.BS_BEEP_RES:
               System.out.println(bsid + "基站beep");
               dataProcess.processBsBeep(bsid, buf);
               break;
           case Constant.COAL_BS_WARNING_RES:
               System.out.println(bsid + "基站报警配置");
               dataProcess.processBsWarning(bsid, buf);
               break;
           case Constant.COAL_BSTEXT_RES:
               System.out.println(bsid + "基站文字配置");
               dataProcess.processBsWord(bsid, buf);
               break;
           case Constant.COAL_BSIMG_RES:
               System.out.println(bsid + "基站图片");
               dataProcess.processBsBackGround(bsid, buf);
               break;
           case Constant.COAL_BSOLD_RES://配置基站老化时间
               System.out.println(bsid + "基站参数设置老化结果");
               dataProcess.processBsOldTimeAndDis(bsid, buf);
               break;
           case Constant.COAL_BSERRORCODETEST_RES://基站误码率测试
               dataProcess.processBsErrorCodeTest(bsid, buf);
               break;
           case Constant.COAL_BSLOCATION_RES://配置基站位置显示文字响应
               dataProcess.processBsLocationWord(bsid, buf);
               break;
           case Constant.CMD_BS_LOCPARA_RES://配置基站定位参数响应
               dataProcess.processBsLocpara(bsid, buf);
               break;
           case Constant.DONW_ACK:
               System.out.println(bsid + "标签配置确认ACK");
               break;
           case Constant.CMD_BS_UPDATE_RES:
               logger.info(bsid+"基站升级");
               bsParaConfig.getBsUpdate(bsid,buf);
               break;
           case Constant.CMD_4BS_2D:
               try {
                   dataProcess.process2D_4bs(bsid, buf);
               } catch (Exception e) {
                   e.printStackTrace();
               }
               break;
           case Constant.CMD_8BS_2D:
               try {
                    dataProcess.process2D_8bs(bsid, buf);
               } catch (Exception e) {
                   e.printStackTrace();
               }
               break;
      /*     case Constant.COAL_BS_WARNING_RES:
               dataProcess.processBsRelay(bsid, buf);
               break;*/

           case Constant.CMD_BS_VERSIONINFRES:
              byte[] version= buf.array();
              String ver=new String(version, "UTF8");
               System.out.println(bsid + "基站版本号"+ver);
               bsParaConfig.getBsVersion(bsid,ver);
               bsParaConfig.sendScreenCache(bsid);
               break;
           case Constant.CMD_BS_NETRES:
               bsParaConfig.getBsNetConfigRes(bsid,buf.get());
               break;
           case Constant.CMD_BS_TAGDIS_RES:
              bsParaConfig.getBsTagDisRes(bsid,buf);
               break;
         /*  case Constant.COAL_BEACON:
               bsParaConfig.getBsBeacon(bsid,buf);
               break;*/
           case Constant.CMD_COAL:
               bsParaConfig.getBsBeacon(bsid,buf);
               break;
           case Constant.CMD_PARK:
               bsParaConfig.processInfraredPark( bsid, buf,Constant.CMD_PARK_STATE,header.getDst(),len);
               break;
           case Constant.CMD_LORAHEART://心跳数据
               bsParaConfig.processGateway_lora( bsid, buf,Constant.CMD_PARK_STATE,header.getDst());
               break;
           case Constant.CMD_LORA_VERSION:
               bsParaConfig.processGateway_lora_version( bsid, data,Constant.CMD_PARK_STATE,header.getDst());
               break;
           case Constant.CMD_INFRARED_VERSION:
               bsParaConfig.processInfrared_version( bsid, data,buf,Constant.CMD_PARK_STATE,header.getDst());
               break;
           case Constant.CMD_INFRAREDSTATE:
               bsParaConfig.processInfrared_state( bsid, buf);
               break;
           case Constant.CMD_GATESTATE:
               bsParaConfig.processGate_state( bsid,buf);
               break;
           case Constant.CMD_4G_485:
               ScreenConfig.process4G_485Heart( bsid,5);
               break;
           case Constant.CMD_NED:
               logger.error(bsid+":ned data:"+ByteUtils.printHexString(data));;
               bsParaConfig.processNed_data( bsid, buf,header.getDst());
               break;
           case Constant.CMD_NED_HEART:
               logger.error(bsid+":CMD_NED_HEART:"+ByteUtils.printHexString(data));;
               bsParaConfig.processCat1( bsid, buf,Constant.CMD_CAT1_STATE,header.getDst());
               break;
           case Constant.CMD_NED_DATA:
               logger.error(bsid+":CMD_NED_DATA:"+ByteUtils.printHexString(data));;
               bsParaConfig.processNed_data( bsid, buf,header.getDst());
               break;
           case Constant.CMD_NED_DEC_DATA:
               logger.error(bsid+":CMD_NED_DEC_DATA:"+ByteUtils.printHexString(data));;
               bsParaConfig.processInfraredPark( bsid, buf,Constant.CMD_CAT1_STATE,header.getDst(),len);
               break;

           default:

               break;

        }
        }catch(Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
     /*   byte[] data={0x00,0x01,0x02,0x03};
        ByteBuffer buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.get();
        buf.get();
        byte[] left=buf.array();
        int pos=buf.position();
       int offset= buf.arrayOffset();
        byte[] dstdata=new byte[2];
         buf.get(dstdata, 0, 2);*/
  int dis=distance(160,60);
        System.out.println(dis);

    }
    public static int distance(int alpha, int beta) {
        int phi = Math.abs(beta - alpha) % 360;       // This is either the distance or 360 - distance
        int distance = phi > 180 ? 360 - phi : phi;
        return distance;
    }


}