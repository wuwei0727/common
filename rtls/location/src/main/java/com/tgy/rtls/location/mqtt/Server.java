package com.tgy.rtls.location.mqtt;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.service.equip.GatewayService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

/*
 *服务端发布消息
 * */
@Slf4j
public class Server {
    private GatewayService gatewayService= SpringContextHolder.getBean("gatewayService");
    private static final String clientid = UUID.randomUUID().toString();
    private MqttClient client;
    private MqttTopic topic;
    public MqttMessage message;

    public Boolean Server(String TOPIC, Integer id) throws MqttException {
        //MemoryPersistence设置clientid的保存形式，默认为以内存保存
        Gateway_uwb gateway_uwb = gatewayService.findById(id);
        if(NullUtils.isEmpty(gateway_uwb)){
            return false;
        }
        String HOST = "tcp://" + gateway_uwb.getIp();
        client = new MqttClient(HOST, clientid, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setUserName(gateway_uwb.getUsername());
        options.setPassword(gateway_uwb.getPassword().toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        try {
            // 设置回调
            client.setCallback(new PushCallback(id));
            client.connect(options);
            topic = client.getTopic(TOPIC);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public void publish(MqttMessage message) throws MqttPersistenceException, MqttException {
        MqttDeliveryToken token = topic.publish(message);
        token.waitForCompletion();
    }

    public static void main(String[] args) throws MqttException {
    }
}

