package com.tgy.rtls.location.config.deviceconfig;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.location.DiagData;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.update.BsfirmwareEntity;
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
import com.tgy.rtls.data.service.location.LocationService;
import com.tgy.rtls.data.snowflake.AutoKey;
import com.tgy.rtls.location.Utils.ByteUtils;
import com.tgy.rtls.location.Utils.Constant;

import com.tgy.rtls.location.dataprocess.TdoaInterfaceImp;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.BsInf;
import com.tgy.rtls.location.model.Bs_tagDis;
import com.tgy.rtls.location.model.Screen;
import com.tgy.rtls.location.model.TagInf;
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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;

import static com.tgy.rtls.location.Utils.Constant.*;

@Component
public class ScreenConfigImp implements ScreenConfig {
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

/*    @Autowired(required = false)
    BserrorcodetestDao bserrorcodetestDao;
    @Autowired(required = false)
    BserrorcodetestrecordDao bserrorcodetestrecordDao;*/
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
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    GuideScreenDeviceMapper guideScreenDeviceMapper;


    @Override
    public boolean sendEmpty_placeToScreen(Long bsid, Integer empty_place,byte[] CMD, byte addr) {
        String ss =String.format("%4s",empty_place+ "").replace(" ", "0");
        byte[] ssc=javax.xml.bind.DatatypeConverter.parseHexBinary(ss);
        byte[] cmd={(byte)0x00,(byte)0x08};
        byte check=(byte)(0xc9^addr^cmd[0]^cmd[1]^ssc[0]^ssc[1]);
        ByteBuffer buffer= ByteBuffer.allocate(8);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte)0xc9);
        buffer.put(addr);
        buffer.put(cmd);
        buffer.put(ssc[0]);
        buffer.put(ssc[1]);
        buffer.put(check);
        buffer.put((byte)0x9c);
        return sendData.sendDataToScreen(bsid,buffer.array());

    }

    public static void main(String[] args) {
     //  String ss= String.format("%4s", "9999").replace(" ", "0");
     //   byte[] ssc=javax.xml.bind.DatatypeConverter.parseHexBinary(ss);
     //   byte[] byte0_9={(byte)0xa0,};
      //  byte check=(byte)(0xc9^0x01^0x00^0x08^ssc[0]^ssc[1]);
       // System.out.println(check);

        String  empty_placeS="1";

        String sss= String.format("%4s", empty_placeS).replace(" ", "0");
        ByteBuffer buffer1= ByteBuffer.allocate(11+sss.length()*2);
        ByteBuffer buffer= ByteBuffer.allocate(10+sss.length()*2);

        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer1.order(ByteOrder.BIG_ENDIAN);
        buffer.put("GZCP".getBytes());
        buffer.put("01".getBytes());
            buffer.put("R".getBytes());
        buffer.put("*".getBytes());
        // buffer.put(empty_placeS.getBytes());
        for(int i=0; i<sss.length();i++) {
            int s0 =Integer.valueOf( sss.charAt(i)+""); // 就是指定索引处的字符值
            byte ss0= (byte)(160+s0);
            buffer.put((byte)0xf8);
            buffer.put(ss0);
        }
        buffer.put("*".getBytes());
        buffer.put("Z".getBytes());
        byte[] array= buffer.array();
        byte check=(byte)0x00;
        int len=array.length;
        for (int i = 0; i < len; i++) {
            if(i==0){
                check=array[0];
            }else {
                check=(byte)(check^array[i]);
            }
        }
        buffer1.put(array);
        buffer1.put(check);
        System.out.println(buffer1.array());
    }
    @Override
    public Boolean process4G_485Heart(Long bsid,Integer msgid) {
        logger.info("4G_485Heart"+bsid);
       BsInf bsInf = mapContainer.bsInf.get(bsid+"");
        if(bsInf==null){
            bsInf=new BsInf();
            mapContainer.bsInf.put(bsid+"",bsInf);
        }
      //  sendEmptyPlaceToScreen(15344);
       return sendTestToScreen_S(bsid);
        //int headid=(int )autoKey.getAutoId("");
      //  sendEmpty_placeToScreen_S(bsid,0,"9999");


     //   return sendData.sendDate(bsid,CMD_BS_SENDHEART, buffer.array());
      // Random random=new Random();
      //  System.out.println("process4G_485Heart"+bsid);
      // sendEmptyPlaceToScreen(238480);
      // sendEmpty_placeToScreen( bsid,  1,null, (byte)0x01);
       // return null;

    }

    @Override
    public boolean sendEmpty_placeToScreen_S(Long bsid, Integer empty_place, String addr) {

        Screen screen_count = mapContainer.device_Screen.get(bsid);
        if(screen_count==null){
            screen_count=new Screen();
            mapContainer.device_Screen.put(bsid,screen_count);
        }
        if(screen_count.screenName_count.containsKey(addr))
            screen_count.screenName_count.replace(addr,empty_place);
        else
            screen_count.screenName_count.put(addr,empty_place);
        String  empty_placeS=empty_place+"";
        logger.info("empty_placeS"+empty_placeS);
        logger.info("addr"+addr);
        String sss= String.format("%4s", empty_placeS).replace(" ", "0");

        ByteBuffer buffer1= ByteBuffer.allocate(11+sss.length()*2);
        ByteBuffer buffer= ByteBuffer.allocate(10+sss.length()*2);

        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer1.order(ByteOrder.BIG_ENDIAN);
        buffer.put("GZCP".getBytes());
        buffer.put(addr.getBytes());
        if(empty_place>10)
        buffer.put("G".getBytes());
        else
            buffer.put("R".getBytes());
        buffer.put("*".getBytes());
        // buffer.put(empty_placeS.getBytes());
        for(int i=0; i<sss.length();i++) {
            int s0 =Integer.valueOf( sss.charAt(i)+""); // 就是指定索引处的字符值
            byte ss0= (byte)(160+s0);
            buffer.put((byte)0xf8);
            buffer.put(ss0);
        }
        buffer.put("*".getBytes());
        buffer.put("Z".getBytes());
        byte[] array= buffer.array();
        byte check=(byte)0x00;
        int len=array.length;
        for (int i = 0; i < len; i++) {
            if(i==0){
                check=array[0];
            }else {
                check=(byte)(check^array[i]);
            }
        }
        buffer1.put(array);
        buffer1.put((byte)check);
        return sendData.sendDataToScreen(bsid,buffer1.array());

    }

    @Override
    public boolean sendTestToScreen_S(Long bsid) {
        byte[] ss={(byte)0x00};
        return sendData.sendDataToScreen(bsid,ss);
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
                    logger.info("first palce_id"+places.get(0).intValue());
                    logger.info("areaPlace_count:"+places.size());
                    String[] arra=new String[places.size()];
                    String ss="(";
                    int letn=places.size()-1;
                    for (int i = 0; i <places.size() ; i++) {
                        arra[i]=places.get(i)+"";
                        if(i<letn)
                        ss=ss+places.get(i)+",";
                        else
                        ss=ss+places.get(i);
                        logger.info("id:"+places.get(i));
                    }
                     ss=ss+")";

                    //根据区域id查询区域绑定的屏幕编号
                    ShowScreenConfig screen=guideScreenDeviceMapper.selectScreenDeviceByAreaId(areaId);
                    logger.info("areid"+ss);
                    Integer count=parkMapper.selectEmptyCountByPlaces(places);
                    logger.info("getDeviceNum"+screen.getGuideScreenId());
                    logger.info("count"+count);
                    Long bsid= screen.getGuideScreenId();

                 //  sendEmpty_placeToScreen( bsid,count,null,addr);

                    sendEmpty_placeToScreen_S(bsid,count,screen.getScreennum());
                }catch (Exception e){
                    System.out.println(e);
                }

            }

        }
    }
}
