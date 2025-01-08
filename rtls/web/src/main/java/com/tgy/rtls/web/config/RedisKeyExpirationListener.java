package com.tgy.rtls.web.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.equip.InfraredState;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVideoDetection;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.BaseService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.service.user.PersonService;
import com.tgy.rtls.data.service.user.impl.MaintenanceStaffService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import com.tgy.rtls.web.util.AlarmNotificationUtil;
import net.sf.json.JSONObject;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 许强
 * @Package com.example.config.lister
 * @date 2020/2/26
 * redis超时回调
 */
@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TagService tagService;
    @Autowired
    private GatewayService gatewayService;

    @Autowired
    private PersonService personService;

    @Autowired
    private WebSocketLocation webSocketLocation;

    @Autowired
    private BaseService baseService;
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired
    private FloorLockService floorLockService;
    @Autowired
    private CameraConfigService cameraConfigService;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;
    @Autowired
    private PlaceVideoDetectionService detectionService;
    @Autowired
    private MaintenanceStaffService maintenanceStaffService;
    @Autowired
    private AlarmNotificationUtil alarmNotificationUtil;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private VipParkingService vipParkingService;
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message 消息
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        try {
            String expiredKey = message.toString();
            String[] split = expiredKey.split(",");
            // 检查split数组长度
            if (split.length < 2) {
//                log.error("onMessage → Invalid message format:{}", message);
                return;
            }
            String deviceType = split[0];
            String deviceId = split[1];
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String currentTime = dateFormat.format(new Date());

            switch (deviceType) {
                case "tag":
                    handleTagOffline(deviceId, currentTime);//标签离线
                    break;
                case "base":
                    handleBaseOffline(deviceId);//微基站离线
                    break;
                case "infrared":
                    handleInfraredOffline(deviceId, currentTime);
                    break;
                case "floorLock":
                    handleFloorLockOffline(deviceId, currentTime);
                    break;
                case "heartbeat":
                    handleCameraOffline(deviceId);
                    break;
                case "a_service:last_upload_time":
                    handleLastUploadTimeExpired(deviceId);
                    break;
                case "vip:parking:expire":
                    handleVipPlaceExpired(deviceId);
                    break;
                case "vip:parking:active":
                    handleVipPlaceTakeEffect(deviceId);
                    break;
                default:
                    System.out.println("Unknown device type: " + deviceType);
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleVipPlaceExpired(String deviceId) {
        VipParking vipParking = new VipParking();
        vipParking.setId(Long.valueOf(deviceId));
        vipParking.setStatus(0);
        vipParkingService.editVipParingSpaceInfo(vipParking);
    }

    private void handleVipPlaceTakeEffect(String deviceId) {
        VipParking vipParking = new VipParking();
        vipParking.setId(Long.valueOf(deviceId));
        vipParking.setStatus(1);
        vipParkingService.editVipParingSpaceInfo(vipParking);
    }

    // New method to handle 'a_service:last_upload_time' expiration and update status
    private void handleLastUploadTimeExpired(String map) {
        try {
            // Assuming deviceId is the ID for a video place, and you need to set the status as '异常'
            detectionService.updateStatusToError(map);

            DeviceAlarms deviceAlarms = new DeviceAlarms();
            deviceAlarms.setEquipmentType(9);
            deviceAlarms.setAlarmType(1);
            deviceAlarms.setPriority(1);
            deviceAlarms.setState(0);
            deviceAlarms.setNum(Integer.valueOf(map));
            deviceAlarms.setDeviceId(Integer.valueOf(map));
            deviceAlarms.setMap(Integer.valueOf(map));
            DeviceAlarms existingAlarm = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                    .ne("state", 1)
                    .eq("device_id", map)
                    .eq("num", map)
                    .eq("equipment_type", 9)
                    .isNull("end_time"));

            boolean isNewAlarm = false;  // 标记是否是新的报警

            if (!NullUtils.isEmpty(existingAlarm)) {
                deviceAlarms.setId(existingAlarm.getId());
            } else {
                isNewAlarm = true;  // 没有现有报警，说明是新报警
            }

            if (NullUtils.isEmpty(existingAlarm) || NullUtils.isEmpty(existingAlarm.getStartTime())) {
                deviceAlarms.setStartTime(LocalDateTime.now());
            }
            deviceAlarm(deviceAlarms, NullUtils.isEmpty(existingAlarm) ? null : existingAlarm.getPriority(), existingAlarm);
            // 如果是新报警，则发送短信通知
            if (isNewAlarm) {
                PlaceVideoDetection placeStatus = detectionService.getOne(new QueryWrapper<PlaceVideoDetection>().eq("map", map));

                if (!NullUtils.isEmpty(placeStatus)) {
                    Set<Integer> mapIds = new HashSet<>();
                    mapIds.add(Integer.valueOf(map));
                    alarmNotificationUtil.sendAlarmNotifications(mapIds,"SMS_475845157",1,9, 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Log the exception for better visibility
            // log.error("Error handling last_upload_time expiration for deviceId: {}", deviceId, e);
        }
    }

    private void handleCameraOffline(String serialNumber) {
        CameraConfig cameraConfig = cameraConfigService.getOne(new QueryWrapper<CameraConfig>().eq("serial_number", serialNumber));
        if(!NullUtils.isEmpty(cameraConfig)) {
            cameraConfig.setNetworkState(0);
            cameraConfig.setUpdateTime(LocalDateTime.now());
            if (cameraConfigService.updateById(cameraConfig)) {
                DeviceAlarms deviceAlarms = new DeviceAlarms();
                deviceAlarms.setEquipmentType(5);
                deviceAlarms.setAlarmType(1);
                deviceAlarms.setPriority(1);
                deviceAlarms.setSerialNumber(cameraConfig.getSerialNumber());
                deviceAlarms.setState(0);
                deviceAlarms.setDeviceId(Math.toIntExact(cameraConfig.getId()));
                deviceAlarms.setMap(Integer.valueOf(cameraConfig.getMap()));
                DeviceAlarms device = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                        .ne("state", 1)
                        .eq("device_id", cameraConfig.getId())
                        .eq("equipment_type", 5)
                        .isNull("end_time"));
                if (!NullUtils.isEmpty(device)) {
                    deviceAlarms.setId(device.getId());
                }
                if (NullUtils.isEmpty(device) || NullUtils.isEmpty(device.getStartTime())) {
                    deviceAlarms.setStartTime(LocalDateTime.now());
                }
                deviceAlarm(deviceAlarms, NullUtils.isEmpty(device) ? null : device.getPriority(), device);
            }
        }
    }

    private void handleFloorLockOffline(String deviceId, String currentTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        FloorLock floorLock = floorLockService.getById(deviceId);
        if(!NullUtils.isEmpty(floorLock)){
            floorLock.setState(1);
            floorLock.setNetworkstate((byte) 0);
            floorLock.setOfflineTime(LocalDateTime.parse(currentTime, formatter));
            floorLockService.updateById(floorLock);
//            String placeId = String.valueOf(floorLock.getPlace());
//            if(!NullUtils.isEmpty(floorLock.getPlace())){
//                tagMapper.updatePlace(placeId.split(","),1);
//
//            }
        }
    }

    private void handleTagOffline(String tagId, String currentTime) {
        //修改标签电压检测时间为当前时间且离线
        tagService.updateTagStatus(tagId,0);
        tagService.updateTagBatteryTime(tagId,null,currentTime);
        //判断该标签绑定的人员是否在井下 在-->更新离线时间
        Person person=personService.findByTagNum(tagId);
        if (person != null && person.getMinestate() == 0) {
            personService.updatePersonOff(person.getId(), currentTime);
        }

        sendTagOfflineMessage(tagId, person);
        updateRedisTagStatus(tagId, 0);
    }

    private void sendTagOfflineMessage(String tagId, Person person) {
        JSONObject data = new JSONObject();
        data.put("tagid", tagId);
        data.put("status", 0);

        JSONObject object = new JSONObject();
        object.put("type", 1);
        object.put("data", data);
        object.put("map", person != null ? person.getMap() : null);
        webSocketLocation.sendAll(object.toString());

        if (person != null) {
            JSONObject objectData = new JSONObject();
            objectData.put("counttype", 2);
            objectData.put("count", personService.findByOff(person.getMap()));

            JSONObject objectCount = new JSONObject();
            objectCount.put("data", objectData);
            objectCount.put("type", 3);
            objectCount.put("map", person.getMap());
            webSocketLocation.sendAll(objectCount.toString());
        }
    }

    private void updateRedisTagStatus(String tagId, int status) {
        String value = redisService.get("tag:" + tagId);
        if (value != null) {
            JSONObject object = JSONObject.fromObject(value);
            object.put("status", status);
            redisService.set("tag:" + tagId, object.toString());
        }
    }
    private void handleBaseOffline(String baseId) {
        baseService.updateBaseNetworkstate(baseId, 0);
        redisService.remove("expired:" + baseId);
    }

    private void handleInfraredOffline(String infraredId, String currentTime) {
        List<Infrared> infrareds = tagMapper.findIredByIdAndName(Integer.valueOf(infraredId), null, null);
        if (infrareds == null || infrareds.isEmpty()) {
            return;
        }

        Infrared infrared = infrareds.get(0);
        infrared.setNetworkstate((short) 0);
        tagMapper.updateInfrared(infrared);

        InfraredState infraredState = new InfraredState();
        infraredState.setState("0");
        infraredState.setInfrarednum(infrared.getNum());
        gatewayService.addInfraredState(infraredState);

        infrared.setNetworkName("离线");

        List<ParkingPlace> parkingPlaces = tagMapper.getPlace(infrared.getNum());
        if (parkingPlaces != null && !parkingPlaces.isEmpty()) {
            String[] idString = parkingPlaces.stream()
                    .map(ParkingPlace::getId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")).split(",");
            tagMapper.updatePlace(idString, 1);
        }

        sendInfraredOfflineMessage(infrared);
        redisService.remove("expired:" + infraredId);
    }

    private void sendInfraredOfflineMessage(Infrared infrared) {
        JSONObject jsonArea = new JSONObject();
        jsonArea.put("type", 7);
        jsonArea.put("data", infrared);
        jsonArea.put("map", infrared.getMap());
        webSocketLocation.sendAll(jsonArea.toString());
    }

    private void deviceAlarm(DeviceAlarms deviceAlarms, Integer lastTimePriority, DeviceAlarms lastTimeDeviceAlarms) {
        if (!NullUtils.isEmpty(lastTimePriority) && !NullUtils.isEmpty(deviceAlarms.getPriority()) && !deviceAlarms.getPriority().equals(lastTimePriority) && deviceAlarms.getState() == 0) {
            deviceAlarms.setStartTime(LocalDateTime.now());
        }
        if (NullUtils.isEmpty(lastTimeDeviceAlarms) || !NullUtils.isEmpty(lastTimeDeviceAlarms.getEndTime())) {
            deviceAlarmsService.save(deviceAlarms);
        } else {
            deviceAlarmsService.updateById(deviceAlarms);
        }
    }
}
