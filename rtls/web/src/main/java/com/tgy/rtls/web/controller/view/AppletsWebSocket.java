package com.tgy.rtls.web.controller.view;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import lombok.extern.slf4j.Slf4j;
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

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.view
 * @Author: wuwei
 * @CreateTime: 2022-09-19 18:31
 * @Description: TODO
 * @Version: 1.0
 */
//与前端交互 用户小程序使用情况 车位实时使用记录

@Component
//@ServerEndpoint("/appletsWebSocket/{uid}/{time}")
@ServerEndpoint("/appletsWebSocket/{uid}")
@Slf4j
public class AppletsWebSocket {
    private static Map<String, Session> clients = new ConcurrentHashMap<>();
    private static Map<String, String> session_uid = new ConcurrentHashMap<>();
    private static AppletsWebSocket appletsWebSocket;

    @PostConstruct
    public void init(){
        appletsWebSocket = this;
    }

    @OnOpen
    public void onOpen(Session session,@PathParam("uid") String uid) throws Exception {
        clients.put(session.getId(),session);
        session_uid.putIfAbsent(session.getId(),uid);
        log.info("有新客户端连接了"+clients.size());
        sendInitialData(uid);
        //客户端第一次连接

    }

    @OnClose
    public void onClose(Session session){
        clients.remove(session.getId());
        session_uid.remove(session.getId());
        log.info("有用户断开了"+session.getId());
    }

    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message){
        log.info("服务端收到客户端发来的消息"+message);
    }

    public synchronized void sendAll(String message) {
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
            try {
                String map="";
                try {
                    JSONObject data = JSONObject.fromObject(message);
                    log.info(data.toString());
                    map = data.getString("uid");
                }catch (Exception e){
                }
                String map_value = session_uid.get(sessionEntry.getKey());
                if(map.equals(map_value)) {
                    sessionEntry.getValue().getBasicRemote().sendText(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private void sendInitialData(String uid,String time) {
    private void sendInitialData(String uid) {
        if("-1".equals(uid)) {
            ViewMapper viewMapper = SpringContextHolder.getBean(ViewMapper.class);
            JSONObject jsonArea = new JSONObject();
            jsonArea.put("uid", uid);

//            List<ViewVo> recommendCarFrequency = viewMapper.getRecommendCarFrequency();
//            jsonArea.put("type", 30);
//            jsonArea.put("data",recommendCarFrequency);
//            appletsWebSocket.sendAll(jsonArea.toString());

//            List<ViewVo> findCarFrequency = viewMapper.getFindCarFrequency();
//            jsonArea.put("type", 28);//27进出实时更新
//            jsonArea.put("data",findCarFrequency);
//            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> userAllInfo = viewMapper.getAllUserInfo(null,null,null);
            jsonArea.put("type", 20);
            jsonArea.put("data",userAllInfo);//用户统计信息：用户总数
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> userTotalNumByMonth = viewMapper.getAllUserTotalNumByMonth();
            jsonArea.put("type", 21);
            jsonArea.put("data",userTotalNumByMonth);//月活用户数
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
            jsonArea.put("type", 22);
            jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> merchantAndFirm = viewMapper.getSettledMerchantAndFirmInfo();
            jsonArea.put("type", 23);
            jsonArea.put("data",merchantAndFirm);//入驻商家公司
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> useFrequency = viewMapper.getCumulativeUseFrequency(null,null,null);
            jsonArea.put("type", 24);
            jsonArea.put("data",useFrequency);
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> singleMapParkingState = viewMapper.getSingleMapParkingStateInfo();
            jsonArea.put("type", 25);
            jsonArea.put("data",singleMapParkingState);
            appletsWebSocket.sendAll(jsonArea.toString());

//            List<ViewVo> realTimeInAndOutData = viewMapper.getRealTimeInAndOutData();
            List<ViewVo> realTimeInAndOutData = viewMapper.getRealTimeInAndOutData1();
            jsonArea.put("type", 26);
            jsonArea.put("data",realTimeInAndOutData);
            appletsWebSocket.sendAll(jsonArea.toString());

            List<ViewVo> useCarFrequency = viewMapper.getUseCarFrequency();
            // List<ViewVo> useCarFrequency =viewService.getUseCarFrequency().get();
            jsonArea.put("type", 29);
            jsonArea.put("data",useCarFrequency);
            appletsWebSocket.sendAll(jsonArea.toString());




            //***********************************************************************************


//            List<ViewVo> monthActiveUser = viewMapper.getMonthActiveUser(month);
//            jsonArea.put("type", 30);
//            jsonArea.put("data",monthActiveUser);
//            appletsWebSocket.sendAll(jsonArea.toString());
        }
    }
}
