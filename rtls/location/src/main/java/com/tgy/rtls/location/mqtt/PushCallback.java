package com.tgy.rtls.location.mqtt;


import com.tgy.rtls.data.algorithm.*;
import com.tgy.rtls.data.algorithm.crosspoint.CGGeometryLib;
import com.tgy.rtls.data.algorithm.crosspoint.CGPoint;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.entity.common.Point2d;
import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.entity.equip.TagBeacon;
import com.tgy.rtls.data.entity.equip.TagPara;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagSensor;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.location.kafuka.KafukaSender;
import com.tgy.rtls.location.model.Bslr_dis;
import com.tgy.rtls.location.model.DisInf;
import com.tgy.rtls.location.model.LocFiterRes;
import com.tgy.rtls.location.model.TagInf;
import com.tgy.rtls.location.netty.DataProcess;
import com.tgy.rtls.location.netty.MapContainer;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * 发布消息的回调类
 *
 * 必须实现MqttCallback的接口并实现对应的相关接口方法
 *      ?CallBack 类将实现 MqttCallBack。每个客户机标识都需要一个回调实例。在此示例中，构造函数传递客户机标识以另存为实例数据。在回调中，将它用来标识已经启动了该回调的哪个实例。
 *  ?必须在回调类中实现三个方法：
 *
 *  public void messageArrived(MqttTopic topic, MqttMessage message)
 *  接收已经预订的发布。
 *
 *  public void connectionLost(Throwable cause)
 *  在断开连接时调用。
 *
 *  public void deliveryComplete(MqttDeliveryToken token))
 *      接收到已经发布的 QoS 1 或 QoS 2 消息的传递令牌时调用。
 *  ?由 MqttClient.connect 激活此回调。
 *
 */
public class PushCallback implements MqttCallbackExtended {
	public Integer id;//网关id
	public int count=0;
	PushCallback(Integer id){
		this.id=id;
	}
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	//private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(5);
	Executor executor = SpringContextHolder.getBean("threadPool1");;
	private RedisService redisService= SpringContextHolder.getBean("redisService");
	private static KafkaTemplate<String, String> kafkaTemplate=SpringContextHolder.getBean("kafkaTemplate");
	private GatewayService gatewayService= SpringContextHolder.getBean("gatewayServiceImpl");
	private MapContainer mapContainer= SpringContextHolder.getBean("mapContainer");
	private KafukaSender kafukaSender=SpringContextHolder.getBean("kafukaSender");
	private TagService tagService=SpringContextHolder.getBean("tagServiceImpl");
	private DataProcess dataProcess=SpringContextHolder.getBean("dataProcess");
	/*HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);*/
	//private ConcurrentHashMap<String,Date> volt=new ConcurrentHashMap<>();
	DecimalFormat df = new DecimalFormat("#0.00");
	public void connectionLost(Throwable cause) {
		// 连接丢失后，一般在这里面进行重连
		logger.error("连接断开，可以做重连"+cause);
		gatewayService.updateGatewayConnect(id,0);
		Gateway_uwb gateway_uwb=gatewayService.findById(id);
		HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
		if(haoXiangMqtt!=null){
			BsState bsState=new BsState();
			bsState.setBsid(gateway_uwb.getName());
			bsState.setErrorCode((short)2);
			bsState.setState((short) 1);
			bsState.setType((short)3);
			bsState.setTime(new Date().getTime());
			haoXiangMqtt.publishBsStateData(gateway_uwb.getName(),bsState);
		}
		Client client=Connect.connect.get(id);
		client.startReconnect();
		/*if (client.start(id)){
			//gatewayService.updateGatewayConnect(id,1);
		}else{
			//gatewayService.updateGatewayConnect(id,0);
		}*/
	}

	@Override
	public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
		/*executor.execute(new Runnable() {
			@Override
			public void run() {
				{*/
					try {
						String message = new String(mqttMessage.getPayload());
						if ("".equals(message)|| message==null||message.equals("close")||message.equals("NaN")){
							return;
						}
						logger.info("receive topic:::"+s);
						if(s.contains("gateway_sub1g")){

                           processSub1G(s,message);
						}else if(s.contains("node")){
							processDw1001Data(s,message);
						}else if(s.contains("vbat")){
							processDw1001Vbat(s,message);

						}else {
							logger.info("传输的数据"+message);
						}

					}catch (Exception e){
						logger.error(e.getMessage());
					}
		/*		}
			}
		});*/
	}
