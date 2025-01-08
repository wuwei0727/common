package com.tgy.rtls.location.config.deviceconfig;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.entity.es.ESMag;
import com.tgy.rtls.data.entity.location.DiagData;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVo;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.update.BsfirmwareEntity;
import com.tgy.rtls.data.entity.video.VideoPlaceStatus;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.data.kafukaentity.FilePara;
import com.tgy.rtls.data.kafukaentity.TagSensor;
import com.tgy.rtls.data.kafukaentity.TagSingleDis;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.location.LocationMapper;
import com.tgy.rtls.data.mapper.park.GuideScreenDeviceMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.update.BsfirmwareDao;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.es.impl.ESInfraredOriginalImpl;
import com.tgy.rtls.data.service.es.impl.ESMagServiceImpl;
import com.tgy.rtls.data.service.location.LocationService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.impl.FloorLockConfigService;
import com.tgy.rtls.data.service.video.VideoPlaceStatusService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.snowflake.AutoKey;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.Utils.Constant;
import com.tgy.rtls.location.check.BsUpdateCheck;
import com.tgy.rtls.location.check.ErrorCodeInf;
import com.tgy.rtls.location.check.Run;
import com.tgy.rtls.location.dataprocess.TdoaInterfaceImp;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.*;
import com.tgy.rtls.location.netty.MapContainer;
import com.tgy.rtls.location.netty.SendData;
import com.tgy.rtls.location.struct.Beacon;
import com.tgy.rtls.location.struct.BsLocMode;
import com.tgy.rtls.location.struct.UwbRawInfAll;
import com.tgy.rtls.location.struct.UwbTag;
import com.tgy.rtls.location.tdoa.BsCoef;
import com.tgy.rtls.location.tdoa.BsTimestamp;
import com.tgy.rtls.location.tdoa.BsTimestamps;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import static com.tgy.rtls.location.Utils.Constant.*;

@Component
public class BsParaConfigImp implements BsParaConfig {
    @Autowired
    SendData  sendData;
    @Autowired
    MapContainer mapContainer;
    @Autowired
    SubService subService;
    @Autowired(required = false)
    TagMapper tagMapper;
    @Autowired(required = false)
    GatewayMapper gatewayMapper;
    @Autowired(required = false)
    GatewayService gatewayService;
    @Autowired
    AutoKey autoKey;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Autowired(required = false)
    GuideScreenDeviceMapper guideScreenDeviceMapper;
    @Autowired(required = false)
    KafukaSender kafukaSender;
    @Autowired
    TagParaConfig tagParaConfig;
    @Autowired(required = false)
    BsfirmwareDao bsfirmwareDao;
    @Autowired(required = false)
    LocationService locationService;
    @Autowired(required = false)
    LocationMapper locationMapper;
    @Autowired
    TdoaInterfaceImp tdoaInterfaceImp;
    @Autowired
    private ESInfraredOriginalImpl esInfraredOriginalImpl;
    @Autowired(required = false)
    private ScreenConfig ScreenConfig;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private VideoPlaceStatusService videoPlaceStatusService;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private RestTemplate restTemplate;
    @Value("${location.infrared.t0.time}")
    private Integer t0Minute;
    @Value("${location.infrared.t1.time}")
    private Integer t1Minute;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired
    private FloorLockService floorLockService;

    @Resource
    private FloorLockConfigService floorLockConfigService;
    @Resource
    private ESMagServiceImpl magService;


    public ConcurrentHashMap<String , NedData> ned_cmd=new ConcurrentHashMap<>();





    public boolean sendHeartData(Long bsid){
        BsInf bsInf = mapContainer.bsInf.get(bsid+"");
        if(bsInf==null){
            bsInf=new BsInf();
            mapContainer.bsInf.put(bsid+"",bsInf);
        }
        //int headid=(int )autoKey.getAutoId("");
        ByteBuffer buffer=ByteBuffer.allocate(12);
        buffer.putInt(bsInf.heartid++);
        buffer.putLong(new Date().getTime());


        return sendData.sendDate(bsid,CMD_BS_SENDHEART, buffer.array());


    }

