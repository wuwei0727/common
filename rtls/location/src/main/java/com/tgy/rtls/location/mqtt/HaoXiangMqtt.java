package com.tgy.rtls.location.mqtt;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.MqttTopic;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.kafukaentity.HaoXiang;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagSensor;
import net.sf.json.JSONObject;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@DependsOn("dataSpringContext")
public class HaoXiangMqtt implements MqttClientInterface, MqttCallback {
    @Value("${haoxiang.ip}")
    public String ip;
    @Value("${haoxiang.user}")
    public String user;
    @Value("${haoxiang.pwd}")
    public String pwd;
    public MqttClient client;
    private Integer gatewayId;
    public volatile String deviceIp = "";
    private MqttConnectOptions options;
    private Logger log = LoggerFactory.getLogger(this.getClass());


        public  HaoXiangMqtt(){
            Executor executor = SpringContextHolder.getBean("threadPool1");
              executor.execute(new Runnable() {
                  @Override
                  public void run() {

                      start(null);
                  }
              });
        }


    @Override
    public void startReconnect() {
                 start(null);
    }

    @Override
    public void start(Integer id) {
                log.error("浩翔开始重连");

        // String clientid = UUID.randomUUID().toString();

                // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
                try {
                    String HOST = "tcp://" + ip;
                    log.error("浩翔开始重连"+HOST);

              // MQTT的连接设置
                options = new MqttConnectOptions();
                // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
                options.setCleanSession(true);
                // 设置连接的用户名
                options.setUserName(user);
                // 设置连接的密码
                options.setPassword(pwd.toCharArray());

                // 设置超时时间 单位为秒
                options.setConnectionTimeout(10);
                // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
                options.setKeepAliveInterval(20);
                client = new MqttClient("tcp://"+ip, "90bbebb7f21cb214104bfeb4bb0ae58ff205", new MemoryPersistence());
                client.setCallback(this);
                client.connect(options);
                //订阅消息
             //   int[] Qos = {1, 1, 1};
                //String[] topic1 = {MqttTopic.TOPIC_DW1001, MqttTopic.TOPIC_SUB1G, MqttTopic.TOPIC_LOCATION + "#"};
               // client.subscribe(topic1, Qos);
                log.error("浩翔重连结束");

            } catch (Exception e) {
                e.printStackTrace();
                   /// startReconnect();
            }


    }

    @Override
    public void disconnect() {

    }

    public 	void publishLocationData(String tagid, TagLocation tagLocation){
      //  log.info("publishLocationData"+tagid+"::"+data);
        HaoXiang haoXiang=new HaoXiang();
        haoXiang.setData_type("event");
        haoXiang.setStream_id("taglocation");
        JSONObject jsonObject=new JSONObject();
        haoXiang.setData(LocalUtil.haoXiangDataProcessJson( LocalUtil.getKeyAndValueAnnotation(tagLocation)));
       log.info("publishLocationData"+tagid+"::"+haoXiang.toString());

        if(client!=null&&client.isConnected())
        {
            Executor executor = SpringContextHolder.getBean("threadPool1");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MqttMessage message1 = new MqttMessage();
                        message1.setPayload(haoXiang.toString().getBytes());
                        message1.setQos(0);
                        message1.setRetained(true);
                        client.publish(MqttTopic.TOPIC_LOCATION_HAOXIANG, message1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public 	void publishSensorData(String tagid, TagSensor tagSensor){
         //   log.info("publishSensorData"+tagid+"::"+data);
        HaoXiang haoXiang=new HaoXiang();
        haoXiang.setData_type("event");
        haoXiang.setStream_id("tagsensor");
        haoXiang.setData(( LocalUtil.haoXiangDataProcessJson( LocalUtil.getKeyAndValueAnnotation(tagSensor))));
       log.info("publishSensorData"+tagid+"::"+haoXiang.toString());
        if(client!=null&&client.isConnected())
        {
            Executor executor = SpringContextHolder.getBean("threadPool1");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MqttMessage message1 = new MqttMessage();
                        message1.setPayload(haoXiang.toString().getBytes());
                        message1.setQos(0);
                        message1.setRetained(true);
                     client.publish(MqttTopic.TOPIC_SENSOR_TAG_HAOXIANG, message1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void publishBsLocation(Basestation base) {

    }

    @Override
    public void publishBsStateData(String bsid, BsState bsState) {
    //    log.info("publishBsStateData"+bsid+"::"+data);
        HaoXiang haoXiang=new HaoXiang();
        haoXiang.setData_type("event");
        haoXiang.setStream_id("bsstate");
        JSONObject jsonObject=new JSONObject();
        haoXiang.setData(( LocalUtil.haoXiangDataProcessJson( LocalUtil.getKeyAndValueAnnotation(bsState))));
        log.info("publishBsStateData"+bsid+"::"+haoXiang.toString());
        if(client!=null&&client.isConnected())
        {
            Executor executor = SpringContextHolder.getBean("threadPool1");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MqttMessage message1 = new MqttMessage();
                        message1.setPayload(haoXiang.toString().getBytes());
                        message1.setQos(0);
                        message1.setRetained(true);
                        client.publish(MqttTopic.TOPIC_BSSTATE_HAOXIANG, message1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public void connectionLost(Throwable cause) {

    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {

    }
}