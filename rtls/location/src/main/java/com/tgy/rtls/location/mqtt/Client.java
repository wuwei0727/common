package com.tgy.rtls.location.mqtt;

import com.tgy.rtls.data.common.MqttTopic;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagSensor;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.location.kafuka.KafukaListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/*
* 客户端接收消息
* */
public class Client implements MqttClientInterface {

	public MqttClient client;
	private Integer gatewayId;
	public volatile String ip="";
	private MqttConnectOptions options;
	public volatile boolean reconnectFlag=true;
	private GatewayService gatewayService= SpringContextHolder.getBean("gatewayServiceImpl");
	private KafukaListener kafukaListener= SpringContextHolder.getBean("dasdsad");
	private ScheduledExecutorService scheduler=Executors.newSingleThreadScheduledExecutor();
	private Logger log = LoggerFactory.getLogger(Client.class);
	//重新链接
	public void startReconnect() {
		if(reconnectFlag) {
			if (!client.isConnected()) {
				try {
					log.error("重连...");
					this.start(gatewayId);
				} catch (Exception e) {
					log.error(e.getMessage(), e);
				}
			}
		}
/*		scheduler.execute(new Runnable(Client cl) {
			@Override
			public void run() {
				{
					if (!client.isConnected()) {
						try {
							client.
						} catch (Exception e) {
							log.error(e.getMessage(),e);
						}
					}
				}
			}
		});*/
	}
	public void  start(Integer id) {
		this.gatewayId=id;
	/*	scheduler.execute(new Runnable() {
			@Override
			public void run() {*/
			 try {
				 log.error("开始重连");
				 String clientid=UUID.randomUUID().toString();
				 Gateway_uwb gateway_uwb=gatewayService.findById(id);
				 ip=gateway_uwb.getIp();
				 if(NullUtils.isEmpty(gateway_uwb)){
					 return ;
				 }
				 String HOST = "tcp://"+gateway_uwb.getIp();
				 String userName=gateway_uwb.getUsername();
				 String passWord=gateway_uwb.getPassword();
				 // host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
				 client = new MqttClient(HOST, clientid, new MemoryPersistence());

				 // MQTT的连接设置
				 options = new MqttConnectOptions();
				 // 设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
				 options.setCleanSession(true);
				 // 设置连接的用户名
				 options.setUserName(userName);
				 // 设置连接的密码
				 options.setPassword(passWord.toCharArray());
				 // 设置超时时间 单位为秒
				 options.setConnectionTimeout(10);
				 // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
				 options.setKeepAliveInterval(20);
				 // 设置回调
	/*		scheduler.execute(new Runnable() {
				@Override
				public void run() {*/

				 client.setCallback(new PushCallback(id));
		/*		}
			});*/



			/*MqttTopic topic = client.getTopic(TOPIC);
			//setWill方法，如果项目中需要知道客户端是否掉线可以调用该方法。设置最终端口的通知消息
			options.setWill(topic, "close".getBytes(), 0, true);*/

				 client.connect(options);
				 //订阅消息
				 int[] Qos  = {1,1,1};
				 String[] topic1 = {MqttTopic.TOPIC_DW1001,MqttTopic.TOPIC_SUB1G,MqttTopic.TOPIC_LOCATION+"#"};
				 client.subscribe(topic1, Qos);
				 log.error("重连结束");
				 //gatewayService.updateGatewayConnect(id,1);
			 } catch (Exception e) {
			 	if(reconnectFlag) {
					log.error("继续重连");
					try {
						Thread.sleep(15000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					startReconnect();
				}
/*			IMqttToken iMqttToken=new IMqttToken() {
				@Override
				public void waitForCompletion() throws MqttException {

				}

				@Override
				public void waitForCompletion(long l) throws MqttException {

				}

				@Override
				public boolean isComplete() {
					return false;
				}

				@Override
				public MqttException getException() {
					return null;
				}

				@Override
				public void setActionCallback(IMqttActionListener iMqttActionListener) {

				}

				@Override
				public IMqttActionListener getActionCallback() {
					return null;
				}

				@Override
				public IMqttAsyncClient getClient() {
					return null;
				}

				@Override
				public String[] getTopics() {
					return new String[0];
				}

				@Override
				public void setUserContext(Object o) {

				}

				@Override
				public Object getUserContext() {
					return id;
				}

				@Override
				public int getMessageId() {
					return 0;
				}

				@Override
				public int[] getGrantedQos() {
					return new int[0];
				}

				@Override
				public boolean getSessionPresent() {
					return false;
				}

				@Override
				public MqttWireMessage getResponse() {
					return null;
				}
			};
			iMqttActionListener.onFailure(iMqttToken, e);
			log.error(e.getMessage(),e);
			*/

				// return false;
			 }
			// return true;
	/*	 }
	 });*/

	}

	public void disconnect() {
		try {
			client.disconnectForcibly();
			//client.disconnect();
		 	client.close();
		}catch(Exception e) {
			log.error(e.getMessage(),e);
		}
	}

	@Override
	public void publishLocationData(String tagid, TagLocation tagLocation) {

	}

	@Override
	public void publishSensorData(String tagid, TagSensor tagSensor) {

	}

	/*// MQTT是否连接成功
	private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

		@Override
		public void onSuccess(IMqttToken arg0) {
			try {
				// 订阅myTopic话题
				String[] topic1 = {TOPIC_DW1001,TOPIC_SUB1G};
				int[] qos={1,1};
				client.subscribe(topic1,qos);
				gatewayService.updateGatewayConnect((Integer) arg0.getUserContext(),1);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(IMqttToken arg0, Throwable arg1) {
			arg1.printStackTrace();
			// 连接失败，重连
			Integer clientid = (Integer) arg0.getUserContext();
			Integer type = kafukaListener.gatewayList.get(clientid);
			gatewayService.updateGatewayConnect((Integer) arg0.getUserContext(),0);
			if(type!=null&&type.intValue()==1){
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				startReconnect();
			start((Integer) arg0.getUserContext());
		   }
		}
	};*/
	public static void main(String[] args) throws MqttException {
	}
public 	void publishLocationData(String tagid,String data){


	}

	public 	void publishSensorData(String tagid,String data){

	}



    public 	void publishBsLocation(Basestation base){

		org.json.JSONObject all=new org.json.JSONObject();
		org.json.JSONObject configuration=new org.json.JSONObject();
		configuration.put("label","DW"+base.getNum().toUpperCase());
		configuration.put("nodeType","ANCHOR");
		configuration.put("ble",true);
		configuration.put("leds",true);
		configuration.put("uwbFirmwareUpdate",false);
		org.json.JSONObject pos=new org.json.JSONObject();
		pos.put("x",base.getX());
		pos.put("y",base.getY());
		pos.put("z",base.getZ());
		pos.put("quality",100);
		org.json.JSONObject anchor=new org.json.JSONObject();
		anchor.put("initiator",base.getInitiator()==1?true:false);
		anchor.put("position",pos);
		anchor.put("routingConfig","ROUTING_CFG_OFF");
		configuration.put("anchor",anchor);
		all.put("configuration",configuration);
        if(client.isConnected())
        {
            scheduler.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        MqttMessage message1 = new MqttMessage();
                        message1.setPayload(all.toString().getBytes());
                        message1.setQos(1);
                        message1.setRetained(true);
                        String topic = "dwm/node/" + base.getNum() + "/downlink/config";
                        client.publish(topic, message1);
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

	@Override
	public void publishBsStateData(String bsid, BsState bsState) {

	}


}