void processDw1001Data(String s,String message){
	String sub=s.substring(s.lastIndexOf("/")+1);
	String[] split=s.split("/");
	String tagid=split[2];//标签id
	//标签判断
	logger.info("获取到mqtt传输的数据"+message);
	if (sub.equals("location")){
		Gateway_uwb gateway_uwb=gatewayService.findById(id);
		if (!NullUtils.isEmpty(gateway_uwb)){
			JSONObject obj1 = JSONObject.fromObject(message);
			TagInf tagInf = mapContainer.tagInf.get(tagid);
			if(tagInf==null){
				tagInf=new TagInf(tagid);
				mapContainer.tagInf.put(tagid,tagInf);
			}
			JSONObject pos = (JSONObject)obj1.get("position");
			double[] oldPos={tagInf.getX(),tagInf.getY(),tagInf.getZ()};
			double[] newPos={pos.getDouble("x"),pos.getDouble("y"),pos.getDouble("z")};
			if(Double.isNaN(newPos[0])||Double.isNaN(newPos[1]))
				return;
			double[] filterRes=getWeightRes(oldPos,newPos,10);
			pos.replace("x",(float)Math.round(filterRes[0]*100)/100f );
			pos.replace("y",(float)Math.round(filterRes[1]*100)/100f);
			pos.replace("z",(float)Math.round(filterRes[2]*100)/100f);

			obj1.replace("position",pos);
			if(!Double.isNaN(filterRes[0])&&!Double.isNaN(filterRes[1])) {
				tagInf.setX((float) filterRes[0]);
				tagInf.setY((float) filterRes[1]);
				tagInf.setZ((float) filterRes[2]);
			}
			obj1.put("tagid", tagid);
			obj1.put("type", 3);//定位数据类型 3 uwb室内定位
			obj1.put("map", gateway_uwb.getMap());
			obj1.put("area", "");
		    obj1.put("floor",gateway_uwb.getFloor()==null?"":gateway_uwb.getFloor()+"");

			kafkaTemplate.send(KafukaTopics.TAG_LOCATION, obj1.toString());

				TagLocation tagLocation = new TagLocation();
				tagLocation.setTagid(tagid);
				tagLocation.setX((float) ((Math.round(filterRes[0] * 100)) / 100f));
				tagLocation.setY((float) ((Math.round(filterRes[1] * 100)) / 100f));
				tagLocation.setZ((float) ((Math.round(filterRes[2] * 100)) / 100f));
				tagLocation.setTime(new Date().getTime());
				tagLocation.setType((short) 3);
				Gateway_uwb gateway = gatewayService.findById(id);
				tagLocation.setFloor(gateway.getFloor() == null ? "" : gateway.getFloor() + "");
			//HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
			// kafukaSender.send(KafukaTopics.TAG_LOCATION, tagLocation.toString());
			HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
			if(haoXiangMqtt!=null)
				haoXiangMqtt.publishLocationData(tagid,tagLocation);


		}
	}
	//基站判断
	if (sub.equals("config")){
		Gateway_uwb gateway_uwb=gatewayService.findById(id);
		JSONObject obj1 = JSONObject.fromObject(message);
		obj1.put("bsid", tagid);//基站编号
		obj1.put("instanceid",gateway_uwb.getInstanceid());//实例id
		obj1.put("map",gateway_uwb.getMap());//所在地图
		obj1.put("type","2");//基站类型
		redisService.setex("base,"+tagid,5*60,message);//存储微基站新
		kafkaTemplate.send(KafukaTopics.BS_STATE, obj1.toString());
		BsState bsState=new BsState();
		bsState.setType(2);
		bsState.setTime(new Date().getTime());
		bsState.setErrorCode((short)2);
		bsState.setState((short) 0);
		bsState.setBsid(tagid);
		HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
		if(haoXiangMqtt!=null)
		haoXiangMqtt.publishBsStateData(tagid,bsState);

		logger.info("发送主题tagConfig"+tagid);
	}
	//SOS报警判断
	if (sub.equals("data")){
		JSONObject obj1 = JSONObject.fromObject(message);
		obj1.put("tagid",tagid);
		kafkaTemplate.send("tagsos",obj1.toString());
	}
	//温度判断
	if (sub.equals("temperature")){
		kafkaTemplate.send("tagtemperature",message);
	}
	//拆卸报警
	if(sub.equals("破拆")){
		JSONObject obj1 = JSONObject.fromObject(message);
		obj1.put("tagid",tagid);
		kafkaTemplate.send("tagbroken",obj1.toString());
	}
	//心率报警
	if (sub.equals("心率")){
		kafkaTemplate.send("tagheart",message);
	}
}

	public static void main(String[] args) {

	}


	void processDw1001Vbat(String s,String message){
		JSONObject obj1 = JSONObject.fromObject(message);
		String mac=obj1.getString("tagid");//	mac
		String vbat =obj1.getString("vbat");//电压
		String sender=obj1.getString("sender");//sub1g id
		Integer rssi=obj1.getInt("rssi");//rssi
		TagPara tagpara = tagService.findTagMac(mac);
		if(tagpara!=null){
		TagSensor tagSensor=new TagSensor();
		tagSensor.setTime(new Date().getTime());
		tagSensor.setTagid(tagpara.getTagid());
		tagSensor.setPower(Float.valueOf(vbat));
		tagSensor.setType((short)0);
		tagSensor.setBsid(sender);
		tagSensor.setSub1g_rssi(rssi.shortValue());
		kafukaSender.send(KafukaTopics.TAG_SENSOR,tagSensor.toString());
		}

	}

	void processSub1G(String s,String message){
		logger.error("sub 1G:"+message);
		String sub=s.substring(s.lastIndexOf("/")+1);
		String[] split=s.split("/");
		String deviceid=split[2];//设备id
		JSONObject obj1 = JSONObject.fromObject(message);
		String msgid=obj1.getString("msgid");//	MQTT消息序号，递增
		String sender=obj1.getString("sender");//网关的MAC地址
		String msgtype=obj1.getString("msgtype");//sub1g消息
		Integer tagid=obj1.getInt("tagid");//定位卡的ID
		String seq=obj1.getString("seq");//定位卡的消息序号，递增
		String adc=obj1.getString("adc");//电池电压
		String sub1g_rssi=obj1.getString("rssi");//SUB1G信道收到的信号强度
		String beacons=obj1.getString("beacons");//定位卡收到的蓝牙信标数据
        String status=obj1.getString("status");//动静状态
		Integer power = Integer.valueOf(adc);
		HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
		BsState bsState=new BsState();
		bsState.setTime(new Date().getTime());
		bsState.setBsid(deviceid);
		bsState.setErrorCode((short)2);
		bsState.setState((short)0);
		bsState.setType(3);
		//bsState.setIp(gatewayService.findById(id).getIp());
		haoXiangMqtt.publishBsStateData(deviceid,bsState);
/*		if(volt.containsKey(tagid+"")){
			Date date = volt.get(tagid + "");
			Date current=new Date();
			long diff=(current.getTime()-date.getTime())/60000;//相差分钟数
			if(diff>10){
				TagVolt tagVolt=new TagVolt();
				tagVolt.setAdc(power);
				tagVolt.setNum(tagid+"");
				tagVolt.setTime(current);
				tagVolt.setRssi(Integer.valueOf(sub1g_rssi));
				volt.replace(tagid + "",current);
				tagService.addTagVolt(tagVolt);
			}
		}else{
			Date current = new Date();
			TagVolt tagVolt=new TagVolt();
			tagVolt.setAdc(power);
			tagVolt.setNum(tagid+"");
			tagVolt.setTime(current);
			tagVolt.setRssi(Integer.valueOf(sub1g_rssi));
			volt.put(tagid + "",current);
			tagService.addTagVolt(tagVolt);
		}*/
		if (power >= 3288) {
			power = 100;
		}  else if (power < 2320) {
			power = 0;
		} else {
			power =(int)Math.round( Math.abs(power-2320)/9.69);
		}

		double volt = 3.2 + power / 100.0;
		TagSensor tagSensor=new TagSensor();
		tagSensor.setTime(new Date().getTime());
		tagSensor.setTagid(tagid+"");
		tagSensor.setPower((float) Math.round(volt* 100) / 100f);
		tagSensor.setMoveState(Short.valueOf(status));
		tagSensor.setType((short)0);
		tagSensor.setBsid(sender);
		tagSensor.setSub1g_rssi(Short.valueOf(sub1g_rssi));

		kafukaSender.send(KafukaTopics.TAG_SENSOR,tagSensor.toString());

		if(haoXiangMqtt!=null)
			haoXiangMqtt.publishSensorData(tagid+"",tagSensor);

		JSONArray array=JSONArray.fromObject(beacons);
		TagInf tagInf = mapContainer.tagInf.get(tagid.longValue()+"");
		if(tagInf==null){
			tagInf=new TagInf(tagid+"");
			mapContainer.tagInf.put(tagid+"",tagInf);
		}
		System.out.println(tagid+"move"+status+"count"+tagInf.firstMove);
		tagInf.jsonArray.clear();
        Integer move = Integer.valueOf(status);

		//System.out.println(tagid+"move"+status+"count"+tagInf.firstMove);





		ArrayList<Double[]> bsposList=new ArrayList();//高频定位
		ArrayList<Double> bsposDis=new ArrayList();// 高频定位
		ArrayList<String> bsNameList=new ArrayList();// bsname
		for (Object beacon:array
			 ) {
			JSONObject jsonobj = (JSONObject) beacon;
			 	String id=jsonobj.getString("id");//	信标id
				String rssi1=jsonobj.getString("r1");//	rssi1 1米处信号强度
				String rssi2=jsonobj.getString("r2");//	rssi2 rssi
			   Bslr_dis rangeInf1 = tagInf.range_bslr_dis.get(id + "-" +0);
			   

			double dis=0;
			TagPara tagpara = tagService.findTagid(tagid + "");
			float fix=0;
			if(tagpara!=null){
				fix=tagpara.getFixvalue();
			}
			dis=(double)BleRssiDis.calcDistByRSSI(Integer.valueOf(rssi2),Float.valueOf(rssi1)+fix);

			if( Integer.valueOf(id).intValue()==0){
				continue;
			}
			TagBeacon tagBeacon=new TagBeacon();
			tagBeacon.setBeacon(id);
			tagBeacon.setDis((float)dis);
			tagBeacon.setNum(tagid+"");
			tagBeacon.setRssi(Integer.valueOf(rssi2));
			tagBeacon.setRssi_1(Integer.valueOf(rssi1));
			tagService.addBeaconRssi(tagBeacon);

			if (rangeInf1 == null) {
				rangeInf1 = new Bslr_dis();
				tagInf.range_bslr_dis.put(id + "-" + 0, rangeInf1);
			}

			Float[] filterDis1 = rangeInf1.addDis(new DisInf((float) dis, 0f),tagInf.getFreq(),mapContainer.timedelay_lowfreq,mapContainer.discachelen_lowfreq);
			if(filterDis1!=null) {
				JSONObject jsonObject_0=new JSONObject();
				//JSONObject jsonObject_1=new JSONObject();
				logger.info("sender MAC:"+sender+"::"+tagid+"信标:"+id+":1米处信号强度:"+(Integer.valueOf(rssi1))+":rssi:"+rssi2+":filterdis:"+filterDis1[0]);
				BsConfigService bsConfigService = SpringContextHolder.getBean("bsConfigServiceImpl");
				BsConfig bsInf1 = bsConfigService.findByNum(id + "");
				jsonObject_0.put("originalDis",(float)Math.round(dis * 100) / 100);
				jsonObject_0.put("filterDis",(float)Math.round(filterDis1[0] * 100) / 100);
				jsonObject_0.put("bsid",id+"-0");
			/*	jsonObject_1.put("originalDis",(float)Math.round(dis * 100) / 100);
				jsonObject_1.put("filterDis",(float)Math.round(filterDis1[0] * 100) / 100);
				jsonObject_1.put("bsid",id+"-0");*/
				tagInf.jsonArray.add(jsonObject_0);
				if(bsInf1==null || dis>mapContainer.bleover)
					continue;
				Double[] bspos={bsInf1.getX(),bsInf1.getY(),bsInf1.getZ(),Double.valueOf(id)};
				bsposList.add(bspos);
				bsposDis.add((double)Math.round(dis * 100) / 100);
				bsNameList.add(id);

				//tagInf.jsonArray.add(jsonObject_1);
			}
		}
/*		if(move==0&&tagInf.firstMove<0)
			return;
		else{
			if(move==0) {
				if (tagInf.firstMove < 5) {
					tagInf.firstMove++;
				} else {
					tagInf.firstMove = -1;
				}
			}else{
				tagInf.firstMove=0;
			}
		}*/

	//	logger.info(tagid+":near:"+bsposDis.size());
	//	int nearBs =geteNearestBs(bsposDis);
		        //优先单点定位
		       DisSort res=null;
				Double[][] bsPos = bsposList.toArray(new Double[0][0]);
				Double[] bsDis = bsposDis.toArray(new Double[0]);
				ArrayList<DisSort> list = new ArrayList();
				int bsCount = bsDis.length;
				for (int i = 0; i < bsCount; i++) {
					DisSort disSort = new DisSort(bsPos[i][0], bsPos[i][1], bsPos[i][2],Double.valueOf(bsPos[i][3]).longValue()+"", bsDis[i]);
					disSort.setR(bsPos[i][3].floatValue());
					list.add(disSort);
				}

				Collections.sort(list);
			/*	DisSort singleRes = singlePointLocation(list);
				if(singleRes!=null)
					res=singleRes;*/
				if(res==null){
				DisSort near_bs1 = list.get(0);
				DisSort near_bs2=list.get(1);
				double twoBsLevel=mapContainer.ble1d;
				if(res==null&&near_bs1.getDis()<twoBsLevel&&near_bs2.getDis()<twoBsLevel) {
					logger.info(tagid+"use 2");
					Double[] near_1 = {near_bs1.getX(), near_bs1.getY(), near_bs1.getZ()};
					Double[] near_2 = {near_bs2.getX(), near_bs2.getY(), near_bs2.getZ()};
					Double[][] near_bs = {near_1, near_2};
					double dis_two_bs = PercentToPosition.getDis(near_1, near_2);
					Double[][] bspos = {{0d, 0d, 0d}, {dis_two_bs, 0d, 0d}};
					Double[] near_dis = {near_bs1.getDis(), near_bs2.getDis()};
					double[][] pos = Hilen.location1D(near_dis, bspos);
					double percent = pos[0][0] / dis_two_bs;
					if (Math.abs(percent) < 1.3) {
						double diffDis=near_bs1.getDis()+near_bs2.getDis()-dis_two_bs;
						double[] res1D = PercentToPosition.percentToPosition(near_1, near_2, percent);
						if(diffDis<0||list.size()==2)
						res = new DisSort(res1D[0], res1D[1], res1D[2], near_bs1.getBsname() + ":" + near_bs2.getBsname(), 0d);
						else if(list.size()>=3&&diffDis>0){
							DisSort near_3 = list.get(2);
							CGPoint p1 = new CGPoint(near_3.getX(), near_3.getY());
							CGPoint p2 = new CGPoint(res1D[0], res1D[1]);
							CGPoint coc = new CGPoint(res1D[0], res1D[1]);
							double[] whatIWanted = CGGeometryLib.getLineCircleNode(p1, p2, coc, diffDis/2);
							if(whatIWanted!=null)
								res = new DisSort(whatIWanted[0], whatIWanted[1], near_bs1.getZ(), near_bs1.getBsname() + ":" + near_bs2.getBsname(), 0d);
						}
					} else {
						res = null;
					}
					if(res!=null){
						logger.info(tagid+"use 2"+":x:"+res.getX()+":y:"+res.getY()+"area:"+res.getBsname());
					}
				}
			}

			if(res==null) {
				//过滤条件严苛，将原始测距数据重新计算出结果输出
				logger.info(tagid+"use 3");
				res= dataProcess.calculPos_weight(bsposList, bsposDis, 3,0);
				if(res!=null) {
					List boundry = ConvexReg.isInPolygon(bsPos);
					boolean in = ArithmeticlUtil.isInPolygon(new Point2d(res.getX(), res.getY()), boundry);
					if (!in) {
						logger.info(tagid + ":x:" + res.getX() + ":y:" + res.getY() + ":no in 3 bs area:" + res.getBsname());
						res = null;
					}
				}
			}

           if(res==null) {
			   int calculateBs = mapContainer.location_bsnum;
			   res = dataProcess.calculPos_weight(bsposList, bsposDis, calculateBs,0);
			   if(res!=null) {
				   List boundry = ConvexReg.isInPolygon(bsPos);
				   boolean in = ArithmeticlUtil.isInPolygon(new Point2d(res.getX(), res.getY()), boundry);
				   if (!in) {
					   logger.info(tagid + ":x:" + res.getX() + ":y:" + res.getY() + ":no in 4 bs area:" + res.getBsname());
					   res = null;
				   }
			   }


		   }

		/*	if(res==null&&mapContainer.location_strictmode==1){
				logger.info("no strict 4 and  3, use location_strictmode ");
				res= DataProcess.lowFreqFilter(allBsList,3);
			}*/



		if(res!=null) {
			LocFiterRes locFiterRes = new LocFiterRes(res.getBsname(), res.getX().floatValue(), res.getY().floatValue(), res.getZ().floatValue(), (short) 1);
			locFiterRes.r=0;

			//  logger.error("8bs res"+"x:"+res.getX()+"y:"+res.getY());
			String[] bsnames=res.getBsname().split(":");
			BsConfigService bsConfigService = SpringContextHolder.getBean("bsConfigServiceImpl");
			BsConfig bsInf1 = bsConfigService.findByNum(Double.valueOf(bsnames[1]).longValue()+"");
			locFiterRes.floor=bsInf1.getFloor();
			tagInf.bsid = Double.valueOf(bsnames[1]).longValue();
			tagInf.setRegion(locFiterRes,mapContainer.timedelay_lowfreq,mapContainer.locationcachelen_lowfreq);
		}

	}

