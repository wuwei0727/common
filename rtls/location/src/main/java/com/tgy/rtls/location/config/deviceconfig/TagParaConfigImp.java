package com.tgy.rtls.location.config.deviceconfig;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.data.service.update.TagFirmwareService;
import com.tgy.rtls.data.snowflake.AutoKey;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.TagInf;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.netty.SendData;
import com.tgy.rtls.location.struct.TagFirmware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

import static com.tgy.rtls.location.Utils.Constant.CMD_TAG_PARA;

@Component
public class TagParaConfigImp implements TagParaConfig {
    @Autowired
    SendData  sendData;

    public  static int TagParaHeadLen=10;
    @Autowired
    MapContainer mapContainer;
    @Autowired(required = false)
    TagFirmwareService tagFirmwareService;
    @Autowired(required = false)
    KafukaSender kafukaSender;
    @Autowired
    AutoKey autoKey;
    short updateid;
    @Autowired
    TagParaConfigImp tagParaConfig;


    /**
     *
     * @param bsid
     * @param tagid
     * @param newId
     * @return
     */
    public boolean setTagId(Long bsid,long tagid,long newId){
        bsid= getBsid(bsid,tagid);
        int size=6;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x01);//id配置
        buffer.putInt((int)newId);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param tagid
     * @param beepState
     * @param period  间隔ms
     * @return
     */
    public boolean setTagBeep(Long bsid,long tagid,int beepState,int period){
        bsid= getBsid(bsid,tagid);
        int size=5;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x02);//beep
        buffer.put((byte)beepState);
        buffer.putShort((short) period);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param tagid
     * @param loc_inval   定位间隔 ms
     * @param rx_inval     窗口开启时间ms
     * @return
     */
    public   boolean setTagLocaPara(Long bsid,long tagid,int loc_inval,int rx_inval){
        bsid= getBsid(bsid,tagid);
        int size=10;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x05);//定位参数
        buffer.putInt(loc_inval);
        buffer.putInt(rx_inval);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());

    }
    /**
     *
     * @param bsid
     * @param tagid
     * @param pa
     * @param gain  0~33
     * @return
     */
   public boolean setTagPower(Long bsid,long tagid,byte pa,byte gain){
       bsid= getBsid(bsid,tagid);
        int size=4;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x06);//功率配置
        buffer.put(pa);
        buffer.put(gain);
       return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }


    /**
     *
     * @param bsid
     * @param tagid
     * @param mode   1：测试模式  0：正常模式
     * @param period  单位10分钟
     * @return
     */
    public boolean setTagMode(Long bsid,long tagid,byte mode,byte period){
        bsid= getBsid(bsid,tagid);
        int size=4;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x12);//功率配置
        buffer.put(mode);
        buffer.put(period);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    public  boolean setTagReboot(Long bsid,long tagid,byte reboot){
        bsid= getBsid(bsid,tagid);
        int size=3;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x07);//重启
        buffer.put(reboot);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param tagid
     * @param state   0:关闭低功耗模式  1：开启低功耗模式
     * @return
     */
    public   boolean setTagLowPowerMode(Long bsid,long tagid,int state){
        bsid= getBsid(bsid,tagid);
        int size=3;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x08);//
        buffer.put((byte)state);//低功耗模式
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());

    }

    /**
     *
     * @param bsid
     * @param tagid
     * @param period   单位ms
     * @return
     */
    public   boolean setTagSensorPeriod(Long bsid,long tagid,int period){
        bsid= getBsid(bsid,tagid);
        int size=6;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x09);//
        buffer.putInt(period);//周期
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());

    }


    /**
     *
     * @param bsid
     * @param tagid
     * @param moveLevel   4<= x <=1020
     * 默认值：500
     * @return
     */
    public   boolean setTagMoveLevel(Long bsid,long tagid,int moveLevel){
        bsid= getBsid(bsid,tagid);
        int size=4;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x10);//
        buffer.putShort((short) moveLevel);//
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());

    }

    /**
     *
     * @param bsid
     * @param tagid
     * @param period  ms
     * @return
     */
    public   boolean setTagHeartPeriod(Long bsid,long tagid,int period){
        bsid= getBsid(bsid,tagid);
        int size=6;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x11);//定位参数
        buffer.putInt(period);//低功耗模式
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());

    }

    /**
     * 告警信息
     * @param bsid
     * @param tagid
     * @param fileId
     * @param text
     * @param level  0： 普通紧急   1：特别紧急
     * @return
     */
    public boolean setTagWarningText(Long bsid,long tagid,int fileId,String text,short level,long time) throws UnsupportedEncodingException {
        bsid= getBsid(bsid,tagid);
        byte[] textByte=text.getBytes("GBK");
        int textLen=textByte.length;
        int size=textLen+12;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x06);//配置指令
        buffer.put((byte)0x03);//
        buffer.putInt(fileId);
        buffer.putInt((int)time/1000);
        buffer.put((byte)(level));
        buffer.put((byte)textLen);
        buffer.put(textByte);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     * 普通文本
     * @param bsid
     * @param tagid
     * @param fileId
     * @param text
     * @return
     */
    public boolean setTagCommonText(Long bsid,long tagid,int fileId,String text,long time) throws UnsupportedEncodingException {
        bsid= getBsid(bsid,tagid);
        if(text!=null&&text.length()>0)
           text=text;
        else
            return false;
        byte[] textByte=text.getBytes("GBK");
        int textLen=textByte.length;
        int size=textLen+11;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x06);//配置指令
        buffer.put((byte)0x01);//
        buffer.putInt(fileId);
        buffer.putInt((int)(time/1000));
        buffer.put((byte)textLen);
        buffer.put(textByte);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     * 配置标签是井口还是井下
     * @param bsid
     * @param tagid
     * @param state   0x00: 井上， 0x01井下，
     * @return
     */
    public boolean setTagPosition(Long bsid,long tagid,int messageid,byte state,String detail,long time) throws UnsupportedEncodingException {
        bsid= getBsid(bsid,tagid);
        byte[] detailBytes=detail.getBytes("GBK");
        int detailBytesLen=detailBytes.length;
        int size=12+detailBytesLen;
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+size);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)size);//length
        buffer.put((byte)0x06);//配置指令
        buffer.put((byte)0x02);//配置标签tag显示井口，井下
        buffer.putInt(messageid);
        buffer.putInt((int)(time/1000));//时间戳
        buffer.put(state);//配置标签tag显示井口，井下
        buffer.put((byte)detailBytesLen);
        buffer.put(detailBytes);
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     * 获取标签版本信息
     * @param bsid
     * @param tagid
     */
    public Boolean getTagVersionInf(long bsid, long tagid) {
        bsid= getBsid(bsid,tagid);

        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)2);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x13);//
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }


    /**
     * 设置标签时间
     * @param bsid
     * @param tagid
     * @return
     */
    public Boolean setTagTimestamp(long bsid, long tagid,Long time) {
        bsid= getBsid(bsid,tagid);
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)6);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x14);//
        buffer.putInt(time==null?(int)(new Date().getTime()/1000):time.intValue());
        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     * 设置09标签组测距地址
     * @param bsid
     * @param tagid
     * @param type
     * @param bsids
     * @return
     */
    public Boolean setTagGroupBslist(long bsid, long tagid,byte type,String bsids) {
        bsid= getBsid(bsid,tagid);
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+35);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)35);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x15);//
        buffer.put(type);//0 读 1 写
        if(type==0)
            bsids="0,0,0,0,0,0,0,0";

       String[] bsidArray=bsids.split(",");
        for(String bs:bsidArray)
            buffer.putInt(Integer.valueOf(bs));

        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

    /**
     * 设置09标签组测距周期
     * @param bsid
     * @param tagid
     * @param type
     * @param time
     * @return
     */
    public Boolean setTagGroupPeriod(long bsid, long tagid,byte type,Integer time) {
        bsid= getBsid(bsid,tagid);
        ByteBuffer buffer= ByteBuffer.allocate(TagParaHeadLen+7);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt((int)tagid);//tagid
        buffer.putShort((short)0);//messageid
        buffer.putShort((short)7);//length
        buffer.put((byte)0x01);//配置指令
        buffer.put((byte)0x16);//
        buffer.put(type);//0 读 1 写
        buffer.putInt(time==null?0:time);

        return sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
    }

 public Boolean processTagUpdate(Long bsid,Long tagid,TagFirmware tagFirmware,String fileUrl,String fileName,Integer pkgLen,String version) throws IOException {
                 bsid=getBsid(bsid,tagid);
             TagfirmwareEntity tagfirmwareEntity= tagFirmwareService.findByTagid(tagid);
             boolean insert=false;
             if(tagfirmwareEntity==null) {
                 insert=true;
                 tagfirmwareEntity = new TagfirmwareEntity();
                 tagfirmwareEntity.setTagid(tagid.intValue());
             }
             tagfirmwareEntity.setUtc(new Date().getTime());
             if(fileUrl!=null)
             tagfirmwareEntity.setUpdatestate(1);
             if(insert)
                 tagFirmwareService.insert(tagfirmwareEntity);
             else
                 tagFirmwareService.updateById(tagfirmwareEntity);



            byte[] firmware=new byte[2];
               if(fileUrl!=null) {
                 //  int version = tagFirmware.Pre_firmware_rev.get();
                 //  firmware= ByteUtils.toByteArray3(fileUrl);
                   if(!mapContainer.tagFirmWare.containsKey(version)) {
                       firmware = ByteUtils.openFile(fileUrl);
                       mapContainer.tagFirmWare.putIfAbsent(version, firmware);
                   }else{
                       firmware=mapContainer.tagFirmWare.get(version);
                   }
                   TagInf taginf = mapContainer.tagInf.get(tagid+"");
                   if(taginf==null){
                       taginf=new TagInf((long)tagid+"");
                       mapContainer.tagInf.put((long)tagid+"",taginf);
                   }
                  taginf.firmwareVersion=version;
                   //current_version = tagUpdateCheck(fileName, tagFirmware.Pre_firmware_rev.get());
               }
               System.out.println("update tagid:"+tagid+":"+(tagfirmwareEntity.getUpdatestate()<100?(byte)0x01:(byte)0x00));
             ByteBuffer buffer=ByteBuffer.allocate(TagParaHeadLen+17);
              buffer.order(ByteOrder.LITTLE_ENDIAN);
              buffer.putShort((short)0);//timing
              buffer.putInt((tagid.intValue()));//tagid
              buffer.putShort((short)(updateid++));//messageid
              buffer.putShort((short)17);//length
              buffer.put((byte)0x04);//标签升级
              buffer.put((byte)0x04);//标签升级
              buffer.put(tagfirmwareEntity.getUpdatestate()<100?(byte)0x01:(byte)0x00);
              buffer.put((byte)0x00);
              buffer.put((byte)0x00);
              buffer.put((byte)0x00);
              buffer.putInt(firmware.length);
              buffer.putInt(pkgLen==null?256:pkgLen);
              sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
              //if(tagfirmwareEntity.getUpdatestate()==1)
              //sendOtaData(bsid,tagid.intValue(),firmware,0,pkgLen==null?256:pkgLen);

      return true;
 }


 public void sendOtaData(long bsid,int tagid,byte[] firmwareData,long pkgid,long pkgsize){
   int totalLen=firmwareData.length;
     double totalPkg = Math.ceil(totalLen / (double) pkgsize);
    int percent=(int)Math.floor((pkgid/totalPkg)*100);
     TagfirmwareEntity tagfirmwareEntity= tagFirmwareService.findByTagid((long)tagid);
     boolean insert=false;
     if(tagfirmwareEntity==null) {
         insert=true;
         tagfirmwareEntity = new TagfirmwareEntity();
     }
     tagfirmwareEntity.setUpdatestate(percent==0?1:percent);
     tagfirmwareEntity.setUtc(new Date().getTime());

     if(insert)
         tagFirmwareService.insert(tagfirmwareEntity);
     else
         tagFirmwareService.updateById(tagfirmwareEntity);

     TagPara tagPara=new TagPara();
     tagPara.setKeyOrder("update");
     tagPara.setUpdatestate(percent);
     tagPara.setTagid(tagid+"");
     kafukaSender.send(KafukaTopics.TAG_CONTROLRES,tagPara.toString());

    byte[] pkgData= tagUpdateData(firmwareData,pkgid,pkgsize);
    int len=pkgData.length;
    ByteBuffer buffer=ByteBuffer.allocate(TagParaHeadLen+14+len);
     buffer.order(ByteOrder.LITTLE_ENDIAN);
     buffer.putShort((short)0);//timing
     buffer.putInt(tagid);//tagid
     int random = new Random().nextInt(1000);
     buffer.putShort((short)(updateid++));//messageid
     buffer.putShort((short)(len+14));//length
    buffer.put((byte)0x04);
     buffer.put((byte)0x05);
     buffer.putInt((int)pkgid);
     buffer.putInt(len);
     buffer.putInt(ByteUtils.BKDRHash(pkgData,len));
     buffer.put(pkgData);
     sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
 }

    public void processTagUpdateFinish(long bsid,int tagid,byte res) throws IOException {

       ByteBuffer buffer=ByteBuffer.allocate(TagParaHeadLen+3);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)0);//timing
        buffer.putInt(tagid);//tagid
        buffer.putShort((short)(updateid++));//messageid
        buffer.putShort((short)3);//length
        buffer.put((byte)0x04);
        buffer.put((byte)0x06);
        buffer.put(res);
        sendData.sendDate(bsid,CMD_TAG_PARA,buffer.array());
        TagfirmwareEntity tagfirmwareEntity= tagFirmwareService.findByTagid((long)tagid);
        boolean insert=false;
        if(tagfirmwareEntity==null) {
            insert=true;
            tagfirmwareEntity = new TagfirmwareEntity();
        }
        tagfirmwareEntity.setUtc(new Date().getTime());
        if(res==0)
        {
            tagfirmwareEntity.setUpdatestate(100);
            System.out.println(tagid+"update 100%");
        }
        else
            tagfirmwareEntity.setUpdatestate(-1);
        if(insert)
            tagFirmwareService.insert(tagfirmwareEntity);
        else
            tagFirmwareService.updateById(tagfirmwareEntity);

        TagPara tagPara=new TagPara();
        tagPara.setKeyOrder("update");
        tagPara.setTagid(tagid+"");
        tagPara.setUpdatestate(res==0?100:0);
        kafukaSender.send(KafukaTopics.TAG_CONTROLRES,tagPara.toString());
        mapContainer.currentUpdateTag.remove((long)tagid);
        Iterator<Map.Entry<Long, TagPara>> iter = mapContainer.waitUpdateTag.entrySet().iterator();
        if(iter.hasNext()){
            Map.Entry<Long, TagPara> entry = iter.next();
            TagPara waitTag = entry.getValue();
            Long tagNum = entry.getKey();
            tagParaConfig.processTagUpdate(-1l,tagNum,null,waitTag.getFirmwareUrl(),"",waitTag.getPkglen(),waitTag.getFirmwareVersion());
            waitTag.setTime(new Date().getTime());
            mapContainer.waitUpdateTag.remove(tagNum);
            mapContainer.currentUpdateTag.put(tagNum,waitTag);

        }

    }


 public long tagUpdateCheck(String fileName,long oldVersion){

     String str =fileName;
     str=str.trim();
     String str2="";
     if(str != null && !"".equals(str)){
         for(int i=0;i<str.length();i++){
             if(str.charAt(i)>=48 && str.charAt(i)<=57){
                 str2+=str.charAt(i);
             }
         }}
     long curr_version=Long.valueOf(str2).longValue();
     if(curr_version>oldVersion)
         return curr_version;
     else
         return -1;

 }



