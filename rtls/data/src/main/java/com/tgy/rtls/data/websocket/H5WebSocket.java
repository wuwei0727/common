package com.tgy.rtls.data.websocket;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.websocket
 * @Author: wuwei
 * @CreateTime: 2023-07-04 09:19
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
@ServerEndpoint(value = "/websocket/h5WebSocket/{uid}")
public class H5WebSocket {
    private static Map<String, Session> clients = new ConcurrentHashMap<>();
    private static Map<String, String> session_uid = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        H5WebSocket h5WebSocket = this;
    }

    /**
     * 有客户端连接成功
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) throws IOException {
        // log.error("uid → session={},uid={}",uid);
        // Find all session ids with the same uid
        List<String> existingSessionIds = session_uid.entrySet().stream()
                .filter(entry -> entry.getValue().equals(uid))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        // Remove all existing sessions with the same uid from clients and session_uid
        for (String sessionId : existingSessionIds) {
//            clients.remove(sessionId);
//            session_uid.remove(sessionId);
            Session s0= clients.get(sessionId);
            if(s0!=null) {
                s0.close();
            }
        }
        // System.out.println("已经连接的数量"+existingSessionIds.size());
        // 保存最新的连接
        clients.put(session.getId(), session);
        session_uid.put(session.getId(), uid);
        // log.error("H5-sessionId"+session.getId());
        // log.error("H5-session_uid"+ session_uid.entrySet());
        // log.info("H5-有新客户端连接了" + clients.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        clients.remove(session.getId());
        session_uid.remove(session.getId());
        log.info("有用户断开了" + session.getId());
    }

    /**
     * 发生错误
     */
    @OnError
    public void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    /**
     * 群发消息
     */
    @OnMessage
    public void onMessage(String message,Session session) {
      //  log.info("服务端收到客户端发来的消息" + message);
        String uid = session_uid.get(session.getId());
        // 发送消息到目标WebSocket连接
        if(!NullUtils.isEmpty(message)){
            sendInitialData(message,uid);
        }
    }

    /**
     * 发送消息
     */
    public synchronized void sendAll(Object message, String uid) {
        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
            try {
                if(!NullUtils.isEmpty(uid)){
                    String map_value = session_uid.get(sessionEntry.getKey());
                    if (uid.equals(map_value)) {
                        sessionEntry.getValue().getBasicRemote().sendText(message.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    void sendInitialData(Object message, String uid){
        if(!NullUtils.isEmpty(message)&&!NullUtils.isEmpty(uid)) {
            UserWebSocket userWebSocket = SpringContextHolder.getBean(UserWebSocket.class);
            userWebSocket.sendAll(message,uid);
        }
    }
}