public static  double[]	getWeightRes(double[] former,double[] current,int weight){
		int len=former.length;
         if(former[0]==0 &&former[1]==0){
              return current;
		 }else {
			 double[] res = new double[len];
			 for (int i = 0; i < len; i++) {
				 res[i] = (weight * former[i] + current[i]) / (weight + 1);
			 }
			 return res;
		 }

	}

/*	int geteNearestBs(ArrayList<Double> bsDis){
		double min = Double.MAX_VALUE;
		int index = -1;
		for (int i = 0; i < bsDis.size(); i++) {
			Double f = bsDis.get(i);
			if (Double.compare(f.doubleValue(), min) < 0) {
				min = f.doubleValue();
				index = i;
			}
		}
		if(min<0.7)
			return index;
		else
			return -1;
	}*/
DisSort  singlePointLocation(ArrayList<DisSort> list ){
	    DisSort res=null;
        DisSort d1=list.get(0);//最近距离信标点
	    int size=list.size();
       if(size==1){//只有一个信标时直接返回位置为当前信标位置
       	return d1;
	   }else {
       	if(d1.getDis()<mapContainer.blesingledis){
			double[] res1D=null;
       		if(size>=3){
				DisSort near_bs1=list.get(1);//第二距离信标点
				DisSort near_bs2=list.get(2);//第二距离信标点
				Double[] near_1 = {near_bs1.getX(), near_bs1.getY(), near_bs1.getZ()};
				Double[] near_2 = {near_bs2.getX(), near_bs2.getY(), near_bs2.getZ()};
				Double[][] near_bs = {near_1, near_2};
				double dis_two_bs = PercentToPosition.getDis(near_1, near_2);
				Double[][] bspos = {{0d, 0d, 0d}, {dis_two_bs, 0d, 0d}};
				Double[] near_dis = {near_bs1.getDis(), near_bs2.getDis()};
				double[][] pos = Hilen.location1D(near_dis, bspos);
				double percent = pos[0][0] / dis_two_bs;

				if (Math.abs(percent) < 1.1) {
					res1D = PercentToPosition.percentToPosition(near_1, near_2, percent);
				}else {
					res1D=new double[2];
					res1D[0]=(near_1[0]+near_2[0])/2;
					res1D[1]=(near_1[1]+near_2[1])/2;
				}

			}else if(size==2) {
				DisSort near_bs1=list.get(1);//第二距离信标点
				res1D=new double[2];
				res1D[0]=near_bs1.getX();
				res1D[1]=near_bs1.getY();
			}
			CGPoint p1 = new CGPoint(res1D[0], res1D[1]);
			CGPoint p2 = new CGPoint(d1.getX(), d1.getY());
			CGPoint coc = new CGPoint(d1.getX(), d1.getY());
			double[] whatIWanted = CGGeometryLib.getLineCircleNode(p1, p2, coc, d1.getDis());
			if(whatIWanted!=null)
				res = new DisSort(whatIWanted[0], whatIWanted[1], d1.getZ(), ":" + d1.getBsname(), 0d);

		}

	   }
       return res;
}
	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// publish后会执行到这里
		System.out.println("deliveryComplete---------" + token.isComplete());
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		gatewayService.updateGatewayConnect(id,1);
		Gateway_uwb gateway_uwb=gatewayService.findById(id);
		HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
		if(haoXiangMqtt!=null){
			BsState bsState=new BsState();
			bsState.setBsid(gateway_uwb.getName());
			bsState.setErrorCode((short)2);
			bsState.setState((short)0);
			bsState.setType((short)3);
			//bsState.setIp(gateway_uwb.getIp());
			bsState.setTime(new Date().getTime());
			haoXiangMqtt.publishBsStateData(gateway_uwb.getName(),bsState);
		}


	}
}



