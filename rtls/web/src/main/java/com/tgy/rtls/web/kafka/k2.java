//package com.tgy.rtls.web.kafka;
//
//import com.tgy.rtls.data.common.KafukaTopics;
//import com.tgy.rtls.data.common.NullUtils;
//import com.tgy.rtls.data.common.TimeUtil;
//import com.tgy.rtls.data.entity.equip.Infrared;
//import com.tgy.rtls.data.entity.park.*;
//import com.tgy.rtls.data.entity.view.ViewVo;
//import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
//import com.tgy.rtls.data.mapper.equip.GatewayMapper;
//import com.tgy.rtls.data.mapper.equip.TagMapper;
//import com.tgy.rtls.data.mapper.park.BookMapper;
//import com.tgy.rtls.data.mapper.park.ParkMapper;
//import com.tgy.rtls.data.mapper.view.ViewMapper;
//import com.tgy.rtls.data.service.common.RedisService;
//import com.tgy.rtls.data.service.lock.impl.RedissonDistributedLocker;
//import com.tgy.rtls.data.service.vip.ParkingInfoStatisticsService;
//import com.tgy.rtls.data.websocket.WebSocketLocation;
//import com.tgy.rtls.web.config.SpringContextHolder;
//import com.tgy.rtls.web.controller.view.AppletsWebSocket;
//import lombok.extern.slf4j.Slf4j;
//import net.sf.json.JSONObject;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.handler.annotation.Header;
//import org.springframework.stereotype.Service;
//
//import java.sql.Timestamp;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.*;
//import java.util.concurrent.Executor;
//import java.util.concurrent.TimeUnit;
//
//@Service
//@Slf4j
//public class KafkaPark {
//    @Autowired(required = false)
//    private ParkMapper parkMapper;
//    @Autowired(required = false)
//    private GatewayMapper gatewayMapper;
//    @Autowired(required = false)
//    private TagMapper tagMapper;
//    @Autowired(required = false)
//    BookMapper bookMapper;
//    @Autowired(required = false)
//    private ViewMapper viewMapper;
//    @Autowired
//    private RedisService redisService;
//    @Autowired
//    Executor scheduledExecutorService;
//    @Autowired
//    private WebSocketLocation webSocketLocation;
//    @Autowired
//    private AppletsWebSocket appletsWebSocket;
//    @Autowired
//    private RedissonDistributedLocker redissonDistributedLocker;
//    @Autowired
//    private ParkingInfoStatisticsService parkingInfoStatisticsService;
//    /*  private ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(5);*/
//
//    private double batter = 0.15;//标签电压变化临界值
//
//    /*
//     * 网关状态
//     * */
//    @KafkaListener(topics = {KafukaTopics.LORA_STATE})
//    public void lora(ConsumerRecord<?, ?> record,/* Acknowledgment ack,//手动提交offset*/
//                     @Header(KafkaHeaders.OFFSET) long offSet) {
//     /*  scheduledExecutorService.execute(new Runnable() {
//           @Override
//           public void run() {*/
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
////                logger.info("------------------ lora网关状态数据 :" + message);
//                JSONObject data = JSONObject.fromObject(message);
//                String map = data.getString("map");
//                JSONObject jsonArea = new JSONObject();
//                JSONObject json = new JSONObject();
//                jsonArea.put("type", 6);
//                jsonArea.put("data", data);
//                jsonArea.put("map", map);
//                webSocketLocation.sendAll(jsonArea.toString());//某个网关的状态数据
//                if (map != null) {
//                    BeaconCount calcut = gatewayMapper.getGateway_loraAcount(Integer.valueOf(map));
//                    jsonArea.put("data", calcut);
//                    jsonArea.put("type", 13);
//                    webSocketLocation.sendAll(jsonArea.toString());//网关的统计数据
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//     /*      }
//       });*/
//    }
//
//    /**
//     * 车位检测器状态//     */
//    @KafkaListener(topics = {KafukaTopics.INFRARED_STATE})
////    @KafkaListener(topics = {KafukaTopics.INFRARED_STATE1})
//    public void infrared(ConsumerRecord<?, ?> record) throws ParseException {
//        try {
//            ////Instant start = Instant.now();
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
////                log.error("------------------ 接收到红外车位检测设备数据 =" + message);
//                JSONObject data = JSONObject.fromObject(message);
//                String map = data.getString("map");
//                JSONObject jsonArea = new JSONObject();
//                jsonArea.put("type", 7);
//                jsonArea.put("data", data);
//                jsonArea.put("map", map);
//                // log.error(data.toString());
//                Infrared infrared = (Infrared) JSONObject.toBean(data, Infrared.class);
//                if(infrared.getMap()!=0&&!NullUtils.isEmpty(infrared.getMap())&&!NullUtils.isEmpty(infrared.getFid())&&!NullUtils.isEmpty(infrared.getPlace())
//                        &&!NullUtils.isEmpty(infrared.getMapName())&&!NullUtils.isEmpty(infrared.getRawProductId())){
//                    redisService.setex("infrared," + infrared.getId(), 1000, "");
//                }
//                webSocketLocation.sendAll(jsonArea.toString());
//                Integer placeId = infrared.getPlace();
//                ParkingPlace place;
//                List<ParkingPlace> places = parkMapper.getPlaceById(placeId);
//                if ((Optional.ofNullable(places).get().size() == 0)) {
//                    return;
//                } else {
//                    place = (data.size() == 0 ? null : places.get(0));
//                }
//
//                BeaconCount calculate;
//                {
//                    JSONObject jsonArea1 = new JSONObject();
//                    if (map != null) {
//                        webSocketLocation.sendAll(jsonArea1.toString());
//                        calculate = tagMapper.getInfraredAcount(Integer.valueOf(map));
//                        jsonArea1.put("type", 14);
//                        jsonArea1.put("map", map);
//                        jsonArea1.put("data", calculate);
//                        webSocketLocation.sendAll(jsonArea1.toString());
//                    }
//                }
//                if (place != null) {
//                    // log.error("Place"+place);
//                    // log.error("batteryTime:"+data.getJSONObject("batteryTime").getLong("time"));
//                    if ("0".equals(infrared.getNetworkstate().toString())) {
//                        ParkingPlace parkingPlace = new ParkingPlace();
//                        parkingPlace.setId(place.getId());
//                        parkingPlace.setState((short) 1);
//                        parkMapper.updatePlace(parkingPlace);
//                    }
//                    JSONObject jsonArea1 = new JSONObject();
//                    jsonArea1.put("type", 8);
//                    jsonArea1.put("data", place);
//                    jsonArea1.put("map", map);
//                    webSocketLocation.sendAll(jsonArea1.toString());
//                    jsonArea1.put("type", 9);
//                    RealTimeData realTimeData = bookMapper.selectRealTimeData(Integer.valueOf(map));
//                    jsonArea1.put("data", realTimeData);
//                    webSocketLocation.sendAll(jsonArea1.toString());
//                    // 设置key的名称
//                    String lockKey = "mapId:" + place.getMap() + ":placeId:" + place.getId();
//                    // 设置锁的超时时间
//                    redissonDistributedLocker.lock(lockKey, TimeUnit.SECONDS, 5);
//                    PlaceUseRecord placeRecord = new PlaceUseRecord();
//                    ////Instant start1 = Instant.now();
//                    List<Infrared> repeatInfrared = tagMapper.findInfraredId(place.getId(), place.getMap(), infrared.getNum());
//                    ////Instant end1 = Instant.now();
//                    ////Duration elapsedTime1 = Duration.between(start1, end1);
//                    ////log.error("realTimeData1代码执行时间：" + elapsedTime1.toMillis() + "毫秒");
//                    Short infraredUploadStatus = infrared.getStatus();
//                    Short repeatInfraredStatus = (NullUtils.isEmpty(repeatInfrared) ? null : repeatInfrared.get(0).getStatus());
//                    long timestamp  = data.getJSONObject("batteryTime").getLong("time");
//                    String timeStr = TimeUtil.timestampToStr(timestamp);
//                    //判断是否存在
//                    if (!NullUtils.isEmpty(repeatInfrared)) {
//                        placeRecord.setPlace(place.getId());
//                        placeRecord.setTimestamp(timestamp);
//                        if (infraredUploadStatus == 0 && repeatInfraredStatus != null && repeatInfraredStatus == 0) {
//                            placeRecord.setEnd(timeStr);
//                            update(placeRecord,place);
//                            ////Instant end = Instant.now();
//                            ////Duration elapsedTime = Duration.between(start, end);
//                            ////log.error("代码执行时间：" + elapsedTime.toMillis() + "毫秒");
//                        }else {
//                            placeRecord.setMap(place.getMap());
//                            placeRecord.setStart(timeStr);
//                            add(placeRecord, place,places);
//                            ////Instant end = Instant.now();
//                            ////Duration elapsedTime = Duration.between(start, end);
//                            ////log.error("代码执行时间：" + elapsedTime.toMillis() + "毫秒");
//                        }
//                    } else {
//                        placeRecord.setTimestamp(timestamp);
//                        placeRecord.setPlace(place.getId());
//                        if (infraredUploadStatus == 0) {
//                            placeRecord.setEnd(timeStr);
//                            update(placeRecord,place);
//                        }else {
//                            placeRecord.setStart(timeStr);
//                            placeRecord.setMap(place.getMap());
//                            add(placeRecord, place,places);
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    public void add(PlaceUseRecord placeRecord,ParkingPlace place,List<ParkingPlace> places) throws ParseException {
//        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
//        LocalDateTime startTime = TimeUtil.strTimeToLocalDateTime(placeRecord.getStart());
//        if(placeRecords.size()>1){
//            places.forEach(placeIds -> bookMapper.delPlaceUseRecord(place.getMap(), place.getId()));
//            placeRecord.setMap(placeRecord.getMap());
//            placeRecord.setPlace(placeRecord.getPlace());
//            placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
//            if(bookMapper.addPlaceUseRecord(placeRecord) > 0){
//                JSONObject jsonArea = new JSONObject();
//                ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//                jsonArea.put("type", 27);
//                jsonArea.put("data", realTimeInAndOutData);
//                jsonArea.put("uid", "-1");
//                appletsWebSocket.sendAll(jsonArea.toString());
//
//                ViewVo oneMapRealTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//                jsonArea.put("type", 18);
//                jsonArea.put("data",oneMapRealTimeInAndOutData);
//                jsonArea.put("map", place.getMap());
//                webSocketLocation.sendAll(jsonArea.toString());
//
//                ParkingInfoStatistics infoStatistics = new ParkingInfoStatistics();
//                infoStatistics.setMap(Long.valueOf(place.getMap()));
//                infoStatistics.setPlace(Long.valueOf(place.getId()));
//                infoStatistics.setPlacename(place.getName());
//                infoStatistics.setStartTime(startTime);
//                parkingInfoStatisticsService.addParkingInfoStatisticsUse(infoStatistics);
//            }
//        }else {
//            if(NullUtils.isEmpty(placeRecords) || !(placeRecords.size() ==1)){
//                if (bookMapper.addPlaceUseRecord(placeRecord) > 0) {
//                    JSONObject jsonArea = new JSONObject();
//                    ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//                    jsonArea.put("type", 27);
//                    jsonArea.put("data", realTimeInAndOutData);
//                    jsonArea.put("uid", "-1");
//                    appletsWebSocket.sendAll(jsonArea.toString());
//
//                    ViewVo oneMapRealTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//                    jsonArea.put("type", 18);
//                    jsonArea.put("data",oneMapRealTimeInAndOutData);
//                    jsonArea.put("map", place.getMap());
//                    webSocketLocation.sendAll(jsonArea.toString());
//
//                    ParkingInfoStatistics infoStatistics = new ParkingInfoStatistics();
//                    infoStatistics.setMap(Long.valueOf(place.getMap()));
//                    infoStatistics.setPlace(Long.valueOf(place.getId()));
//                    infoStatistics.setPlacename(place.getName());
//                    infoStatistics.setStartTime(startTime);
//                    parkingInfoStatisticsService.addParkingInfoStatisticsUse(infoStatistics);
//                }
//            }
//        }
//    }
//
//    public void update(PlaceUseRecord placeRecord,ParkingPlace place) throws ParseException {
//        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
//        if(!NullUtils.isEmpty(placeRecords)){
//            placeRecord.setId(placeRecords.get(0).getId());
//            JSONObject jsonArea = new JSONObject();
//            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placeRecord.getTimestamp()), ZoneId.of("Asia/Shanghai"));
//            LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Shanghai"));
//            Duration duration = Duration.between(localDateTime, currentDateTime);
//            long diffInMinutes = Math.abs(duration.toMinutes());
//            if (diffInMinutes < 5) {
//                SpringContextHolder.parkingPlaceConcurrentHashMap.remove(place.getId());
//            }
//
//            bookMapper.UpdatePlaceUseRecordByid(placeRecord);
//            ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//            jsonArea.put("type", 27);
//            jsonArea.put("data", realTimeInAndOutData);
//            jsonArea.put("uid", "-1");
//            appletsWebSocket.sendAll(jsonArea.toString());
//
//            ViewVo oneMapRealTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
//            jsonArea.put("type", 18);
//            jsonArea.put("data",oneMapRealTimeInAndOutData);
//            jsonArea.put("map", place.getMap());
//            webSocketLocation.sendAll(jsonArea.toString());
//        }
//
//    }
//
//
//    @KafkaListener(topics = {KafukaTopics.VISUALDATASEND})
//    public void VisualDataSend(ConsumerRecord<?, ?> record) throws ParseException {
//        try {
//            ////Instant start = Instant.now();
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
////                log.error("------------------ 接收到红外车位检测设备数据 =" + message);
//                JSONObject data = JSONObject.fromObject(message);
//                String map = data.getString("map");
//                JSONObject jsonArea = new JSONObject();
//                jsonArea.put("type", 7);
//                jsonArea.put("data", data);
//                jsonArea.put("map", map);
//                webSocketLocation.sendAll(jsonArea.toString());
//                // log.error(data.toString());
//                Infrared infrared = (Infrared) JSONObject.toBean(data, Infrared.class);
//
//                Integer placeId = infrared.getPlace();
//                ParkingPlace place;
//                List<ParkingPlace> places = parkMapper.getPlaceById(placeId);
//                if ((Optional.ofNullable(places).get().size() == 0)) {
//                    return;
//                } else {
//                    place = (data.size() == 0 ? null : places.get(0));
//                }
//
//                BeaconCount calculate;
//                JSONObject jsonArea1 = new JSONObject();
//                jsonArea1.put("map", map);
//                {
//                    if (map != null) {
//                        webSocketLocation.sendAll(jsonArea1.toString());
//                        calculate = tagMapper.getInfraredAcount(Integer.valueOf(map));
//                        jsonArea1.put("type", 14);
//                        jsonArea1.put("data", calculate);
//                        webSocketLocation.sendAll(jsonArea1.toString());
//                    }
//                }
//                if (place != null) {
//                    // log.error("Place"+place);
//                    // log.error("batteryTime:"+data.getJSONObject("batteryTime").getLong("time"));
//
//                    jsonArea1.put("type", 8);
//                    jsonArea1.put("data", place);
//                    jsonArea1.put("map", map);
//                    webSocketLocation.sendAll(jsonArea1.toString());
//
//                    RealTimeData realTimeData = bookMapper.selectRealTimeData(Integer.valueOf(map));
//                    jsonArea1.put("type", 9);
//                    jsonArea1.put("data", realTimeData);
//                    webSocketLocation.sendAll(jsonArea1.toString());
//
//
//
//                }
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//
//
//
//    public static void main(String[] args) {
//        // 执行需要计时的代码
//        String json = "{\"addTime\":{\"date\":30,\"hours\":10,\"seconds\":9,\"month\":4,\"timezoneOffset\":-480,\"year\":123,\"minutes\":17,\"time\":1685413029000,\"day\":2},\"rawProductId\":\"EI022083000491\",\"num\":\"1169\",\"networkName\":\"在线\",\"mapImg\":\"\",\"networkstate\":1,\"infraredName\":\"\",\"id\":25365896,\"mapName\":\"香雪国际公寓停车场\",\"place\":15432,\"power\":13,\"floor\":2,\"firmware\":\"\",\"map\":75,\"hardware\":\"\",\"themeImg\":\"\",\"batteryTime\":{\"date\":31,\"hours\":14,\"seconds\":34,\"month\":4,\"timezoneOffset\":-480,\"year\":123,\"minutes\":32,\"time\":1685514754488,\"day\":3},\"appName\":\"\",\"count\":65,\"mapKey\":\"\",\"fmapID\":\"\",\"license\":\"\",\"x\":1.2634517849411419E7,\"y\":2653464.8567,\"placeName\":\"G020\",\"status\":0}";
//        JSONObject jsonArea1 = JSONObject.fromObject(json);
//        long time = jsonArea1.getJSONObject("batteryTime").getLong("time");
//        Instant instant = Instant.ofEpochMilli(time);
//        ZoneId zone = ZoneId.of("Asia/Shanghai");
//        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
//        LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), zone);
//
//        Duration duration = Duration.between(localDateTime, currentDateTime);
//        long diffInMinutes = Math.abs(duration.toMinutes());
//
//        if (diffInMinutes <= 5) {
//            System.out.println("时间戳对应的北京时间在5分钟之内");
//        } else {
//            System.out.println("时间戳对应的北京时间超过5分钟");
//        }
//
//
//
//    }
//
//    void resetUserMarker(String fid, String x, String y, String floor, Integer map, String placeName, String license) {
//        Calendar beforeTime = Calendar.getInstance();
//        Calendar afterTime = Calendar.getInstance();
//        // 5分钟之前的时间
//        beforeTime.add(Calendar.MINUTE, -3);
//        // 5分钟之前的时间
//        afterTime.add(Calendar.MINUTE, 3);
//
//        List<LicensePos> list = parkMapper.findLicensePosRecent(map, beforeTime.getTime(), afterTime.getTime(), floor, license);
//        if (list != null && list.size() > 0) {
//            for (LicensePos licensePos : list) {
//                double dis = Math.sqrt(Math.pow((Double.valueOf(licensePos.getX()) - Double.valueOf(x).doubleValue()), 2) + Math.pow((Double.valueOf(licensePos.getY()) - Double.valueOf(y).doubleValue()), 2));
//                licensePos.setDis(dis);
//            }
//            Collections.sort(list, new Comparator<LicensePos>() {
//                @Override
//                public int compare(LicensePos o1, LicensePos o2) {
//                    if ((o1.getDis() > o2.getDis())) {
//                        return 1;
//                    }
//                    if (o1.getDis() == o2.getDis()) {
//                        return 0;
//                    }
//                    return -1;
//                }
//            });
//            if(list.get(0).getState()==0) {
//                for (LicensePos licensePos : list) {
//                    if (licensePos.getDis() < 20) {
//                        licensePos.setX(x);
//                        licensePos.setY(y);
//                        licensePos.setY(y);
//                        licensePos.setFid(fid);
//                        licensePos.setFloor(floor + "");
//                        licensePos.setName(placeName);
//                        licensePos.setUpdatetime(new Date());
//                        licensePos.setState(1);
//                        parkMapper.updateLicensePos(licensePos);
//                    }
//                }
//            }
//        }
//
//    }
//
//}
