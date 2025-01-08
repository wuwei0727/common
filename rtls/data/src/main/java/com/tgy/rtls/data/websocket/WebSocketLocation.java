package com.tgy.rtls.data.websocket;


import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.park.BeaconCount;
import com.tgy.rtls.data.entity.park.FeeCalcul;
import com.tgy.rtls.data.entity.park.RealTimeData;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkingRecordMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/*
* 与前端交互 用于传输定位数据和报警数据
* */
@Component
@ServerEndpoint("/websocket/location/{uid}")
public class WebSocketLocation {
    private Logger log = Logger.getLogger("WebSocketLocation");
    private static Map<String, Session> clients = new ConcurrentHashMap<>();
    private static Map<String, String> session_uid = new ConcurrentHashMap<>();
    private static WebSocketLocation webSocketLocation;

    @PostConstruct
    public void init(){
        webSocketLocation = this;
    }

    /**
     * 有客户端连接成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid")String uid) throws  IOException {
        clients.put(session.getId(),session);
        session_uid.putIfAbsent(session.getId(),uid);
        log.info("有新客户端连接了"+clients.size());

        sendInitialData(uid);

    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session){
        clients.remove(session.getId());
        session_uid.remove(session.getId());
        log.info("有用户断开了"+session.getId());
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    /**
     * 群发消息
     */
    @OnMessage
    public void onMessage(String message){
        log.info("服务端收到客户端发来的消息"+message);
    }

    /**
     * 发送消息
     */
    public synchronized void sendAll(String message) {
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
                try {
                    String map="";
                    try {
                        JSONObject data = JSONObject.fromObject(message);
                        map = data.getString("map");
                    }catch (Exception e){
                    }
                    String map_value = session_uid.get(sessionEntry.getKey());
                    if(map.equals(map_value))
                        sessionEntry.getValue().getBasicRemote().sendText(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    void sendInitialData(String uid){
        if(uid!=null) {
            ParkingRecordMapper parkingRecordMapper = SpringContextHolder.getBean(ParkingRecordMapper.class);
            BookMapper bookMapper = SpringContextHolder.getBean(BookMapper.class);
            SubMapper subMapper = SpringContextHolder.getBean(SubMapper.class);
            GatewayMapper gatewayMapper = SpringContextHolder.getBean(GatewayMapper.class);
            TagMapper tagMapper = SpringContextHolder.getBean(TagMapper.class);
            ViewMapper viewMapper = SpringContextHolder.getBean(ViewMapper.class);

            JSONObject jsonArea = new JSONObject();
            jsonArea.put("map", uid);

            RealTimeData realTimeData=bookMapper.selectRealTimeData(Integer.valueOf(uid));
            jsonArea.put("data", realTimeData);
            jsonArea.put("type", 9);//车位统计数据
            webSocketLocation.sendAll(jsonArea.toString());
            jsonArea.put("data", realTimeData);
            jsonArea.put("type", 10);//充电桩使用数据
            webSocketLocation.sendAll(jsonArea.toString());
            jsonArea.put("data", realTimeData);
            jsonArea.put("type", 11);//车位预约统计数据
            webSocketLocation.sendAll(jsonArea.toString());

            BeaconCount data = subMapper.findCalcuuByMap(Integer.valueOf(uid));
            jsonArea.put("data", data);
            jsonArea.put("type", 12);//信标统计数据
            webSocketLocation.sendAll(jsonArea.toString());


            BeaconCount loraCount=gatewayMapper.getGateway_loraAcount(Integer.valueOf(uid));
            jsonArea.put("data", loraCount);
            jsonArea.put("type", 13);//网关统计数据
            webSocketLocation.sendAll(jsonArea.toString());

            BeaconCount calculate = tagMapper.getInfraredAcount(Integer.valueOf(uid));
            jsonArea.put("data", calculate);
            jsonArea.put("type", 14);//车位检测设备统计数据
            webSocketLocation.sendAll(jsonArea.toString());

            FeeCalcul ss = parkingRecordMapper.findFeeMap(Integer.valueOf(uid), TimeUtil.getDayStartTimeStr(), TimeUtil.getDayEndTimeStr());
            ss.setMonthlyRentCount(ss.getTotalCount() - ss.getNonMonthlyRentCount());
            jsonArea.put("data", ss);
            jsonArea.put("type", 15);//停车场收费统计数据
            webSocketLocation.sendAll(jsonArea.toString());

            List<ViewVo> useCarFrequency = viewMapper.getRealTimeInAndOutData(uid);
            jsonArea.put("type", 17);
            jsonArea.put("data",useCarFrequency);
            webSocketLocation.sendAll(jsonArea.toString());

        }
    }
}