public byte[] tagUpdateData(byte[] data,long pkgid,long pkgsize) {
        boolean last=true;
       int left= (int)(data.length%pkgsize);
       int totalcount=0;
       totalcount=(int)(data.length/pkgsize);
       if(left>0)
           totalcount=totalcount+1;
     byte[] download_data_pkgdata;
    // pkgid=201;
    if((pkgid+1)==totalcount) {
       download_data_pkgdata = Arrays.copyOfRange(data, (int) (pkgid * pkgsize), data.length);
       if(download_data_pkgdata.length<pkgsize){
           byte[] download_data_pkgdata2 = new byte[(int) pkgsize];
           for(int i=0;i<download_data_pkgdata.length;i++){
               download_data_pkgdata2[i]=download_data_pkgdata[i];
           }
           return  download_data_pkgdata2;
       }
   }else
         download_data_pkgdata = Arrays.copyOfRange(data, (int) (pkgid * pkgsize),(int) ((pkgid+1) * pkgsize));

    // download_data_pkgdata = Arrays.copyOfRange(data, (int) ((totalcount-1)*pkgsize), data.length);
    return download_data_pkgdata;
 }
    /**
     * 判断是否基站是否为空，如果为空，则需要选择标签附件的基站进行发送
     * @param target
     * @return
     */
    public long  getBsid(Long bsid,long target){
        long res=bsid;
        if(bsid==null||bsid==-1){
            TagInf tagInf = mapContainer.tagInf.get(target+"");
            if(tagInf!=null){
                res=tagInf.bsid;
            }
        }
        return res;
    }



}
