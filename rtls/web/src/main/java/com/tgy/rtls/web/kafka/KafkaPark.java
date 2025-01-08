// package com.tgy.rtls.web.kafka;
//
// import com.alibaba.fastjson.JSON;
// import com.alibaba.fastjson.JSONObject;
// import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
// import com.tgy.rtls.data.common.KafukaTopics;
// import com.tgy.rtls.data.common.NullUtils;
// import com.tgy.rtls.data.entity.equip.Infrared;
// import com.tgy.rtls.data.entity.park.BeaconCount;
// import com.tgy.rtls.data.entity.park.LicensePos;
// import com.tgy.rtls.data.entity.park.ParkingPlace;
// import com.tgy.rtls.data.entity.park.PlaceUseRecord;
// import com.tgy.rtls.data.entity.park.floorLock.PlaceUnlockRecords;
// import com.tgy.rtls.data.entity.vip.FloorLock;
// import com.tgy.rtls.data.mapper.equip.GatewayMapper;
// import com.tgy.rtls.data.mapper.equip.TagMapper;
// import com.tgy.rtls.data.mapper.park.BookMapper;
// import com.tgy.rtls.data.mapper.park.ParkMapper;
// import com.tgy.rtls.data.mapper.view.ViewMapper;
// import com.tgy.rtls.data.service.common.RedisService;
// import com.tgy.rtls.data.service.lock.impl.RedissonDistributedLocker;
// import com.tgy.rtls.data.service.park.ParkingService;
// import com.tgy.rtls.data.service.park.floorLock.impl.PlaceUnlockRecordsService;
// import com.tgy.rtls.data.service.vip.FloorLockService;
// import com.tgy.rtls.data.service.vip.ParkingInfoStatisticsService;
// import com.tgy.rtls.data.websocket.WebSocketLocation;
// import com.tgy.rtls.web.config.SpringContextHolder;
// import com.tgy.rtls.web.controller.view.AppletsWebSocket;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.kafka.clients.consumer.ConsumerRecord;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.kafka.annotation.KafkaListener;
// import org.springframework.stereotype.Service;
//
// import java.sql.Timestamp;
// import java.text.ParseException;
// import java.text.SimpleDateFormat;
// import java.time.Duration;
// import java.time.Instant;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.time.format.DateTimeFormatter;
// import java.util.*;
// import java.util.concurrent.Executor;
//
// @Service
// @Slf4j
// public class KafkaPark {
//   @Autowired(required = false)
//   private ParkMapper parkMapper;
//   @Autowired(required = false)
//   private GatewayMapper gatewayMapper;
//   @Autowired(required = false)
//   private TagMapper tagMapper;
//   @Autowired(required = false)
//   BookMapper bookMapper;
//   @Autowired(required = false)
//   private ViewMapper viewMapper;
//   @Autowired
//   private RedisService redisService;
//   @Autowired
//   private ParkingService parkingService;
//   @Autowired
//   Executor scheduledExecutorService;
//   @Autowired
//   private WebSocketLocation webSocketLocation;
//   @Autowired
//   private AppletsWebSocket appletsWebSocket;
//   @Autowired
//   private RedissonDistributedLocker redissonDistributedLocker;
//   @Autowired
//   private ParkingInfoStatisticsService parkingInfoStatisticsService;
//   @Autowired
//   private PlaceUnlockRecordsService placeUnlockRecordsService;
//   @Autowired
//   private FloorLockService floorLockService;
//   private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//
//    //网关状态
//   @KafkaListener(topics = {KafukaTopics.LORA_STATE},groupId = "infrared-group2")
//   public void lora(ConsumerRecord<?, ?> record) {
//       try {
//           Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//           if (kafkaMessage.isPresent()) {
//               Object message = kafkaMessage.get();
//               JSONObject data = JSON.parseObject(message.toString());
//               String map = data.getString("map");
//               JSONObject jsonArea = new JSONObject();
//               jsonArea.put("type", 6);
//               jsonArea.put("data", data);
//               jsonArea.put("map", map);
//               webSocketLocation.sendAll(jsonArea.toString());//某个网关的状态数据
//               if (map != null) {
//                   BeaconCount calcut = gatewayMapper.getGateway_loraAcount(Integer.valueOf(map));
//                   jsonArea.put("data", calcut);
//                   jsonArea.put("type", 13);
//                   webSocketLocation.sendAll(jsonArea.toString());//网关的统计数据
//               }
//
//           }
//       } catch (Exception e) {
//           e.printStackTrace();
//       }
//   }
//
//    //车位检测器状态
//   @KafkaListener(topics = {KafukaTopics.INFRARED_STATE},groupId = "#{T(com.tgy.rtls.web.kafka.KafkaPark).generateInfraredGroupId()}")
////     @KafkaListener(topics = "testt1",groupId = "test-1")
//   public void infrared(ConsumerRecord<?, ?> record) throws ParseException {
//       try {
//           ////Instant start = Instant.now();
//           Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//           if (kafkaMessage.isPresent()) {
//               Object message = kafkaMessage.get();
//               JSONObject data = JSON.parseObject(message.toString());
//               if (!"0".equals(data.getString("place")) && data.containsKey("batteryTime") && !"null".equals(data.getString("batteryTime"))) {
//                   LocalDateTime currentDateTime = LocalDateTime.now();
//                   String batteryTimeStr = data.getString("batteryTime");
//                   LocalDateTime dateTime = LocalDateTime.parse(batteryTimeStr,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//
//                   Duration duration = Duration.between(dateTime, currentDateTime);
//                   if(duration.toMinutes() < 1){
//                       String map = data.getString("map");
//                       String jsonString = message.toString();
//                       Infrared infrared = JSON.parseObject(jsonString, Infrared.class);
//                       //log.error("infrared:"+ data);
//                       if(!NullUtils.isEmpty(infrared.getMap())&&infrared.getMap()!=0&&!NullUtils.isEmpty(infrared.getPlace())&&!NullUtils.isEmpty(infrared.getMapName())&&!NullUtils.isEmpty(infrared.getRawProductId())){
//                           redisService.setex("infrared," + infrared.getId(), 1000, "");
//                       }
//                       Integer placeId = infrared.getPlace();
//                       ParkingPlace place;
//                       List<ParkingPlace> places = parkMapper.getPlaceById3(placeId,map);
//                       if ((Optional.ofNullable(places).get().size() == 0)) {
//                           return;
//                       } else {
//                           place = (data.size() == 0 ? null : places.get(0));
//                       }
//                       if (place != null) {
//                           // log.error("Place"+place);
//                           // log.error("batteryTime:"+data.getJSONObject("batteryTime").getLong("time"));
//                           if ("0".equals(infrared.getNetworkstate().toString())) {
//                               ParkingPlace parkingPlace = new ParkingPlace();
//                               parkingPlace.setId(place.getId());
//                               parkingPlace.setState((short) 1);
//                               parkMapper.updatePlace(parkingPlace);
//                           }
//                          parkingService.processPlaceRecord(place,infrared,batteryTimeStr,dateTime,places);
//                       }
//                   }
//               }
//           }
//       } catch (Exception e) {
//           log.error(e.getMessage());
//           e.printStackTrace();
//       }
//   }
//    @KafkaListener(topics = {KafukaTopics.NED_STATE},groupId = "floorLock-state-group")
//    public void floorLock(ConsumerRecord<?, ?> record) throws ParseException {
//        try {
//            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
//            if (kafkaMessage.isPresent()) {
//                Object message = kafkaMessage.get();
//                FloorLock floorLock = JSON.parseObject(message.toString(), FloorLock.class);
//                //log.error("floorLock:"+ data);
//                if(Integer.parseInt(floorLock.getMapId())!=0&&!NullUtils.isEmpty(floorLock.getMapId())&&!NullUtils.isEmpty(floorLock.getMapName())){
//                    String key = "floorLock," + floorLock.getId();
//                    redisService.setex(key, 1000, "");
//                    if("1".equals(floorLock.getFloorLockState())){
// //                        floorLockService.getById()
//                        PlaceUnlockRecords placeUnlockRecords = placeUnlockRecordsService.getOne(new QueryWrapper<PlaceUnlockRecords>().eq("place_id", floorLock.getPlace()));
//                        if(!NullUtils.isEmpty(placeUnlockRecords)){
//                            placeUnlockRecords.setParkingStatus("1");
//                            placeUnlockRecordsService.updateById(placeUnlockRecords);
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
//   public void add(PlaceUseRecord placeRecord,ParkingPlace place,List<ParkingPlace> places) throws ParseException {
//       List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
//       if(NullUtils.isEmpty(placeRecords)){
//           placeRecord.setMap(placeRecord.getMap());
//           placeRecord.setPlace(placeRecord.getPlace());
//           placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
//           bookMapper.addPlaceUseRecord(placeRecord);
//       }
//   }
//
//   public void update(PlaceUseRecord placeRecord,ParkingPlace place) throws ParseException {
//       List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
//       if(!NullUtils.isEmpty(placeRecords)){
//           placeRecord.setId(placeRecords.get(0).getId());
//           LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placeRecord.getTimestamp()), ZoneId.of("Asia/Shanghai"));
//           LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Shanghai"));
//           Duration duration = Duration.between(localDateTime, currentDateTime);
//           long diffInMinutes = Math.abs(duration.toMinutes());
//           if (diffInMinutes < 5) {
//               SpringContextHolder.parkingPlaceConcurrentHashMap.remove(place.getId());
//           }
//           bookMapper.UpdatePlaceUseRecordByid(placeRecord);
//       }
//
//   }
//
//
////    @KafkaListener(topics = {KafukaTopics.INFRARED_STATE},groupId = "#{T(com.tgy.rtls.web.kafka.KafkaPark).generateVisualGroupId()}")
//// //    @KafkaListener(topics ="testt1",groupId = "test-2")
////    public void VisualDataSend(ConsumerRecord<?, ?> record) {
////        try {
////            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
////            if (kafkaMessage.isPresent()) {
////                Object message = kafkaMessage.get();
////                JSONObject data = JSON.parseObject(message.toString());
////                if (!"0".equals(data.getString("place")) && data.containsKey("batteryTime") && !"null".equals(data.getString("batteryTime"))) {
////                    LocalDateTime currentDateTime = LocalDateTime.now();
////                    String batteryTimeStr = data.getString("batteryTime");
////                    LocalDateTime dateTime = LocalDateTime.parse(batteryTimeStr,DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
////
////                    Duration duration = Duration.between(dateTime, currentDateTime);
////                    if(duration.toMinutes() < 1) {
////                        String map = data.getString("map");
////                        String jsonString = message.toString();
////
////                        JSONObject jsonArea = new JSONObject();
////                        jsonArea.put("type", 7);
////                        jsonArea.put("data", data);
////                        jsonArea.put("map", map);
////                        webSocketLocation.sendAll(jsonArea.toString());
//// //                        log.error("VisualDataSend:" + data);
////
////                        Infrared infrared = JSON.parseObject(jsonString, Infrared.class);
////
////                        Integer placeId = infrared.getPlace();
////                        ParkingPlace place;
////                        List<ParkingPlace> places = parkMapper.getPlaceById(placeId);
////                        if ((Optional.ofNullable(places).get().size() == 0)) {
////                            return;
////                        } else {
////                            place = (data.size() == 0 ? null : places.get(0));
////                        }
////
////                        BeaconCount calculate;
////                        JSONObject jsonArea1 = new JSONObject();
////                        jsonArea1.put("map", map);
////                        {
////                            if (map != null) {
////                                calculate = tagMapper.getInfraredAcount(Integer.valueOf(map));
////                                jsonArea1.put("type", 14);
////                                jsonArea1.put("data", calculate);
////                                webSocketLocation.sendAll(jsonArea1.toString());
////                            }
////                        }
////                        if (place != null) {
////                            jsonArea1.put("type", 8);
////                            jsonArea1.put("data", place);
////                            webSocketLocation.sendAll(jsonArea1.toString());
//////                            log.error("map → map={}",map);
////
////                            RealTimeData realTimeData = bookMapper.selectRealTimeData(Integer.valueOf(map));
////                            jsonArea1.put("type", 9);
////                            jsonArea1.put("data", realTimeData);
////                            webSocketLocation.sendAll(jsonArea1.toString());
////
////
////                            ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
////                            if (!NullUtils.isEmpty(realTimeInAndOutData)&&!NullUtils.isEmpty(realTimeInAndOutData.getTime())) {
//////                                log.error("VisualDataSend → record={}", realTimeInAndOutData);
////
////                                long realTimeTimestamp = dateFormat.parse(realTimeInAndOutData.getTime()).getTime();
//////                                log.error("realTimeData → realTimeData={}",realTimeInAndOutData.getTime());
////                                // 获取当前时间的时间戳
////                                long currentTime = System.currentTimeMillis();
////
////                                // 计算30分钟的毫秒数
////                                long thirtyMinutesInMillis = 30 * 60 * 1000;
////                                if (currentTime - realTimeTimestamp <= thirtyMinutesInMillis) {
////                                    jsonArea.put("type", 27);
////                                    jsonArea.put("data", realTimeInAndOutData);
////                                    jsonArea.put("uid", "-1");
////                                    appletsWebSocket.sendAll(jsonArea.toString());
////
////                                    ViewVo oneMapRealTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
////                                    jsonArea.put("type", 18);
////                                    jsonArea.put("data", oneMapRealTimeInAndOutData);
////                                    jsonArea.put("map", place.getMap());
////                                    webSocketLocation.sendAll(jsonArea.toString());
////                                }
////                            }
////                        }
////                    }
////                }
////            }
////        } catch (Exception e) {
////            log.error(e.getMessage());
////            e.printStackTrace();
////        }
////    }
//
//    public static String generateInfraredGroupId() {
//        return "infrared-state-group-" + UUID.randomUUID();
//    }
//    public static String generateVisualGroupId() {
//        return "visual-state-group-" + UUID.randomUUID();
//    }
//
//   public static void main(String[] args) {
//       System.out.println("\"infrared-state-group-\" + UUID.randomUUID() = " + "infrared-state-group-" + UUID.randomUUID());
//       // 执行需要计时的代码
//       String json = "{\"addTime\":{\"date\":30,\"hours\":10,\"seconds\":9,\"month\":4,\"timezoneOffset\":-480,\"year\":123,\"minutes\":17,\"time\":1685413029000,\"day\":2},\"rawProductId\":\"EI022083000491\",\"num\":\"1169\",\"networkName\":\"在线\",\"mapImg\":\"\",\"networkstate\":1,\"infraredName\":\"\",\"id\":25365896,\"mapName\":\"香雪国际公寓停车场\",\"place\":15432,\"power\":13,\"floor\":2,\"firmware\":\"\",\"map\":75,\"hardware\":\"\",\"themeImg\":\"\",\"batteryTime\":{\"date\":31,\"hours\":14,\"seconds\":34,\"month\":4,\"timezoneOffset\":-480,\"year\":123,\"minutes\":32,\"time\":1685514754488,\"day\":3},\"appName\":\"\",\"count\":65,\"mapKey\":\"\",\"fmapID\":\"\",\"license\":\"\",\"x\":1.2634517849411419E7,\"y\":2653464.8567,\"placeName\":\"G020\",\"status\":0}";
//       JSONObject jsonArea1 = JSON.parseObject(json);
//       long time = jsonArea1.getJSONObject("batteryTime").getLong("time");
//       Instant instant = Instant.ofEpochMilli(time);
//       ZoneId zone = ZoneId.of("Asia/Shanghai");
//       LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
//       LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), zone);
//
//       Duration duration = Duration.between(localDateTime, currentDateTime);
//       long diffInMinutes = Math.abs(duration.toMinutes());
//
//       if (diffInMinutes <= 5) {
//           System.out.println("时间戳对应的北京时间在5分钟之内");
//       } else {
//           System.out.println("时间戳对应的北京时间超过5分钟");
//       }
//
//   }
//
//   void resetUserMarker(String fid, String x, String y, String floor, Integer map, String placeName, String license) {
//       Calendar beforeTime = Calendar.getInstance();
//       Calendar afterTime = Calendar.getInstance();
//       // 5分钟之前的时间
//       beforeTime.add(Calendar.MINUTE, -3);
//       // 5分钟之前的时间
//       afterTime.add(Calendar.MINUTE, 3);
//
//       List<LicensePos> list = parkMapper.findLicensePosRecent(map, beforeTime.getTime(), afterTime.getTime(), floor, license);
//       if (list != null && list.size() > 0) {
//           for (LicensePos licensePos : list) {
//               double dis = Math.sqrt(Math.pow((Double.valueOf(licensePos.getX()) - Double.valueOf(x).doubleValue()), 2) + Math.pow((Double.valueOf(licensePos.getY()) - Double.valueOf(y).doubleValue()), 2));
//               licensePos.setDis(dis);
//           }
//           Collections.sort(list, new Comparator<LicensePos>() {
//               @Override
//               public int compare(LicensePos o1, LicensePos o2) {
//                   if ((o1.getDis() > o2.getDis())) {
//                       return 1;
//                   }
//                   if (o1.getDis() == o2.getDis()) {
//                       return 0;
//                   }
//                   return -1;
//               }
//           });
//           if(list.get(0).getState()==0) {
//               for (LicensePos licensePos : list) {
//                   if (licensePos.getDis() < 20) {
//                       licensePos.setX(x);
//                       licensePos.setY(y);
//                       licensePos.setY(y);
//                       licensePos.setFid(fid);
//                       licensePos.setFloor(floor + "");
//                       licensePos.setName(placeName);
//                       licensePos.setUpdatetime(new Date());
//                       licensePos.setState(1);
//                       parkMapper.updateLicensePos(licensePos);
//                   }
//               }
//           }
//       }
//
//   }
//
// }
