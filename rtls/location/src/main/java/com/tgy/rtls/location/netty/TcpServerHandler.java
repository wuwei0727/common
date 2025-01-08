package com.tgy.rtls.location.netty;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.GateWayState;
import com.tgy.rtls.data.entity.equip.Gateway_lora;
import com.tgy.rtls.data.entity.park.GuideScreenDevice;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.mapper.park.GuideScreenDeviceMapper;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.location.Utils.Constant;
import com.tgy.rtls.location.Utils.ServerConstant;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfig;
import com.tgy.rtls.location.config.deviceconfig.BsParaConfigImp;
import com.tgy.rtls.location.config.deviceconfig.ScreenConfig;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.Cmd;
import com.tgy.rtls.location.model.Message;
import com.tgy.rtls.location.model.Screen;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.Attribute;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.tgy.rtls.location.Utils.ByteUtils.printHexString;

public class TcpServerHandler extends SimpleChannelInboundHandler<Object> {
    private MapContainer mapContainer = SpringContextHolder.getBean(MapContainer.class);;
  //  private Executor executor = SpringContextHolder.getBean("threadPool1");;
    private KafukaSender kafukaSender = SpringContextHolder.getBean(KafukaSender.class);;
    private BsParaConfig bsParaConfig = SpringContextHolder.getBean(BsParaConfigImp.class);;
    private GatewayMapper gatewayMapper = SpringContextHolder.getBean(GatewayMapper.class);;
    private GuideScreenDeviceMapper guideScreenDeviceMapper = SpringContextHolder.getBean(GuideScreenDeviceMapper.class);;
    private GatewayService gatewayService = SpringContextHolder.getBean(GatewayService.class);;
    private ScreenConfig ScreenConfig  = SpringContextHolder.getBean(ScreenConfig .class);
    private DeviceAlarmsService deviceAlarmsService  = SpringContextHolder.getBean(DeviceAlarmsService .class);


    private Logger logger= LoggerFactory.getLogger(TcpServerHandler.class);
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();