    /**
     * 设置基站公司名
     * @param bsid
     * @param text
     * @return
     */
    public boolean setBsCompanyText(Long bsid,String text){
        text=text+"\0";
        byte[]  textBytes=text.getBytes();
        int textLen=textBytes.length;
        ByteBuffer buffer= ByteBuffer.allocate(textLen+2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)textLen);
        buffer.put(textBytes);
       return sendData.sendDate(bsid,CMD_BS_TEXT,buffer.array());
    }

    /**
     * 基站升级
     * @param bsid
     * @return
     */
    public void getBsUpdate(Long bsid,ByteBuffer data){
       int tagid=data.getInt();//tagid
       byte fileType=data.get();
       int fileId=data.getInt();
        FilePara filePara=new FilePara();
       filePara.setFileType(fileType);
       filePara.setBsid(bsid);
       filePara.setMessageid(fileId);
        filePara.setDirection((short) 1);
        filePara.setProcess((short)100);
        filePara.setState((short)0);
       kafukaSender.send(KafukaTopics.FIlE_RES,filePara.toString());
        BsfirmwareEntity bs = bsfirmwareDao.findByBsid(bsid);
        if(bs==null) {
            bs=new BsfirmwareEntity();
            if(fileType==0)
                bs.setArmupdatestate(100);
            else if(fileType==3)
                bs.setUwbupdatestate(100);
                bs.setBsid(bsid);
            bsfirmwareDao.insertBsfirmwareEntity(bs);
        }
        else {
            if(fileType==0)
                bs.setArmupdatestate(100);
            else if(fileType==3)
                bs.setUwbupdatestate(100);
            bsfirmwareDao.update(bs);
        }

    }





    /**
     * 获取基站版本参数
     * @param bsid
     * @return
     */
    public boolean getBsVersionInf(Long bsid){
        String text="\0";
        byte[]  textBytes=text.getBytes();
        int textLen=textBytes.length;
        ByteBuffer buffer= ByteBuffer.allocate(textLen+2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)textLen);
        buffer.put(textBytes);
        return sendData.sendDate(bsid,CMD_BS_VERSIONINF,buffer.array());
    }

    /**
     * 设置基站网络参数
     * @param bsid
     * @param ip
     * @param netmask
     * @param network
     * @param gateway
     * @return
     */
    public boolean setBsNetInf(Long bsid,String ip,String netmask,String network,String gateway,int ip_type){
        if(ip_type==0){
            ip="192.168.1.5";
            netmask="255.255.0.0";
            network="192.168.1.1";
            gateway="192.168.1.1";
        }else{
  /*          String[] ips=ip.split(".");
            netmask="255.255.0.0";
            network=ips[0]+"."+ips[1]+"."+ips[2]+"."+1;
            gateway=ips[0]+"."+ips[1]+"."+ips[2]+"."+1;*/
        }

        String ip1=ip+"\0";
        String netmask1=netmask+"\0";
        String network1=network+"\0";
        String gateway1=gateway+"\0";
        byte[] ip_byte=new byte[20];
        byte[] netmask_byte=new byte[20];
        byte[] network_byte=new byte[20];
        byte[] gateway_byte=new byte[20];
        byte[] ip_bytes=ip1.getBytes();
        byte[] netmask_bytes=netmask1.getBytes();
        byte[] network_bytes=network1.getBytes();
        byte[] gateway_bytes=gateway1.getBytes();
     for(int i=0;i<ip_bytes.length;i++){
         ip_byte[i]=ip_bytes[i];
     }
        for(int i=0;i<netmask_bytes.length;i++){
            netmask_byte[i]=netmask_bytes[i];
        }
        for(int i=0;i<network_bytes.length;i++){
            network_byte[i]=network_bytes[i];
        }
        for(int i=0;i<gateway_bytes.length;i++){
            gateway_byte[i]=gateway_bytes[i];
        }



        ByteBuffer buffer= ByteBuffer.allocate(20*4+4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(ip_type);
        buffer.put(ip_byte);
        buffer.put(netmask_byte);
        buffer.put(network_byte);
        buffer.put(gateway_byte);

        return sendData.sendDate(bsid,CMD_BS_NET,buffer.array());
    }

    /**
     * 设置基站位置文字信息
     * @param bsid
     * @param text
     * @return
     */
    public boolean setBsLocationText(Long bsid,String text){
        text=text+"\0";
        byte[]  textBytes=text.getBytes();
        int textLen=textBytes.length;
        ByteBuffer buffer= ByteBuffer.allocate(textLen+2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)textLen);
        buffer.put(textBytes);
        return sendData.sendDate(bsid,CMD_BS_LOCATION,buffer.array());
    }


    /**
     * 设置基站背景图
     * @param bsid
     * @param url
     * @param fileName
     * @return
     */
    public boolean setBsCompanyImg(Long bsid,String url,String fileName){
       String[] fileUrl=url.split("/");
        url=url+"\0";
       if(fileName==null||fileName.length()==0)
           fileName=fileUrl[fileUrl.length-1]+"\0";

    //    fileName=fileName+"\0";
        byte[] urlBytes=url.getBytes();
     //   byte[] fileNameBytes=fileName.getBytes();
        ByteBuffer buffer= ByteBuffer.allocate(urlBytes.length+2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short)(urlBytes.length));
        buffer.put(urlBytes);
     //   buffer.put(fileNameBytes);
        return sendData.sendDate(bsid,CMD_BS_IMG,buffer.array());
    }

    /**
     * 设置基站功率
     * @param bsid
     * @param gain
     * @param rw
     * @return
     */
    public boolean setBsPower(Long bsid,int gain,int  rw){
        ByteBuffer buffer= ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(rw);
        buffer.putInt(gain);
        return sendData.sendDate(bsid,CMD_BS_POWER,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param gegeral  0 reboot  1 syn 2 stop syn
     * @return
     */
    public boolean setBsGeneral(Long bsid,int gegeral){
        ByteBuffer buffer= ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(gegeral);
        return sendData.sendDate(bsid,CMD_BS_GENERAL,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param beepState  0 关闭  1 响
     * @param period   间隔单位ms
     * @return
     */
    public boolean setBsBeep(Long bsid,int beepState,int  period){
        ByteBuffer buffer= ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(beepState);
        buffer.putInt(period);
        return sendData.sendDate(bsid,CMD_BS_BEEP,buffer.array());
    }

    /**
     *
     * @param bsid
     * @param mode
     * @param superFrame_interval
     * @param slot_duration
     * @param bsrssi
     * @param bsrange
     * @return
     */
    public boolean setTagModeViaBs(Long bsid,Byte mode,short superFrame_interval,short slot_duration,float bsrssi,float  bsrange){

        BsLocMode bsLocMode=new BsLocMode();
        ByteBuffer buffer=ByteBuffer.allocate(bsLocMode.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        //使用默认时区和语言环境获得一个日历。
        Calendar    rightNow    =    Calendar.getInstance();
        /*用Calendar的get(int field)方法返回给定日历字段的值。
        HOUR 用于 12 小时制时钟 (0 - 11)，HOUR_OF_DAY 用于 24 小时制时钟。*/
        int year = rightNow.get(Calendar.YEAR);
        int month = rightNow.get(Calendar.MONTH)+1; //第一个月从0开始，所以得到月份＋1
        int day = rightNow.get(rightNow.DAY_OF_MONTH);
        int hour12 = rightNow.get(rightNow.HOUR);
        int hour24 = rightNow.get(rightNow.HOUR_OF_DAY);
        int minute = rightNow.get(rightNow.MINUTE);
        int second = rightNow.get(rightNow.SECOND);
        int millisecond = rightNow.get(rightNow.MILLISECOND);

        buffer.put(mode);
        buffer.putShort(superFrame_interval);
        buffer.putShort(slot_duration);
        buffer.putFloat(bsrssi);
        buffer.putFloat(bsrange);
        buffer.put((byte) (year-2020));
        buffer.put((byte)(month));
        buffer.put((byte)(day));
        buffer.put((byte)(hour24));
        buffer.put((byte) (minute));
        buffer.put((byte)(second));
        buffer.putShort((short)(millisecond));

        return sendData.sendDate(bsid,CMD_BS_LOCPARA,buffer.array());
    }


    /**
     *
     * @param bsid
     * @param warning  0 关闭  1 响
     * @return
     */
    public boolean setBsWarning(Long bsid,BsPara warning){
        ByteBuffer buffer= ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)warning.getWarningState());
        buffer.put((byte)warning.getRelay_id());
        return sendData.sendDate(bsid,CMD_BS_WARNING,buffer.array());
    }



    /**
     *发送文件到基站，包括音频文件和固件，可由基站发送到标签
     * @param bsid
     * @param target 标签ID(0XFFFFFFFF代表广播，0代表该文件发送给基站)
     * @param fileType 文件类型 0：固件 1：音频文件
     * @param fileId  唯一文件id
     * @param fileUrl ascii编码 ‘\0’结尾
     * @param fileName  ascii编码 ‘\0’结尾
     * @return
     */
    public boolean sendBsFile(Integer instanceId,Long bsid,Integer target,byte fileType,int fileId,String fileUrl,String fileName){
          logger.error(instanceId+":"+bsid+":"+target+":"+fileType+":"+fileId+":"+fileUrl+":"+fileName);
             switch (target){
                 case -1:  //发送给所有标签

                     break;
                 case 0:   //发送给基站
                     target=0;
                     break;
                     default://发送给特定标签
                         if(bsid==-1)
                         bsid=getBsid((long)target);
                         break;
             }
              fileUrl=fileUrl+'\0';
              fileName=fileName+'\0';
              byte[] fileUrlBytes=fileUrl.getBytes();
              byte[] fileNameBytes=fileName.getBytes();
                ByteBuffer buffer= ByteBuffer.allocate(9+fileUrlBytes.length+fileNameBytes.length);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                if(target==-1)
                buffer.putInt(0xfffffff);
                else
                    buffer.putInt(target);
                buffer.put(fileType);
                buffer.putInt(fileId);
                buffer.put(fileUrlBytes);
                buffer.put(fileNameBytes);
                byte[] cmd=CMD_BS_SENDFILE;
                switch (fileType){
                    case 0:
                        cmd=CMD_BS_UPDATE;// ARM update
                        break;
                    case 1:
                        cmd=CMD_BS_SENDFILE;
                        break;
                    case 3:
                        cmd=CMD_BS_UPDATE;//MCU update
                        break;


                }
                if(fileType==0 ||fileType ==3){
                    BsfirmwareEntity bs = bsfirmwareDao.findByBsid(bsid);
                    if(bs==null) {
                        bs=new BsfirmwareEntity();
                        if(fileType==0) {
                          //  bs.setArmupdatestate(1);
                        }
                        else if(fileType==3){
                           // bs.setUwbupdatestate(1);
                        }

                        bs.setBsid(bsid);
                        bsfirmwareDao.insertBsfirmwareEntity(bs);
                    }
                    else {
                        if(fileType==0) {
                           // bs.setArmupdatestate(1);
                        }
                        else if(fileType==3) {
                          //  bs.setUwbupdatestate(1);
                        }
                        bsfirmwareDao.update(bs);
                    }
                    BsUpdateCheck bsUpdate = new BsUpdateCheck(bsid+"", fileType,new Date().getTime() + 50000); // 加3000，表示延时3秒
                    mapContainer.messageQueue.offer(bsUpdate);
                    Executor executor = SpringContextHolder.getBean("threadPool1");;
                   executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            logger.error("基站升级");
                            while (mapContainer.messageQueue.size()!=0) {
                                try {
                                    BsUpdateCheck message = mapContainer.messageQueue.take(); // take方法，有消息（延时时间<0的消息）取消息，没有，阻塞住。
                                    BsfirmwareEntity bsUpdateInf = bsfirmwareDao.findByBsid(Long.valueOf(message.bsid));
                                    if(bsUpdateInf!=null){
                                        int bsUpdateFileType=message.fileType;
                                        switch (bsUpdateFileType){
                                            case 0:
                                                if(bsUpdateInf.getArmupdatestate()!=100){
                                                   bsUpdateInf.setArmupdatestate(-1);
                                                }
                                                break;
                                            case 3:
                                                if(bsUpdateInf.getUwbupdatestate()!=100){
                                                    //bsUpdateInf.setUwbupdatestate(-1);
                                                }
                                                break;
                                        }
                                        bsfirmwareDao.update(bsUpdateInf);
                                    }
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                       }
                    });

                }
                if(bsid==-1) {
                 /*   Iterator bsiter = mapContainer.bsInf.entrySet().iterator();
                    while (bsiter.hasNext()) {
                        Map.Entry entry = (Map.Entry) bsiter.next();
                        BsInf bsInf = (BsInf) entry.getValue();
                        Long eachBsid = (Long) entry.getKey();
                        sendData.sendDate(eachBsid, CMD_BS_SENDFILE, buffer.array());
                    }*/
                    List<BsSyn> list = subService.findByAll(null,null,null, null, null, null, null, null,null,null, instanceId);
                    for (BsSyn bs:list
                         ) {

                        logger.info("- qun send file =via:" + bs.getNum()+":target:"+target+":url:"+fileUrl);
                        try {
                            sendData.sendDate(Long.valueOf(bs.getNum()), cmd, buffer.array());
                        }catch (Exception e){
                              e.printStackTrace();
                        }

                    }
                }else {
                    logger.info("-send file =via:" + bsid+":target:"+target+":url:"+fileUrl);
                    sendData.sendDate(bsid, cmd, buffer.array());
                }
        return true;
    }




    /**
     * 设置基站范围内标签的距离
     * @param bsid
     * @param tagid
     * @param dis  距离  单位米
     * @param lr  0：左侧 1：右侧
     * @param rssi
     * @param rssiFp
     */
    public void setCoalBs_TagDis(Long bsid,String tagid,float dis,byte lr,float rssi,float rssiFp,byte flag,String name,byte move,float volt,Long rangeBs){

        TagSingleDis tagSingleDis=new TagSingleDis();
        tagSingleDis.setDis(dis);
        tagSingleDis.setBsid(bsid);
        tagSingleDis.setTagid(Integer.valueOf(tagid));
        tagSingleDis.setDis(dis);
        tagSingleDis.setLr(lr);
        tagSingleDis.setTime(new Date().getTime());
       // System.out.println(tagid+"测距基站"+bsid+":左右:"+lr+"---距离--"+dis);
        name=name+"\0";
        if(rangeBs!=null)
        kafukaSender.send(KafukaTopics.TAG_RANGE,tagSingleDis.toString());
        ByteBuffer buffer = ByteBuffer.allocate(43);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(Integer.valueOf(tagid));//tagid
        buffer.put(lr);//0 :左侧 1 ：右侧
        buffer.putFloat(dis);//距离
        buffer.putFloat(rssi);//信号强度
        buffer.putFloat(rssiFp);//首达径强度
        buffer.put(flag);//移除或者添加数据  0：移除 1： 添加
        buffer.put(move);//运动传感器  0：不动 1： 运动
        buffer.putFloat(volt);//电压
        byte[] name_bytes=new byte[20];
        byte[] name_byteso=name.getBytes();
        for(int i=0;i<name_byteso.length;i++){
            name_bytes[i]=name_byteso[i];
        }
        buffer.put(name_bytes);
        sendData.sendDate(bsid,CMD_BS_TAGDIS,buffer.array());

/*        TagLocation tagLocation=new TagLocation();
        tagLocation.setArea(bsid+"");
        tagLocation.setTagid(tagid);
        tagLocation.setX( (float)(Math.round(dis*100))/100);
        tagLocation.setY( lr);
        tagLocation.setZ( 0);
        tagLocation.setTime(new Date().getTime());
        tagLocation.setType((short) 0);
        tagLocation.setBsid(bsid);
        kafukaSender.send(KafukaTopics.TAG_LOCATION,tagLocation.toString());*/






    }

    /**
     *  基站测距
     * @param bsid
     * @param rangeId  测距id
     * @param type    设备类型，0 为基站   1为标签
     * @param targetId   测距目标id
     * @return
     */
    public boolean startBsRange(Long bsid,int rangeId,int type, Long targetId){
        ByteBuffer buffer= ByteBuffer.allocate(12);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(rangeId);
        buffer.putInt(type);
        buffer.putInt((int)targetId.longValue());
        return sendData.sendDate(bsid,CMD_BS_RANGE,buffer.array());
    }

    /**
     * 设置基站老化距离和老化时间
     * @param bsid
     * @param
     */
    public void setBsOldTimeAndDis(Long bsid ,float  dis,short time){
        ByteBuffer buffer= ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(dis);
        buffer.putShort(time);
        sendData.sendDate(bsid,CMD_BS_OLDTIMEDIS,buffer.array());
    }
    /**
     * 设置基站老化距离和老化时间
     * @param bsid
     * @param
     */
    public Boolean  processCat1_cmd(Long bsid,int  cmd,int data){
        ByteBuffer buffer= ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte)cmd);
        buffer.putInt(bsid.intValue());
        buffer.put((byte)data);
        byte[] msgid={0x00,0x00,0x00,0x00};
        sendData.sendDataToCAT1(bsid,CMD_CAT1_CMD,msgid,buffer.array());
        return true;
    }


    /**
     * 基站网络配置响应
     * @param bsid
     * @param res
     */
    public void getBsNetConfigRes(Long bsid ,byte res){
        BsPara bsPara=new BsPara();
        bsPara.setBsid(bsid);
        bsPara.setKeyOrder("net");
        if(res==0)
        kafukaSender.send(KafukaTopics.BS_CONTROLRES,bsPara.toString());
    }


    /**
     * 配置基站屏幕参数响应
     * @param bsid
     * @param buffer
     */
    public void getBsTagDisRes(Long bsid , ByteBuffer buffer){
        int tagid = buffer.getInt();
        byte flag = buffer.get();
        byte res=buffer.get();
        if(flag==0&&res==0){
            TagInf tagInf = mapContainer.tagInf.get(tagid+"");
            tagInf.screenCache.remove(bsid);
        }
    }

    @Override
    public UwbRawInfAll getBsBeacon(Long bsid, ByteBuffer buffer) throws ParseException {

        byte[] data1 = buffer.array();
        UwbRawInfAll uwbRawInf=new UwbRawInfAll();
        uwbRawInf.setByteBuffer(buffer,0);
        if(true){
            storageRawData(bsid,uwbRawInf,data1);
        }

        switch (uwbRawInf.type.get()){
            case 0x64://beacon 帧
                processBeacon(bsid,uwbRawInf);
                break;
            case 0x70://标签poll帧
                processTagPoll(bsid,data1);
                break;
        }



                ;


        return uwbRawInf;


      /*  cc.

        UwbFrameTiming uwbFrameTiming=new UwbFrameTiming();
        uwbFrameTiming.*/
      /*  BsInf bsinf = mapContainer.bsInf.get(bsid + "");
        if(bsinf==null){
            bsinf=new BsInf();
            mapContainer.bsInf.put(bsid+"",bsinf);
        }*/
   /*     logger.info("beacon up bsid:"+bsid+
                        ":beacon src:" +beacon.beacon_src.get()+
                         ":beacon tx:"+beacon.beacon_txTs.get()+
                           ":beacon rx:"+beacon.beacon_rxTs.get());*/
      //  bsinf.beacon=beacon;
/*        BigDecimal ft1_1= ByteUtils.readUnsignedLong(beacon.ft1_1.get());
        BigDecimal ft2_1=ByteUtils.readUnsignedLong(singleRange.ft2_1.get());
        BigDecimal ft3_1=ByteUtils.readUnsignedLong(singleRange.ft3_1.get());*/
/*        beacondata.setBsid(bsid+"");
        beacondata.setBeacon_src(beacon.beacon_src.get()+"");
        beacondata.setBeacon_rxTs(beacon.beacon_rxTs.get()+"");
        beacondata.setBeacon_txTs(beacon.beacon_txTs.get()+"");
        beacondata.setBeacon_fp(beacon.beacon_fp.get()+"");
        beacondata.setBeacon_rssi(beacon.beacon_rssi.get()+"");
        beacondata.setX(mapContainer.d1);
        beacondata.setY(mapContainer.d2);*/
        //locationService.addBeacondata(beacondata);
    }




    /**
     * 向基站发送属于当前基站应该显示的标签
     * @param bsid
     */
    public void sendScreenCache(long bsid) {
        Iterator<Map.Entry<String, TagInf>> tagIter = mapContainer.tagInf.entrySet().iterator();
        while (tagIter.hasNext()){
            Map.Entry<String, TagInf> entry = tagIter.next();
            TagInf tagInf=entry.getValue();
            Iterator<Map.Entry<Long, Bs_tagDis>> bsIter = tagInf.screenCache.entrySet().iterator();
            while (bsIter.hasNext()){
                Map.Entry<Long, Bs_tagDis> bs_tagDis = bsIter.next();
                Long bs = bs_tagDis.getKey();
                Bs_tagDis disInf = bs_tagDis.getValue();
                if(bs.longValue()==bsid){
                    setCoalBs_TagDis(bsid,disInf.getTagid(),disInf.getDis(),disInf.getLr(),0,0,(byte) 1,disInf.name,disInf.move,disInf.getVolt(),null);
                }
            }
        }

    }



    /**
     * 处理基站信息，修改数据库和发送卡夫卡信息
     * @param bsid
     * @param ver
     */
    public void getBsVersion(long bsid, String ver) {

        JSONObject json =  JSONObject.fromObject(ver);
        String value=json.getString("value");
        BsfirmwareEntity bsFirmWare=(BsfirmwareEntity)JSONObject.toBean(JSONObject.fromObject(value), BsfirmwareEntity.class);

        bsFirmWare.setBsid(bsid);
        BsfirmwareEntity bs = bsfirmwareDao.findByBsid(bsid);
        if(bs==null) {
                bsfirmwareDao.insertBsfirmwareEntity(bsFirmWare);
        }
        else {
            bsFirmWare.setId(bs.getId());
            bsFirmWare.setUwbupdatestate(bs.getUwbupdatestate());
            bsFirmWare.setArmupdatestate(bs.getArmupdatestate());
            bsfirmwareDao.update(bsFirmWare);
        }
    }


    /**
     * 测试基站误码率
     * @param bsid
     * @param bsPara
     */
    public void setRandomKey(Long bsid , BsPara bsPara){
        if(bsPara.getType()==1){
        ErrorCodeInf errorCodeInf=new ErrorCodeInf();
        errorCodeInf.messageid=bsPara.getMessageid();
        errorCodeInf.count=bsPara.getCount();
        errorCodeInf.bsid=bsid;
        errorCodeInf.interval=bsPara.getSendInterval();
        //mapContainer.testid_inf.put(bsPara.getMessageid(),errorCodeInf);
        Thread th1=new Thread(new Run(errorCodeInf));
        errorCodeInf.th=th1;
        th1.start();
        }else{
      //      ErrorCodeInf error = mapContainer.testid_inf.get(bsPara.getMessageid());
      /*      BserrorcodetestrecordEntity res = bserrorcodetestDao.getByTagCheckId((long)bsPara.getMessageid());
            BserrorcodetestrecordEntity res1 = bserrorcodetestrecordDao.getByTagCheckid((long) bsPara.getMessageid());
            res1.setEnd(new Timestamp(new Date().getTime()));
            res1.setState((short)0);
            res1.setSendnum(res.getSendnum());
            res1.setReceivenum(res.getReceivenum());
            res1.setLost(res.getSendnum()-res.getReceivenum());
            res1.setErrornum(res.getErrornum());
            res1.setLostrate(res.getLost()/(double)res.getSendnum());
            res1.setErrorrate(res.getErrornum()/(double)res.getSendnum());*/
         //   error.stopFlag=true;
        }

    }

    /**
     * 判断是否是发送给特定标签的如果是，则需要选择标签附件的基站进行发送
     * @param target
     * @return
     */
  public long  getBsid(Long target){
        long bsid=-1;
      if(target!=null||target>0){
          TagInf tagInf = mapContainer.tagInf.get(target+"");
          if(tagInf!=null){
              bsid=tagInf.bsid;
          }
      }
      return bsid;
   }

    @Override
    public long processBeacon(Long bsid, UwbRawInfAll  uwbRawInf) throws ParseException {
        Beacon beacon=new Beacon();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String time = (2000 + uwbRawInf.year.get()) + "-" +
                String.format("%0" + 2 + "d", uwbRawInf.mon.get()) + "-" +
                String.format("%0" + 2 + "d", uwbRawInf.date.get()) + " " +
                String.format("%0" + 2 + "d", uwbRawInf.hour.get()) + ":" +
                String.format("%0" + 2 + "d", uwbRawInf.minute.get()) + ":" +
                String.format("%0" + 2 + "d", uwbRawInf.second.get()) + "." +
                String.format("%0" + 3 + "d", uwbRawInf.ms.get());
        Date date = formatter.parse(time);
        long rangeid = date.getTime();
       //  logger.error(bsid + ":beacon id" + rangeid + ":is tx:" + uwbRawInf.isTx.get() + ":bstimestamp:" + uwbRawInf.timestamp);
        BsInf bsinf = mapContainer.bsInf.get(uwbRawInf.src.get()  + "");
        if (bsinf == null) {
            bsinf = new BsInf();
            mapContainer.bsInf.put(uwbRawInf.src.get()  + "", bsinf);
        }

        BsCoef bsinf_coef = bsinf.bsCoef.get(bsid + "");
        if (bsinf_coef == null) {
            bsinf_coef = new BsCoef();
            bsinf.bsCoef.put(bsid  + "", bsinf_coef);
        }

        beacon.beacon_id.set(rangeid);
        if (uwbRawInf.isTx.get() == 1) {
            beacon.beacon_txTs.set(uwbRawInf.timestamp.get());//主基站
        } else
            beacon.beacon_rxTs.set(uwbRawInf.timestamp.get());//从基站
        //计算晶振校正值

        bsinf_coef.reFreshTimestamp(rangeid,uwbRawInf.timestamp.get());
        bsinf.CalculateCoef(uwbRawInf.src.get()  + "",bsid+"");




        beacon.beacon_src.set(uwbRawInf.src.get());
        BsInf bsinf_beacon = mapContainer.bsInf.get(bsid + "");
        if (bsinf_beacon == null) {
            bsinf_beacon = new BsInf();
            mapContainer.bsInf.put(bsid  + "", bsinf_beacon);
        }
        bsinf_beacon.beacon = beacon;
        return 0;
    }



    @Override
    public long processTagPoll(Long bsid, byte[] data1) {
        ByteBuffer buf1 = ByteBuffer.wrap(data1);
        buf1.order(ByteOrder.LITTLE_ENDIAN);
        UwbTag tag=new UwbTag();
        tag.setByteBuffer(buf1,0);
      //  logger.error("status"+tag.status.get());
        int has_baro=ByteUtils.getBitByByte((byte)tag.status.get(),7);
        int pg=ByteUtils.getBitByByte((byte)tag.status.get(),6);
        int charge=ByteUtils.getBitByByte((byte)tag.status.get(),5);
        int is_active=ByteUtils.getBitByByte((byte)tag.status.get(),4);
        int is_stationary=ByteUtils.getBitByByte((byte)tag.status.get(),3);
        int has_sos=ByteUtils.getBitByByte((byte)tag.status.get(),2);
        int has_motion_sensor=ByteUtils.getBitByByte((byte)tag.status.get(),1);
        int has_motion=ByteUtils.getBitByByte((byte)tag.status.get(),0);
      /*  logger.error(tag.tagid.get()+"标签电压"+tag.vbat.get()
                +" 是否有气压计 "+has_baro
                +" 标签是否连接充电线 "+pg
                +" 标签是否正在充电 "+charge
                +" 标签为活动状态 "+is_active
                +" 标签是否完全静止 "+is_stationary
                +" 标签sos "+has_sos
                +" 标签G_seneor是否正常 "+has_motion_sensor
                +" 是否motion "+has_motion
                +" 步数 "+tag.steps.get()
        );*/
        TagSensor tagSensor=new TagSensor();
        tagSensor.setTime(new Date().getTime());
        tagSensor.setTagid(tag.tagid.get()+"");
        tagSensor.setPower((float) Math.round(tag.vbat.get()* 100) / 100f);
        tagSensor.setMoveState((short)is_active);
        tagSensor.setType((short)0);
        tagSensor.setBsid(bsid+"");
        tagSensor.setSteps(tag.steps.get());
        kafukaSender.send(KafukaTopics.TAG_SENSOR,tagSensor.toString());

        long pacc=tag.pacc.get();
        long maxGrowthCIR= tag.maxGrowthCIR.get();
        long firstPathAmp1=tag.firstPathAmp1.get();
        long firstPathAmp2=tag.firstPathAmp2.get();
        long firstPathAmp3=tag.firstPathAmp3.get();

        double  rssi = 10 * Math.log10((maxGrowthCIR*Math.pow(2,17)/(pacc * pacc)))-121.74;
        double  fp = 10 * Math.log10((firstPathAmp1*firstPathAmp1 + firstPathAmp2 * firstPathAmp2 + firstPathAmp3 * firstPathAmp3)/(pacc * pacc))  - 121.74;



        logger.error(tag.tagid.get()+"::ping:"+bsid+":up ssss rangeid:"+tag.seq+":ping:"+tag.timestamp.get()+"fp:"+fp+":rssi:"+rssi);
        TagInf taginf = mapContainer.tagInf.get(tag.tagid.get() + "");
        if(taginf==null){
            taginf=new TagInf(tag.tagid.get() + "");
            mapContainer.tagInf.put(tag.tagid.get() + "",taginf);
        }
        if(taginf.getSteps()!=0&&taginf.getSteps()==tag.steps.get())
            is_active=0;
        taginf.setSteps((int)tag.steps.get());

       /* if(is_active==0&&taginf.firstMove<0)
            return 0;
        else{
            if(is_active==0) {
                if (taginf.firstMove < 20) {
                    taginf.firstMove++;
                } else {
                    taginf.firstMove = -1;
                }
            }else{
                taginf.firstMove=0;
            }
        }*/


        synchronized (taginf){
            BsTimestamps bsTimestamps = taginf.getTdoaTimestamp().get(tag.seq.get());
            if(bsTimestamps==null){
                bsTimestamps=new BsTimestamps();
                taginf.getTdoaTimestamp().put(tag.seq.get(),bsTimestamps);
                bsTimestamps.rangeid=tag.seq.get();
                bsTimestamps.time=new Date().getTime();
            }
            BsTimestamp bsTimestamp = new BsTimestamp();
            bsTimestamp.bid = bsid + "";
            bsTimestamp.ping_ts = new BigDecimal(tag.timestamp.get());


            tdoaInterfaceImp.storageTimestamp(bsTimestamps, bsTimestamp);
           // logger.error(tagid + ":ping up bsid:" + bsid + ":rangeid:" + rangeid + ":pingts:" + pollRx);
            tdoaInterfaceImp.tdoaLocation(taginf);
        }
        return 0;
    }

    @Override
    public long storageRawData(Long bsid,UwbRawInfAll uwbRawInf, byte[] data1) {
        ByteBuffer buf1 = ByteBuffer.wrap(data1);
        buf1.order(ByteOrder.LITTLE_ENDIAN);
        DiagData diagData=new DiagData();
        diagData.setFrametype(uwbRawInf.type.get());
        diagData.setIsTx(uwbRawInf.isTx.get());
        diagData.setTimestamp(new BigDecimal(uwbRawInf.timestamp.get()));
        diagData.setUpbsid(bsid+"");
        diagData.setHasdiag(uwbRawInf.has_diag.get());
        diagData.setUwb_error(uwbRawInf.uwb_error.get());
        diagData.setX(mapContainer.d1);
        diagData.setY(mapContainer.d2);
        String data = ByteUtils.printHexString(data1);
        diagData.setUwbdata(data);
        //diagData.setDiag(uwbRaw.diag_info.toString());
        locationMapper.addDiag(diagData);
        return 0;
    }

    @Override
    public void processInfraredPark(Long bsid,ByteBuffer data,byte[] cmd,byte[] msgid,long len) {
        Date date = new Date();
        int  parkid = data.getInt();
         int  state1 = data.get();
        int  power = data.get();
         int timestamp1=-9999;
        double rssi=0;
        if(len==23){
            rssi=(data.get()&0xff-20)*0.5-120;
            timestamp1=((state1)>>1&0x7f);
            byte mv=(byte) (0xff>>(8-1));
            state1=state1&mv;
        }
        final int  state=state1;
        final int  timestamp=timestamp1;
       long msg_id= ByteUtils.bytes2long(msgid);
        ESInfraredOriginal original = new ESInfraredOriginal();
        original.setTimestamp(TimeUtil.localDateTimeToStrTime(LocalDateTime.now()));
        original.setGatewayNum(String.valueOf(bsid.intValue()));
        original.setInfraredNum(String.valueOf(parkid));
        original.setState(state);
        original.setCount(timestamp);
        original.setRssi((int) rssi);
        original.setPower(power);
        esInfraredOriginalImpl.addOriginal(original);

        logger.error("#网关#"+bsid+"#车位检测器状态ID#"+parkid+"#网关messageid#"+msg_id+"#车位占用状态#"+state+"#时间戳#"+timestamp+"#电量#"+power+"#信标强度#"+rssi);
        byte[] send_data ={};
        sendData.sendDataToLora(bsid,CMD_PARK_STATE,msgid,send_data);
        Ned ned=mapContainer.device_Ned.get((long)parkid);
            if(ned==null){
                ned=new Ned();
                ned.bsid=bsid;
                ned.id=parkid;
                ned.date=new Date();
                mapContainer.device_Ned.put((long)parkid,ned);
            }else{
                ned.bsid=bsid;
                ned.date=new Date();
            }


        if(state>1||parkid>10000) {
            return;
        }


        if(mapContainer.lora_finalTIme.containsKey(bsid)){
            mapContainer.lora_finalTIme.replace(bsid,new Timestamp(new Date().getTime()));
        }else {
            mapContainer.lora_finalTIme.put(bsid,new Timestamp(new Date().getTime()));
        }
        Executor executor = SpringContextHolder.getBean("threadPool1");
        executor.execute(() -> {
            List<InfraredMessage> infrareds = tagMapper.findIredByIdAndName1(parkid);
            InfraredMessage infrared;
            if(infrareds!=null&&infrareds.size()==1) {
                infrared=infrareds.get(0);
                Integer network = infrared.getNetworkstate();
                if(network==2){
                    network=3;
                }else {
                    network=1;
                }

                infrared.setBatteryTime(date);
                infrared.setNetworkName("在线");
                kafukaSender.send(KafukaTopics.INFRARED_STATE,infrared.toString());
                logger.info("deviceId： "+infrared.getId());

                deactivateExistingLowPowerAlarm(infrared);

                if(!NullUtils.isEmpty(infrared.getMap())){
                    DeviceAlarms deviceAlarms = new DeviceAlarms();
                    deviceAlarms.setEquipmentType(3);
                    deviceAlarms.setAlarmType(2);
                    deviceAlarms.setNum(Integer.valueOf(infrared.getNum()));
                    deviceAlarms.setState(0);
                    deviceAlarms.setDeviceId(infrared.getId());
                    deviceAlarms.setMap(infrared.getMap());

                    // 计算电量百分比
                    short batteryPercentage = 100;
                    Integer lifetimeMonths = infrared.getLifetimeMonths();  // 假设 infrared 实体中添加了 lifetimeMonths 字段
                    if (lifetimeMonths != null && lifetimeMonths > 0) {
                        LocalDateTime installTime = infrared.getLocalDateTime();  // 假设 infrared 实体中添加了 addTime 字段
                        if(installTime==null){
                            installTime=LocalDateTime.now();
                        }
                        LocalDateTime now = LocalDateTime.now();
                        long monthsBetween = ChronoUnit.MONTHS.between(installTime, now);

                        double percentage = (1 - ((double)monthsBetween / lifetimeMonths)) * 100;
                        percentage = Math.round(percentage);
                        batteryPercentage = (short) Math.max(0, Math.min(100, percentage));
                        if(!NullUtils.isEmpty(batteryPercentage)){
                            infrared.setPower(batteryPercentage);
                            infrared.setStatus(state);
                            infrared.setNetworkstate(network);
                            infrared.setBatteryTime(date);
                            infrared.setCount(timestamp);
                            tagMapper.updateInfrared1(infrared);
                        }
                    }
                    DeviceAlarms device = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                            .eq("equipment_type",3)
                            .eq("state", 0)
                            .eq("device_id", deviceAlarms.getDeviceId())
                            .isNull("end_time"));

                    if (!NullUtils.isEmpty(device)){
                        deviceAlarms.setId(device.getId());
                    }

                    if(NullUtils.isEmpty(device)||NullUtils.isEmpty(device.getStartTime())){
                        deviceAlarms.setStartTime(LocalDateTime.now());
                    }
                    handleBatteryAlarm(deviceAlarms, batteryPercentage, device, infrared.getNum());
                }


                if(infrared!=null&&infrared.getPlace()!=null) {
                    List<PlaceVo> list = new ArrayList<>();
                    List<ParkingPlace> places = parkMapper.getPlaceById(infrared.getPlace());
                    if(places != null && places.size() > 0){
                        logger.error("----检测器编号"+infrared.getNum()+"#车位名:#"+places.get(0).getName()+"#车位检测器状态ID#"+infrared.getStatus());

                            //检测器更新方式
                            if(places.get(0).getConfigWay()==1||places.get(0).getConfigWay()==3){
                                PlaceVo placeVo = new PlaceVo();
                                placeVo.setId(infrared.getPlace());
                                placeVo.setName(infrared.getPlaceName());
                                placeVo.setConfigWay(places.get(0).getConfigWay());
                                List<Infrared> infraredLists = parkingService.getInfraredByPlaceId(null, infrared.getPlace());

                                // 检查是否存在检测器且检测器数量是否为1
                                if (!NullUtils.isEmpty(infrareds) && infraredLists.size() == 1) {
                                    // 只有一个检测器，跳过超声判断子流程，但继续后续逻辑
                                    placeVo.setState((short)state);
                                    list.add(placeVo);
                                    updatePlaceDataByPlaceId(list); // 更新车位状态信息
                                    return; // 执行完继续流程，不进入超声子流程
                                }

                                // 如果存在多个检测器，进入超声判断子流程
                                if (infraredLists.size() > 1) {
                                    PlaceVo manyInfrared = manyInfrared(placeVo.getId());

                                    // 判断是否存在多个检测器（超声检测器+视频检测器组合）
                                    if (!NullUtils.isEmpty(manyInfrared)) {
                                        // 进入超声判断子流程
                                        placeVo.setState(manyInfrared.getState());
                                        placeVo.setLicense(places.get(0).getLicense());
                                        list.add(placeVo);
                                        updatePlaceDataByPlaceId(list); // 更新车位状态信息
                                    } else {
                                        return; // 没有多个检测器且无法进入子流程，结束流程
                                    }
                                }
                            }
                        }
                    }
                }else {
                    infrared=new InfraredMessage();
                    infrared.setNum(parkid+"");
                    infrared.setStatus(state);
                    infrared.setNetworkstate(1);
                    infrared.setPower((short)power);
                    tagMapper.addInfrared1(infrared);
                    infrared.setNetworkName("在线");
                    kafukaSender.send(KafukaTopics.INFRARED_STATE,infrared.toString());
                }
        });

    }

    /**
     * 取消现有的低电量报警
     *
     * @param infrared 红外设备对象
     */
    private void deactivateExistingLowPowerAlarm(InfraredMessage infrared) {
        DeviceAlarms infraredDevice = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                .eq("equipment_type", 3)
                .eq("state", 0)
                .eq("device_id", infrared.getId())
                .isNull("end_time"));

        if (!NullUtils.isEmpty(infraredDevice)) {
            LocalDateTime now = LocalDateTime.now();
            LambdaUpdateWrapper<DeviceAlarms> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(DeviceAlarms::getState, 1)
                    .set(DeviceAlarms::getEndTime, now)
                    .eq(DeviceAlarms::getAlarmType, 1)
                    .eq(DeviceAlarms::getEquipmentType, 3)
                    .eq(DeviceAlarms::getState, 0)
                    .eq(DeviceAlarms::getDeviceId, infrared.getId())
                    .isNull(DeviceAlarms::getEndTime);
            deviceAlarmsService.update(null, updateWrapper);
            logger.info("已取消红外设备 {} 的低电量报警", infrared.getId());
        }
    }

    // 添加新的处理方法
    private void handleBatteryAlarm(DeviceAlarms deviceAlarms, short batteryPercentage, DeviceAlarms existingDevice, String infraredNum) {
        if (batteryPercentage < 10) {
            activateAlarm(deviceAlarms, 1, existingDevice);
            tagMapper.updateInfraredStateBecomesLowPower(deviceAlarms.getDeviceId(), 2, null);
            System.out.println("高级别报警触发，红外编号: " + infraredNum + ", 电量百分比: " + batteryPercentage + "%");
        } else if (batteryPercentage >= 10 && batteryPercentage <= 30) {
            activateAlarm(deviceAlarms, 2, existingDevice);
            System.out.println("中级别报警触发，红外编号: " + infraredNum + ", 电量百分比: " + batteryPercentage + "%");
        } else if (batteryPercentage > 50) {
            if (!NullUtils.isEmpty(existingDevice)) {
                deviceAlarms.setState(1);
                deviceAlarms.setEndTime(LocalDateTime.now());
                deviceAlarm(deviceAlarms, NullUtils.isEmpty(existingDevice) ? null : existingDevice.getPriority(), existingDevice);
                System.out.println("报警解除，红外编号: " + infraredNum + ", 电量百分比: " + batteryPercentage + "%");
            }
        }
    }

    private void activateAlarm(DeviceAlarms deviceAlarms, int priority, DeviceAlarms existingDevice) {
        deviceAlarms.setPriority(priority);
        deviceAlarms.setState(0);
        deviceAlarm(deviceAlarms, NullUtils.isEmpty(existingDevice) ? null : existingDevice.getPriority(), existingDevice);
    }

    public void deviceAlarm(DeviceAlarms deviceAlarms,Integer lastTimePriority,DeviceAlarms lastTimeDeviceAlarms){
        if(!NullUtils.isEmpty(lastTimePriority)&&!NullUtils.isEmpty(deviceAlarms.getPriority())&&!deviceAlarms.getPriority().equals(lastTimePriority)&&deviceAlarms.getState()==0){
            deviceAlarms.setStartTime(LocalDateTime.now());
        }
        if(NullUtils.isEmpty(lastTimeDeviceAlarms)||!NullUtils.isEmpty(lastTimeDeviceAlarms.getEndTime())){
            deviceAlarms.setId(null);
            deviceAlarms.setStartTime(LocalDateTime.now());
            deviceAlarmsService.save(deviceAlarms);
        }else {
            deviceAlarmsService.saveOrUpdate(deviceAlarms);
        }
    }



    private PlaceVo manyInfrared(Integer place){
        // 检查是否有其他红外检测器，以及它们的状态
        boolean allInfraredsFree = true;
        PlaceVo placeVo = new PlaceVo();

        List<Infrared> infrareds = parkMapper.getInfraredByTime(16, place);
        if(!NullUtils.isEmpty(infrareds)){
            placeVo.setUpdateTime(LocalDateTime.now());
            for (Infrared infrared : infrareds) {
                if(infrared.getStatus()==1){
                    allInfraredsFree = false;
                    break;
                }
            }
            // 定义新的车位状态，0 代表空闲，1 代表占用
            int newStatus = allInfraredsFree ? 0 : 1;
            placeVo.setState((short) newStatus);
        }else{
            placeVo.setState((short) 3);
        }
        return placeVo;
    }

    private void updatePlaceDataByPlaceId(@RequestBody List<PlaceVo> list){
        try {
            //更新数据的list
            PlaceVo newPlace = new PlaceVo();
            LocalDateTime now = LocalDateTime.now(); // 当前时间
            list.forEach(v ->{
                //车位更新状态是超声
                if(v.getConfigWay()==1){//当前为超声检测器
                    //更新状态和车牌
                    ParkingPlace place = new ParkingPlace();
                    place.setId(v.getId());
                    place.setState(v.getState());
                    place.setUpdateTime(now);
                    parkingService.updatePlace(place);
                    sendEmptyPlaceToScreen(v.getId());
                }else if(v.getConfigWay()==3){
                    if(v.getState()==1){//超声_车位对象为占用
                        //查询车位关联的摄像头数据是占用 and 当前时间减去摄像头时间是小于t0(用t2: 15)分钟
                        VideoPlaceStatus videoPlaceStatus = videoPlaceStatusService.getById(v.getId());
                        if(!NullUtils.isEmpty(videoPlaceStatus)){
                            Duration videoDuration = Duration.between(videoPlaceStatus.getUpdateTime(), now); // 计算两个时间之间的持续时间
                            if(videoPlaceStatus.getState()==1&&videoDuration.toMinutes() < t0Minute){
                                //更新状态和车牌
                                newPlace.setId(v.getId());
                                newPlace.setState(v.getState());
                                newPlace.setLicense(videoPlaceStatus.getLicense());
                                newPlace.setUpdateTime(now);
                                parkMapper.updateInfraredPlace(newPlace);
                                sendEmptyPlaceToScreen(v.getId());
                            }
                            // else{
                            //     //更新状态和车牌
                            //     newPlace.setId(v.getId());
                            //     newPlace.setState((short) 1);
                            //     newPlace.setUpdateTime(now);
                            //     parkMapper.updateInfraredPlace(newPlace);
                            // }
                        }else{
                            //更新状态和车牌
                            ParkingPlace place = new ParkingPlace();
                            place.setId(v.getId());
                            place.setState((short) 1);
                            place.setUpdateTime(now);
                            parkingService.updatePlace(place);
                            sendEmptyPlaceToScreen(v.getId());
                        }
                    }else {//超声_车位对象空闲
                        //空闲状态下车位更新子流程
                        updateParkingSpaceToIdle(v,now);
                    }
                }
            });
            // dockingService.updateBatchById(placeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void updateParkingSpaceToIdle(PlaceVo v,LocalDateTime now){
        //不是占用-->空闲
        VideoPlaceStatus videoPlaceStatus = videoPlaceStatusService.getPlaceByTime(t1Minute,v.getId());
        if(!NullUtils.isEmpty(videoPlaceStatus)){
            if(videoPlaceStatus.getState()==0){//如果不是空,代表满足是条件,
                //车位状态更新为空闲，车牌置空
                parkMapper.updatePlaceById(v.getId(), (short) 0,"null",null,null,null,null);
                sendEmptyPlaceToScreen(v.getId());
            }else {//否
                if(videoPlaceStatus.getState()==1){
                    parkMapper.updatePlaceById(v.getId(), videoPlaceStatus.getState(),videoPlaceStatus.getLicense(),null,null,null,null);
                    sendEmptyPlaceToScreen(v.getId());
                }
            }
        }else {
            ParkingPlace place = new ParkingPlace();
            place.setId(v.getId());
            place.setState((short) 0);
            parkingService.updatePlace(place);
            sendEmptyPlaceToScreen(v.getId());
        }
    }


    void sendEmptyPlaceToScreen(Integer placeId){
        //根据车位id查询绑定的区域
        List<Integer>areaIds=guideScreenDeviceMapper.selectAreaIdByPlaceId(placeId);
        //根据区域id查询该区域绑定的车位编号
        if (areaIds != null) {
            for (Integer areaId:areaIds
            ) {
                try{
                    List<Integer> places= guideScreenDeviceMapper.selectPlaceBindAreaId(areaId);
                    logger.info("车位id"+places.get(0).intValue());
                    logger.info("车位数"+places.size());
                    //根据区域id查询区域绑定的屏幕编号
                    ShowScreenConfig screen=guideScreenDeviceMapper.selectScreenDeviceByAreaId(areaId);
                    logger.info("区域id"+areaId);
                    Integer count=parkMapper.selectEmptyCountByPlaces( places);
                    logger.info("getDeviceNum"+screen.getGuideScreenId());
                    logger.info("count"+screen.getScreennum());
                    Long bsid= screen.getGuideScreenId();
                    Byte addr= (byte) Integer.valueOf(screen.getScreennum()).intValue();
                    ScreenConfig.sendEmpty_placeToScreen_S( bsid,count,screen.getScreennum());
                }catch (Exception e){
                    logger.info(e.getMessage());
                }
            }

        }
    }
    public static void main(String[] args) {
        String ssds="sd";
        Infrared infrared=new Infrared();
        infrared.setNum(ssds);
        infrared.setStatus((short)1);
        if(infrared.getNum().equals("sd")){
            System.out.println("==string");
            if(infrared.getStatus()==1){
                System.out.println("==1");
            }
        }
    }

    //网关心跳
    @Override
    public void processGateway_lora(Long bsid, ByteBuffer data, byte[] cmd, byte[] msgid) {
        logger.info("433Mhz网关心跳数据:"+bsid+":msgid:"+ByteUtils.bytes2long(msgid));
        byte[] send_data ={};
      List<Gateway_lora> gateways = gatewayMapper.findGateway_loraByNum(bsid + "");
        Gateway_lora gateway;
        if(mapContainer.lora_finalTIme.containsKey(bsid)){
            Timestamp time_final = mapContainer.lora_finalTIme.get(bsid);
            if((new Date().getTime()-time_final.getTime())/1000>1500){
                processGate_reboot(bsid,msgid);
                mapContainer.lora_finalTIme.replace(bsid,new Timestamp(new Date().getTime()));
            }
        }
        if(gateways!=null&&gateways.size()>=1) {
            gateway=gateways.get(0);
            gateway.setNetworkstate((short)1);
            gatewayMapper.updateGateway_Lora(gateway);

        }else {
            gateway=new Gateway_lora();
            gateway.setNum(bsid+"");
            gateway.setNetworkstate((short)1);
             gatewayMapper.addGatewayLora(gateway);
        }
        sendData.sendDataToLora(bsid, Constant.CMD_GATEWAY_STATE,msgid,send_data);
    }



    public void  processCat1(Long bsid,ByteBuffer data,byte[] cmd,byte[] msgid)
    {
        logger.info("CAT1心跳数据:"+bsid+":msgid:"+ByteUtils.bytes2long(msgid));
       byte[] send_data ={};
      /*   List<Gateway_lora> gateways = gatewayMapper.findGateway_loraByNum(bsid + "");
        Gateway_lora gateway;
        if(mapContainer.lora_finalTIme.containsKey(bsid)){
            Timestamp time_final = mapContainer.lora_finalTIme.get(bsid);
            if((new Date().getTime()-time_final.getTime())/1000>1500){
                processGate_reboot(bsid,msgid);
                mapContainer.lora_finalTIme.replace(bsid,new Timestamp(new Date().getTime()));
            }
        }
        if(gateways!=null&&gateways.size()>=1) {
            gateway=gateways.get(0);
            gateway.setNetworkstate((short)1);
            gatewayMapper.updateGateway_Lora(gateway);

        }else {
            gateway=new Gateway_lora();
            gateway.setNum(bsid+"");
            gateway.setNetworkstate((short)1);
            gatewayMapper.addGatewayLora(gateway);
        }*/
        sendData.sendDataToCAT1(bsid, Constant.CMD_CAT1_STATE,msgid,send_data);
    }


    @Override
    public void processGateway_lora_version(Long bsid, byte[] data, byte[] cmd, byte[] msgid) {
        String version=new String(data);
        logger.error("bsid:"+bsid+version);
        String[] ss=version.split("\0");
        List<Gateway_lora> gateways = gatewayMapper.findGateway_loraByNum(bsid + "");
        Gateway_lora gateway;
        if(gateways!=null&&gateways.size()>=1) {
            gateway=gateways.get(0);
            gateway.setNetworkstate((short)1);
            gateway.setFirmware(ss[1]);
            gateway.setHardware(ss[0]);
            gatewayMapper.updateGateway_Lora(gateway);

        }else {
            gateway=new Gateway_lora();
            gateway.setNum(bsid+"");
            gateway.setNetworkstate((short)1);
            gateway.setFirmware(ss[1]);
            gateway.setHardware(ss[0]);
            gatewayMapper.addGatewayLora(gateway);
        }
    }

    @Override
    public void processInfrared_version(Long bsid, byte[] data,ByteBuffer buffer, byte[] cmd, byte[] msgid) {

        int parkid = buffer.getInt();
        int len=data.length;
        byte[] ddd= Arrays.copyOfRange(data, 4, len);
        //;
        logger.info(ByteUtils.printHexString(ddd));
        String version=new String(ddd);
        String[] ss=version.split("\0");
        List<Infrared> infrareds = tagMapper.findIredByIdAndName(null, null, parkid + "");
        Infrared infrared;

        if(infrareds!=null&&infrareds.size()==1) {
            infrared=infrareds.get(0);
            infrared.setStatus((short)1);
            infrared.setHardware(ss[0]);
            infrared.setFirmware(ss[1]);
            tagMapper.updateInfrared(infrared);
           
        }else {
            infrared=new Infrared();
            infrared.setNum(parkid+"");
            infrared.setStatus((short)1);
            infrared.setHardware(ss[0]);
            infrared.setFirmware(ss[1]);
            //infrared.setPower((short)power);
            tagMapper.addInfrared(infrared);
        }
    }

    @Override
    public Boolean processInfrared_para(Long bsid, Long parkid, byte check_interval, byte heart_interval) {
        ByteBuffer buffer= ByteBuffer.allocate(6);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(parkid.intValue());
        buffer.put(check_interval);
        buffer.put(heart_interval);
        return sendData.sendDate(bsid,CMD_INFRARED_PARA,buffer.array());
    }

    @Override
    public Boolean processGatewayLora_para(Long bsid, String doamin, String port) {
        byte[] domain_byte = (doamin+"\0").getBytes();
        byte[] port_byte =(port+"\0").getBytes();
        ByteBuffer buffer= ByteBuffer.allocate(port_byte.length+domain_byte.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(domain_byte);
        buffer.put(port_byte);
        return sendData.sendDate(bsid,CMD_LORA_NET,buffer.array());
    }

    @Override
    public Boolean processGatewayLora_update(Long bsid, String url) {
        byte[] domain_byte = (url+"\0").getBytes();
        ByteBuffer buffer= ByteBuffer.allocate(domain_byte.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(domain_byte);
        return sendData.sendDate(bsid,CMD_LORA_UPDATE,buffer.array());
    }

    @Override
    public Boolean processInfrared_update(Long bsid, Long parkid, String url) {
        byte[] domain_byte = (url+"\0").getBytes();
        ByteBuffer buffer= ByteBuffer.allocate(domain_byte.length+4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(parkid.intValue());
        buffer.put(domain_byte);
        return sendData.sendDate(bsid,CMD_INFRARED_UPDATE,buffer.array());
    }

    @Override
    public Boolean processInfrared_state(Long bsid, ByteBuffer buffer) {
        int  parkid = buffer.getInt();
        int  state1 = buffer.get()&0xFF;
        int  state2 = buffer.get()&0xFF;
        InfraredState infraredState=new InfraredState();
        infraredState.setInfrarednum(parkid+"");
        infraredState.setState(state1+"_"+state2);
        gatewayService.addInfraredState(infraredState);
        return null;
    }

    @Override
    public Boolean processGate_state(Long bsid, ByteBuffer buffer) {
        int  state = buffer.get()&0xff;
        GateWayState gateWayState=new GateWayState();
        gateWayState.setGatewaynum(bsid+"");
        gateWayState.setState(state);
        gatewayService.addGateWayState(gateWayState);
        return null;
    }

    /**
     * 控制网关重启
     * @param bsid
     */
    public Boolean  processGate_reboot(Long bsid,byte[] msgid){
        GateWayState gateWayState=new GateWayState();
        gateWayState.setGatewaynum(bsid+"");
        gateWayState.setState(-1);
        gatewayService.addGateWayState(gateWayState);
        ByteBuffer buffer= ByteBuffer.allocate(0);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return sendData.sendDataToLora(bsid,CMD_LORA_REBOOT,msgid,buffer.array());

    }
    public Boolean  processNed_data(Long bsid,ByteBuffer buffer,byte[] msgid){
        int  cmd = buffer.get();
        int  nedid = buffer.getInt();
        Ned ned = mapContainer.device_Ned.get((long) nedid);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         ESMag mag2=null;


            if(ned!=null){
                ned.bsid=bsid;
                ned.date=new Date();

            }else{
                ned=new Ned();
                ned.bsid=bsid;
                ned.id=nedid;
                ned.date=new Date();
                mapContainer.device_Ned.put((long)nedid,ned);
            }
        if(cmd<5) {
            int  data = buffer.getInt();
            if (ned.cmd != null && ned.cmd == cmd)
                ned.state = ned.cmd%2==data?0:1;
            logger.error("bsid:"+bsid+"ned:"+nedid+":ned.cmd:"+ned.cmd+"ned.state"+ned.state+"data"+data);//切换到正常模式指令执行结果
            }

        NedData nedData=new NedData();
        nedData.setNedid(nedid);
        nedData.setTime(dateFormat.format(new Date()));
        nedData.setCmd(cmd);
        switch (cmd){
            case 2:                // 0成功  1 失败
                logger.info("ned:"+nedid+":change to normal mode:"+ned.state);//切换到正常模式指令执行结果
                break;
            case 3:
                logger.info("ned:"+nedid+":-up ned:"+ned.state);//升锁状态指令执行结果
                break;
            case 4:
                logger.info("ned:"+nedid+":-down ned:");//降锁状态状态指令执行结果
                break;
            case 5:
                byte mode=buffer.get();// 3,4,2 锁的模式
                byte ultrsound=buffer.get();// 1占用 0 空闲
                byte up_down=buffer.get();//1 升起 0 降落
                int power=buffer.get()*10;
                nedData.setMode((int)mode);
                nedData.setOccupyState((int)ultrsound);
                nedData.setPosition((int)up_down);
                nedData.setPower((int)power);
                logger.info("ned:"+nedid+":-mode:"+mode);
                logger.info("ned:"+nedid+":-occupy:"+ultrsound);
                logger.info("ned:"+nedid+":-up-down:"+up_down);
                logger.info("ned:"+nedid+":-power:"+power);
                break;
            case 7:
                byte err_pos=buffer.get();// 1 开始  0结束
                logger.info("ned:"+nedid+":state:"+err_pos);//位置检测异常报警
                nedData.setWarningState((int)err_pos);
                break;
            case 8:
                byte duzhuan=buffer.get();// 1 开始  0 结果
                nedData.setWarningState((int)duzhuan);
                logger.info("ned:"+nedid+":duzhuan:"+duzhuan);//堵转报警
                break;
            case 9:
                String x_fix = String.valueOf(buffer.getFloat());
                String x = String.valueOf(buffer.getFloat());
                String y_fix = String.valueOf(buffer.getFloat());
                String y = String.valueOf(buffer.getFloat());
                String z_fix = String.valueOf(buffer.getFloat());
                String z = String.valueOf(buffer.getFloat());
                String x_diff = String.valueOf(buffer.getFloat());
                String y_diff = String.valueOf(buffer.getFloat());
                String z_diff = String.valueOf(buffer.getFloat());
                String occupy_x = String.valueOf(buffer.getFloat());
                String occupy_y = String.valueOf(buffer.getFloat());
                String occupy_z = String.valueOf(buffer.getFloat());
                String empty_x = String.valueOf(buffer.getFloat());
                String empty_y = String.valueOf(buffer.getFloat());
                String empty_z = String.valueOf(buffer.getFloat());

                String fix_state = String.valueOf(buffer.get());
                int mag_state = buffer.get();


                logger.info("ned:"+nedid
                        +":x_fix:"+x_fix
                        +":x:"+x
                        +":y_fix:"+y_fix
                        +":y:"+y
                        +":z_fix:"+z_fix
                        +":z_fix:"+z
                        +":x_diff:"+x_diff
                        +":y_diff:"+y_diff
                        +":z_diff:"+z_diff
                        +":state:"+mag_state
                );
                ESMag mag=new ESMag();
                mag.setNum(String.valueOf(nedid));
                mag.setX(x);
                mag.setY(y);
                mag.setZ(z);
                mag.setX_fix(x_fix);
                mag.setY_fix(y_fix);
                mag.setZ_fix(z_fix);
                mag.setX_diff(x_diff);
                mag.setY_diff(y_diff);
                mag.setZ_diff(z_diff);
                mag.setOccupy_x(occupy_x);
                mag.setOccupy_y(occupy_y);
                mag.setOccupy_z(occupy_z);
                mag.setEmpty_x(empty_x);
                mag.setEmpty_y(empty_y);
                mag.setEmpty_z(empty_z);
                mag.setState(mag_state);
                mag.setTime(TimeUtil.localDateTimeToStrTime(LocalDateTime.now()));
                magService.addMag(mag) ;
                mag2=mag;

                break;
            default:
                break;
        }
           Executor executor = SpringContextHolder.getBean("threadPool1");;
          executor.execute(new Runnable() {
              private ESMag  parameter;

              public Runnable withParameter(ESMag mag) {
                  this.parameter = mag;
                  return this;
              }
              @Override
              public void run() {
                  FloorLock lock=null;
                  List<FloorLock> res = floorLockService.getConditionData(nedid + "", null, null, null);
                  if (res != null && res.size() == 1)
                      lock = res.get(0);

        if(cmd==5||cmd==7||cmd==8||cmd==9){
            if(cmd==5) {
                if(lock!=null) {
                    if (lock.getState()==null||lock.getState().intValue() != nedData.getOccupyState() ||
                            lock.getModel()==null||  Integer.valueOf(lock.getModel()) != nedData.getMode().intValue() ||
                            lock.getFloorLockState()==null||Integer.valueOf(lock.getFloorLockState()) != nedData.getPosition().intValue() ||
                            lock.getNetworkstate() == 0||
                            lock.getPower()==null||Integer.valueOf(lock.getPower())!=nedData.getPower().intValue()
                           ) {
                        lock.setState(nedData.getOccupyState());
                        lock.setModel(nedData.getMode() + "");
                        lock.setNetworkstate((byte) 0x01);
                        lock.setFloorLockState(nedData.getPosition() + "");
                        lock.setPower(nedData.getPower()+"");
                        lock.setDate(new Date().getTime());
                        floorLockService.editFloorLockInfo(lock);
                        Integer placeId = lock.getPlace();
                        if(placeId!=null){
                            List<PlaceVo> list = new ArrayList<>();
                            List<ParkingPlace> places = parkMapper.getPlaceById(placeId);
                            if(places != null && places.size() > 0){
                                //检测器更新方式
                                if(places.get(0).getConfigWay()==1||places.get(0).getConfigWay()==3){
                                    PlaceVo placeVo = new PlaceVo();
                                    placeVo.setId(placeId);
                                    placeVo.setConfigWay(places.get(0).getConfigWay());
                                    placeVo.setState((short) (!NullUtils.isEmpty(nedData.getPosition().shortValue())&&(nedData.getPosition().shortValue()==1)?0:1));
                                    placeVo.setLicense(places.get(0).getLicense());
                                    list.add(placeVo);
                                    if(!NullUtils.isEmpty(list)){
                                        updatePlaceDataByPlaceId(list);
                                    }else {;}
                                }else {;}
                            }
                        }
                        List<DeviceAlarms> nedWarnings = deviceAlarmsService.list(new QueryWrapper<DeviceAlarms>()
                                .eq("equipment_type", 4)//地锁设备
                                .eq("state", 0)//0报警中
                                .eq("alarm_type", 2)//
                                .eq("device_id", lock.getId())//设备id
                                .eq("num", lock.getDeviceNum())//设备id
                                .isNull("end_time"));
                        if (nedData.getPower()<10&&(nedWarnings == null||nedWarnings.size()==0)) {
                            DeviceAlarms nedWarning = new DeviceAlarms();
                            nedWarning.setAlarmType(2);
                            nedWarning.setStartTime(LocalDateTime.now());
                            nedWarning.setEquipmentType(4);
                            nedWarning.setState(0);
                            nedWarning.setPriority(2);
                            nedWarning.setMap(lock.getMap().intValue());
                            nedWarning.setNum(nedData.getNedid());
                            nedWarning.setDeviceId(lock.getId().intValue());
                            deviceAlarmsService.save(nedWarning);
                        } else if((nedWarnings != null&&nedWarnings.size()>0)&&nedData.getPower()>=10) {

                           List<String> list_id =new ArrayList<>();
                            Iterator<DeviceAlarms> iterator = nedWarnings.iterator();
                            while (iterator.hasNext()) {
                                DeviceAlarms element = iterator.next();
                                list_id.add(element.getId()+"");
                            }

                            deviceAlarmsService.updateByIds(list_id.stream().toArray(String[]::new));
                        }

                    }
                    kafukaSender.send(KafukaTopics.NED_STATE,lock.toString());


                }


            }else if(cmd==9&&parameter!=null){
                Integer placeId=lock.getPlace();
                if(placeId!=null){
                    List<ParkingPlace> places = parkMapper.getPlaceById(placeId);
                    if(places != null && places.size() > 0) {
                        ParkingPlace place = places.get(0);
                        LocalDateTime time1 = place.getUpdateTime();
                        LocalDateTime time2 = LocalDateTime.now();
                        long secondsBetween = Math.abs(ChronoUnit.SECONDS.between(time1,time2));
                         if (place.getState() !=parameter.getState().intValue()&&secondsBetween>30&&secondsBetween<60){
                             List<Infrared> infrared = tagMapper.findInfraredId(placeId, null, null);
                             MagDiff magdiff = new MagDiff();
                            magdiff.setNed(Integer.valueOf(parameter.getNum()));
                            magdiff.setNed_state(parameter.getState().shortValue());
                            if(infrared!=null&&(!infrared.isEmpty())) {
                                String num=infrared.get(0).getNum();
                                magdiff.setInfrared(Integer.valueOf(num));
                            }
                            magdiff.setInfrared_state(place.getState());
                            tagMapper.addMagdiff(magdiff);
                        }
                    }
                    }

            }else {
								if (lock != null) {

                                    List<DeviceAlarms> nedWarnings = deviceAlarmsService.list(new QueryWrapper<DeviceAlarms>()
											.eq("equipment_type", 4)//地锁设备
											.eq("state", 0)//0报警中
											.eq("alarm_type", cmd == 7 ? 4 : 5)//
											.eq("device_id", lock.getId())//设备id
											.eq("num", lock.getDeviceNum())//设备id
											.isNull("end_time"));
									if ((nedWarnings == null||nedWarnings.size()==0)&&nedData.getWarningState()==1) {
                                        DeviceAlarms 	nedWarning = new DeviceAlarms();
										nedWarning.setAlarmType(cmd == 7 ? 4 : 5);
										nedWarning.setStartTime(LocalDateTime.now());
										nedWarning.setEquipmentType(4);
										nedWarning.setState(0);
										nedWarning.setPriority(2);
										nedWarning.setMap(lock.getMap().intValue());
										nedWarning.setNum(nedData.getNedid());
										nedWarning.setDeviceId(lock.getId().intValue());
										deviceAlarmsService.save(nedWarning);

									} else if((nedWarnings != null&&nedWarnings.size()>0)&&nedData.getWarningState()==0) {
                                        Iterator<DeviceAlarms> iterator = nedWarnings.iterator();
                                        List<String> list_id =new ArrayList<>();
                                        while (iterator.hasNext()) {
                                            DeviceAlarms element =(DeviceAlarms) iterator.next();
                                            list_id.add(element.getId()+"");
                                        }
										deviceAlarmsService.updateByIds(list_id.stream().toArray(String[]::new));
									}

                          }
                      }
                      if(lock!=null&&lock.getMap()!=null) {
                          List<FloorLockConfig> data = floorLockConfigService.getFloorLockConfigInfo(lock.getMap() + "", null, "id", null);
                          if (data != null && data.size() > 0) {
                              FloorLockConfig floorLockConfig = data.get(0);
                              String url = floorLockConfig.getDataInterface();
                             NedData  cmd= ned_cmd.get(nedData.getNedid()+"_"+nedData.getCmd());
                             int sendFlag=0;
                              if(cmd==null){
                                  cmd=new NedData();
                                  cmd.setNedid(nedData.getNedid());
                                  cmd.setCmd(nedData.getCmd());
                                  cmd.setMode(nedData.getMode());
                                  cmd.setOccupyState(nedData.getOccupyState());
                                  cmd.setPosition(nedData.getPosition());
                                  cmd.setPower(nedData.getPower());
                                  cmd.setDate( LocalDateTime.now());
                                  cmd.setWarningState(nedData.getWarningState());
                                  ned_cmd.put(nedData.getNedid()+"_"+nedData.getCmd(),cmd);
                                  sendFlag=1;
                              }else{
                                  LocalDateTime time1=cmd.getDate();
                                  LocalDateTime time2= LocalDateTime.now();
                                  long secondsBetween = Math.abs(ChronoUnit.SECONDS.between(time1,time2));
                                   if(secondsBetween>20){
                                       cmd.setDate(LocalDateTime.now());
                                       cmd.setMode(nedData.getMode());
                                       cmd.setOccupyState(nedData.getOccupyState());
                                       cmd.setPosition(nedData.getPosition());
                                       cmd.setPower(nedData.getPower());
                                       cmd.setDate( LocalDateTime.now());
                                       cmd.setWarningState(nedData.getWarningState());
                                       cmd.setDate(LocalDateTime.now());
                                       sendFlag=1;
                                   }else {
                                       if ((nedData.getCmd() == 5 && (cmd.getMode().intValue() != nedData.getMode().intValue() ||
                                               cmd.getOccupyState().intValue() != nedData.getOccupyState().intValue() ||
                                               cmd.getPosition().intValue() != nedData.getPosition().intValue() ||
                                               cmd.getPower().intValue() != nedData.getPower().intValue()
                                       ))||(nedData.getCmd() ==7||nedData.getCmd() ==8)&&(nedData.getWarningState().intValue()!=cmd.getWarningState().intValue())) {
                                           cmd.setMode(nedData.getMode());
                                           cmd.setOccupyState(nedData.getOccupyState());
                                           cmd.setPosition(nedData.getPosition());
                                           cmd.setPower(nedData.getPower());
                                           cmd.setDate( LocalDateTime.now());
                                           cmd.setWarningState(nedData.getWarningState());
                                           cmd.setDate(LocalDateTime.now());
                                           sendFlag=1;

                                       }
                                   }

                              }
                              if (url != null&&sendFlag==1) {
                                      logger.info("url post ned:"+nedData.getNedid()
                                              +":cmd:"+nedData.getCmd()
                                              +":mode:"+nedData.getMode()
                                              +":position:"+nedData.getPosition()
                                              +":power:"+nedData.getPower()
                                      );
                                      CompletableFuture.supplyAsync(() -> restTemplate.postForObject(url, nedData, Object.class));
                              }

                          }
                      }
                  }
              }
          }.withParameter(mag2));

        return null;
    }

}