  /*      executor.execute(new Runnable() {
            @Override
            public void run() {
             //   logger.error("连接：" + channel.remoteAddress()+":"+Thread.currentThread().getName());
            }
        });*/

    }

    //网关离线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
        logger.error("disconnect:" +addr.getAddress().getHostAddress());

        Attribute<String> devIdAttr = channel.attr(ServerConstant.NETTY_CHANNEL_DEVID);
        String devId = devIdAttr.get();

        if(devId != null) {
            mapContainer.all_channel.remove(devId,channel);
            if(devId.startsWith("4G_")){
                String real_id=devId.replace("4G_","");
                logger.error("dis_connect 4G_485_ID:" + real_id);
                List<GuideScreenDevice> guideScreenDevices = guideScreenDeviceMapper.  getAllGuideScreenDeviceByNum(real_id);
                GuideScreenDevice guideScreenDevice=null;
                if (guideScreenDevices != null && guideScreenDevices.size() >= 1) {
                    guideScreenDevice = guideScreenDevices.get(0);
                    guideScreenDevice.setNetworkStatus(0);
                    guideScreenDevice.setUpdateTime(LocalDateTime.now());
                    guideScreenDeviceMapper.updateGuideScreenDevice(guideScreenDevice);
                    logger.error(devId+"set online 0:"+guideScreenDevice.getId());
                }
            }else  if(devId.startsWith("CAT1_")){
                String real_id=devId.replace("CAT1_","");
                logger.error("dis_connect CAT1_ID:" + real_id);
            /*    List<GuideScreenDevice> guideScreenDevices = guideScreenDeviceMapper.  getAllGuideScreenDeviceByNum(real_id);
                GuideScreenDevice guideScreenDevice=null;
                if (guideScreenDevices != null && guideScreenDevices.size() >= 1) {
                    guideScreenDevice = guideScreenDevices.get(0);
                    guideScreenDevice.setNetworkStatus(0);
                    guideScreenDevice.setUpdateTime(LocalDateTime.now());
                    guideScreenDeviceMapper.updateGuideScreenDevice(guideScreenDevice);
                    logger.error(devId+"set online 0:"+guideScreenDevice.getId());
                }*/
            }else{
                logger.error("disconnect ID:" + devId);
                List<Gateway_lora> gateways = gatewayMapper.findGateway_loraByNum(devId);
                GateWayState gateWayState = new GateWayState();
                gateWayState.setGatewaynum(devId + "");
                gateWayState.setState(0);
                gatewayService.addGateWayState(gateWayState);

                Gateway_lora gateway;
                if (gateways != null && gateways.size() >= 1) {
                    gateway = gateways.get(0);
                    gateway.setNetworkstate((short) 0);
                    gateway.setBatteryTime(LocalDateTime.now());
                    gateway.setOfflineTime(LocalDateTime.now());
                    gatewayMapper.updateGateway_Lora(gateway);

                    logger.error(devId + "offline:");
                    gateway.setNetworkName("离线");
                    kafukaSender.send(KafukaTopics.LORA_STATE, gateway.toString());

                }
            }
        }

    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg == null)
            return;

        try {
            Message message = (Message) msg;
            String devId = message.getBsId()+"";
            Channel channel = ctx.channel();

            byte[] cmd=message.getHeader().getCmd();

            String cmd_type= new Cmd(cmd).getCmd();
            logger.info("cmd"+cmd_type);
            logger.info("devId"+devId);
            logger.info("length"+printHexString(message.getHeader().getLength()));

            if (Constant.list_GW.contains(cmd_type)) {
                System.out.println(cmd_type + " list_GW.");
                processLora(channel,devId);
            } else if(Constant.list_485.contains(cmd_type)) {
                System.out.println(cmd_type + " list_485");
                process4G_485(channel,devId);
            }else if(Constant.list_CAT1.contains(cmd_type)){
                System.out.println(cmd_type + " list_cat1");
                processCAT1(channel,devId);
            }


     //   Executor executor = SpringContextHolder.getBean("threadPool1");;
     //  executor.execute(new MessageReadTask(message,channel));
             MessageReadTask.MessageReadTask1(message,channel);



        } catch(Exception e) {
            logger.error(e.getMessage(), e.getStackTrace());
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        Channel channel = ctx.channel();

        Attribute<String> devIdAttr = channel.attr(ServerConstant.NETTY_CHANNEL_DEVID);//获取自定义属性设备ID
        String currentDevId = devIdAttr.get();//读取自定义属性设备ID
        ctx.channel().close();
        mapContainer.all_channel.remove(currentDevId);
        logger.error(currentDevId+"----overTime");
        if(evt instanceof IdleStateEvent) {

            ctx.close().addListener(ChannelFutureListener.CLOSE_ON_FAILURE);// 关闭连接
        } else {
            // 传递给下一个处理程序
            super.userEventTriggered(ctx, evt);
        }

    }


    //网关在线
    void processLora( Channel channel,  String devId){

        logger.info("lora_devId"+devId);
        InetSocketAddress addssr = (InetSocketAddress) channel.remoteAddress();
        Attribute<String> devIdAttr = channel.attr(ServerConstant.NETTY_CHANNEL_DEVID);//获取自定义属性设备ID
        String currentDevId = devIdAttr.get()+"";//读取自定义属性设备ID
        List<Gateway_lora> gateways = gatewayMapper.findGateway_loraByNum( devId);
        Gateway_lora gateway= gateways.get(0);
        if(currentDevId == null||currentDevId.equals("null")) {
            logger.info("devid:"+devId+":currentDevId:"+currentDevId+"----addr"+addssr.getAddress());
            BsState bsState=new BsState();
            bsState.setErrorCode((short)2);
            bsState.setState((short) 0);
            bsState.setTime(new Date().getTime());
            bsState.setBsid(devId+"");
            InetSocketAddress addr = (InetSocketAddress) channel.remoteAddress();
            bsState.setIp(addr.getAddress().getHostAddress());
            kafukaSender.send(KafukaTopics.BS_STATE,bsState.toString());
            devIdAttr.set((devId));
            if(mapContainer.all_channel.containsKey(devId))
                mapContainer.all_channel.replace(devId,channel);
            else
                mapContainer.all_channel.put(devId, channel);//设置自定义属性设备ID
            InetSocketAddress addr1 = (InetSocketAddress) channel.remoteAddress();
            logger.info("devid:"+devId+":ip:"+addr1.getAddress().getHostAddress());
            if(gateways!=null&&gateways.size()>=1) {
//                网关在线
                gateway=gateways.get(0);
                gateway.setNetworkstate((short)1);
                gateway.setIp(addr1.getAddress().getHostAddress());
                logger.error(devId+"set online 1");
                gatewayMapper.updateGateway_Lora(gateway);
                gateway.setNetworkName("在线");
            }else {
                gateway=new Gateway_lora();
                gateway.setNum(devId+"");
                gateway.setIp(addr1.getAddress().getHostAddress());
                gateway.setNetworkstate((short)1);
                gatewayMapper.addGatewayLora(gateway);
                logger.info("add gateway"+devId);
            }
            GateWayState gateWayState=new GateWayState();
            gateWayState.setGatewaynum(devId+"");
            gateWayState.setState(1);
            gatewayService.addGateWayState(gateWayState);
            kafukaSender.send(KafukaTopics.LORA_STATE,gateway.toString());

        }else{

            if( !StringUtils.equals(currentDevId, devId)){
                mapContainer.all_channel.putIfAbsent(devId,channel);

                 /*   if(mapContainer.all_channel_id.containsKey(devId))
                        mapContainer.all_channel_id.replace(devId,channel);
                    else
                        mapContainer.all_channel_id.put(devId, channel);//设置自定义属性设备ID*/
                logger.info("devid:"+devId+":currentDevId:"+currentDevId+"--replace--addr"+addssr.getAddress());
            }
            if(mapContainer.all_channel.containsKey(currentDevId)){
                mapContainer.all_channel.replace(currentDevId,channel);
                logger.info("devid:"+devId+":currentDevId:"+currentDevId+"--continue--addr"+addssr.getAddress());
            }else{
                mapContainer.all_channel.put(currentDevId, channel);//设置自定义属性设备ID
                InetSocketAddress addr1 = (InetSocketAddress) channel.remoteAddress();
                logger.info("devid:"+devId+":ip:"+addr1.getAddress().getHostAddress());
                if(gateways!=null&&gateways.size()>=1) {
                    gateway=gateways.get(0);
                    gateway.setNetworkstate((short)1);
                    gateway.setIp(addr1.getAddress().getHostAddress());
                    logger.error(devId+"set online 1");
                    gatewayMapper.updateGateway_Lora(gateway);
                    gateway.setNetworkName("在线");
                }
            }
        }
        DeviceAlarms infraredDevice = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                .eq("equipment_type",2)
                .eq("alarm_type",1)
                .eq("state", 0)
                .eq("device_id", gateway.getId())
                .isNull("end_time"));

        if(!NullUtils.isEmpty(infraredDevice)) {
            LocalDateTime now = LocalDateTime.now();
            LambdaUpdateWrapper<DeviceAlarms> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(DeviceAlarms::getState, 1)
                    .set(DeviceAlarms::getEndTime, now)
                    .eq(DeviceAlarms::getEquipmentType, 2)
                    .eq(DeviceAlarms::getAlarmType, 1)
                    .eq(DeviceAlarms::getState, 0)
                    .eq(DeviceAlarms::getDeviceId, gateway.getId())
                    .isNull(DeviceAlarms::getEndTime);
            deviceAlarmsService.update(null, lambdaUpdateWrapper);
        }
        devIdAttr.set((devId));
        // ChannelId chId = channel.id();
        //	GisUtils.writeToFile(new Timestamp(new Date().getTime()).toString()+";" + devId+";设备连接正常，接收到数据帧ID"+"CMD:"+message.getHeader().getFrameType()+":帧ID:"+message.getFrameId() );

    }
    void processCAT1( Channel channel, String devId){
        if(devId==null){
            return;
        }
        logger.info("CAT1_devId"+devId);
       String  new_devId="CAT1_"+devId;
        InetSocketAddress addssr = (InetSocketAddress) channel.remoteAddress();
        Attribute<String> devIdAttr = channel.attr(ServerConstant.NETTY_CHANNEL_DEVID);//获取自定义属性设备ID
        String currentDevId = devIdAttr.get();//读取自定义属性设备ID
        if(currentDevId == null||currentDevId.equals("null")) {
            logger.info("devid:"+devId+":currentDevId:"+currentDevId+"----addr"+addssr.getAddress());


            devIdAttr.set((new_devId));
            if(mapContainer.all_channel.containsKey(new_devId))
                mapContainer.all_channel.replace(new_devId,channel);
            else
                mapContainer.all_channel.put(new_devId, channel);//设置自定义属性设备ID

            InetSocketAddress addr1 = (InetSocketAddress) channel.remoteAddress();
            logger.info("cat1devid:"+devId+":ip:"+addr1.getAddress().getHostAddress());

          /*  GateWayState gateWayState=new GateWayState();
            gateWayState.setGatewaynum(devId+"");
            gateWayState.setState(1);
            gatewayService.addGateWayState(gateWayState);
            kafukaSender.send(KafukaTopics.LORA_STATE,gateway.toString());*/

        }else{

            logger.info("currentDevId:"+currentDevId+":485new_devId:"+new_devId);
            if(!StringUtils.equals(currentDevId, new_devId)){
                mapContainer.all_channel.putIfAbsent(new_devId,channel);

                 /*   if(mapContainer.all_channel_id.containsKey(devId))
                        mapContainer.all_channel_id.replace(devId,channel);
                    else
                        mapContainer.all_channel_id.put(devId, channel);//设置自定义属性设备ID*/
                logger.info("devid:"+devId+":485currentDevId:"+currentDevId+"--replace--addr"+addssr.getAddress());
            }
            if(mapContainer.all_channel.containsKey(new_devId)){
                mapContainer.all_channel.replace(new_devId,channel);
                logger.info("devid:"+devId+":485currentDevId:"+currentDevId+"--continue--addr"+addssr.getAddress());
            }else{
                mapContainer.all_channel.put(new_devId, channel);//设置自定义属性设备ID

            }
        }
        devIdAttr.set((new_devId));

    }

    void process4G_485( Channel channel, String devId){
        if(devId==null){
            return;
        }
        logger.info("4G_devId"+devId);
       String  new_devId="4G_"+devId;
        InetSocketAddress addssr = (InetSocketAddress) channel.remoteAddress();
        Attribute<String> devIdAttr = channel.attr(ServerConstant.NETTY_CHANNEL_DEVID);//获取自定义属性设备ID
        String currentDevId = devIdAttr.get();//读取自定义属性设备ID
        if(currentDevId == null||currentDevId.equals("null")) {
            logger.info("devid:"+devId+":currentDevId:"+currentDevId+"----addr"+addssr.getAddress());
            GuideScreenDevice guideScreenDevice;

            devIdAttr.set((new_devId));
            if(mapContainer.all_channel.containsKey(new_devId))
                mapContainer.all_channel.replace(new_devId,channel);
            else
                mapContainer.all_channel.put(new_devId, channel);//设置自定义属性设备ID
            List<GuideScreenDevice> guideScreenDevices = guideScreenDeviceMapper.getAllGuideScreenDeviceByNum(devId);
            ;
            InetSocketAddress addr1 = (InetSocketAddress) channel.remoteAddress();
            logger.info("485devid:"+devId+":ip:"+addr1.getAddress().getHostAddress());
            Long device_id=Long.valueOf(devId);
            Screen screen_count = mapContainer.device_Screen.get(device_id);
            if(screen_count!=null){
                Iterator entrys = screen_count.screenName_count.entrySet().iterator();
                while (entrys.hasNext()) {
                    Map.Entry entry = (Map.Entry) entrys.next();
                    String screen = (String)entry.getKey();
                    Integer count = (Integer) entry.getValue();
                    ScreenConfig.sendEmpty_placeToScreen_S(device_id,count,screen);
                }
            }else{
                mapContainer.device_Screen.put(device_id,new Screen());
            }

            if(guideScreenDevices!=null&&guideScreenDevices.size()>=1) {
                guideScreenDevice=guideScreenDevices.get(0);
                guideScreenDevice.setNetworkStatus(1);
                guideScreenDevice.setIp(addr1.getAddress().getHostAddress());
                guideScreenDevice.setUpdateTime( LocalDateTime.now());
                logger.error(devId+"set online 1:"+guideScreenDevice.getId());
                guideScreenDeviceMapper.updateGuideScreenDevice(guideScreenDevice);
            }else {
                guideScreenDevice= new GuideScreenDevice();
                guideScreenDevice.setDeviceId(Long.valueOf(devId));
                guideScreenDevice.setIp(addr1.getAddress().getHostAddress());
                guideScreenDevice.setNetworkStatus(1);
                guideScreenDeviceMapper.addGuideScreenDevice(guideScreenDevice);
                logger.info("add 4g_RS485"+devId);
            }
          /*  GateWayState gateWayState=new GateWayState();
            gateWayState.setGatewaynum(devId+"");
            gateWayState.setState(1);
            gatewayService.addGateWayState(gateWayState);
            kafukaSender.send(KafukaTopics.LORA_STATE,gateway.toString());*/

        }else{

            logger.info("currentDevId:"+currentDevId+":485new_devId:"+new_devId);
            if(!StringUtils.equals(currentDevId, new_devId)){
                mapContainer.all_channel.putIfAbsent(new_devId,channel);

                 /*   if(mapContainer.all_channel_id.containsKey(devId))
                        mapContainer.all_channel_id.replace(devId,channel);
                    else
                        mapContainer.all_channel_id.put(devId, channel);//设置自定义属性设备ID*/
                logger.info("devid:"+devId+":485currentDevId:"+currentDevId+"--replace--addr"+addssr.getAddress());
            }
            if(mapContainer.all_channel.containsKey(new_devId)){
                mapContainer.all_channel.replace(new_devId,channel);
                logger.info("devid:"+devId+":485currentDevId:"+currentDevId+"--continue--addr"+addssr.getAddress());
            }else{
                mapContainer.all_channel.put(new_devId, channel);//设置自定义属性设备ID
                List<GuideScreenDevice> guideScreenDevices = guideScreenDeviceMapper.getAllGuideScreenDeviceByNum(devId);
                ;    GuideScreenDevice guideScreenDevice;
                if(guideScreenDevices!=null&&guideScreenDevices.size()>=1) {
                    guideScreenDevice=guideScreenDevices.get(0);
                    guideScreenDevice.setNetworkStatus(1);
                    guideScreenDevice.setUpdateTime( LocalDateTime.now());
                    logger.error(devId+"set online 1:"+guideScreenDevice.getId());
                    guideScreenDeviceMapper.updateGuideScreenDevice(guideScreenDevice);
                }
            }
        }
        devIdAttr.set((new_devId));
        // ChannelId chId = channel.id();
        //	GisUtils.writeToFile(new Timestamp(new Date().getTime()).toString()+";" + devId+";设备连接正常，接收到数据帧ID"+"CMD:"+message.getHeader().getFrameType()+":帧ID:"+message.getFrameId() );

    }




}
