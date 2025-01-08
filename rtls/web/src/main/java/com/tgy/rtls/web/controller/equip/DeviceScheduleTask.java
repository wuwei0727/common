//package com.tgy.rtls.web.controller.equip;
//
//import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
//import com.tgy.rtls.data.common.NullUtils;
//import com.tgy.rtls.data.entity.equip.DeviceAlarms;
//import com.tgy.rtls.data.entity.equip.DeviceVo;
//import com.tgy.rtls.data.entity.equip.Infrared;
//import com.tgy.rtls.data.entity.park.ParkingPlace;
//import com.tgy.rtls.data.entity.view.ViewVo2;
//import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;
//import com.tgy.rtls.data.entity.warn.ParkingAlertConfig;
//import com.tgy.rtls.data.entity.warn.WhitelistSlots;
//import com.tgy.rtls.data.mapper.equip.DeviceAlarmsMapper;
//import com.tgy.rtls.data.mapper.equip.SubMapper;
//import com.tgy.rtls.data.mapper.equip.TagMapper;
//import com.tgy.rtls.data.mapper.park.ParkMapper;
//import com.tgy.rtls.data.service.equip.GatewayService;
//import com.tgy.rtls.data.service.equip.SubService;
//import com.tgy.rtls.data.service.park.ParkingService;
//import com.tgy.rtls.data.service.park.SmsQuotaService;
//import com.tgy.rtls.data.service.sms.ALiYunSmsService;
//import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
//import com.tgy.rtls.data.service.warn.AlarmPersonnelBindingsService;
//import com.tgy.rtls.data.service.warn.ParkingAlertConfigService;
//import com.tgy.rtls.data.service.warn.WhitelistSlotsService;
//import com.tgy.rtls.data.service.warn.impl.HolidayServiceImpl;
//import com.tgy.rtls.web.util.AlarmNotificationUtil;
//import io.swagger.annotations.Api;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.function.BiFunction;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.web.controller.equip
// * @Author: wuwei
// * @CreateTime: 2022-12-14 15:00
// * @Description: TODO
// * @Version: 1.0
// */
//@RestController
//@RequestMapping(value = "/task")
//@CrossOrigin
//@Api(value = "蓝牙巡检定时报警任务")
//@Slf4j
//public class DeviceScheduleTask {
//    private static final String API_URL = "http://timor.tech/api/holiday/info/";
//    private final int time = 60;
//    @Autowired
//    private RedissonClient redissonClient;
//    @Autowired
//    private SubService subService;
//    @Autowired
//    private TagMapper tagMapper;
//    @Autowired
//    private DeviceAlarmsService deviceAlarmsService;
//    @Autowired
//    private GatewayService gatewayService;
//    @Autowired
//    private ParkingService parkingService;
//    @Autowired
//    private ParkMapper parkMapper;
//    @Autowired
//    private ParkingAlertConfigService parkingAlertConfigService;
//    @Autowired
//    private WhitelistSlotsService whitelistSlotsService;
//    @Autowired
//    private HolidayServiceImpl holidayService;
//    @Autowired
//    private SmsQuotaService smsQuotaService;
//    @Autowired
//    private ALiYunSmsService aLiYunSmsService;
//    @Autowired
//    private AlarmPersonnelBindingsService alarmPersonnelBindingsService;
//    @Autowired
//    private AlarmNotificationUtil alarmNotificationUtil;
//    private final String CODE="SMS_474890660";
//    private final Integer COUNT=1;
//    private final Integer PRIORITY=1;
//
//    private static DeviceAlarms deviceAlarm(DeviceAlarms deviceAlarms, Integer lastTimePriority) {
//        // 设置必要的属性
//        if (!NullUtils.isEmpty(lastTimePriority) && !NullUtils.isEmpty(deviceAlarms.getPriority()) && !deviceAlarms.getPriority().equals(lastTimePriority) && deviceAlarms.getState() == 0) {
//            deviceAlarms.setStartTime(LocalDateTime.now());
//        }
//        // 返回更新后的 DeviceAlarms 对象以便进一步处理
//        return deviceAlarms;
//    }
//
//    /**
//     * 先判断当天是否为节假日
//     * isHoliday为true（周末和节日）
//     * isHoliday为false（工作日和调休）
//     */
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @GetMapping("/placeWarningTaskT3")
//    public void placeWarningTaskT3() {
//        String lockKey = "lock:placeWarnT3";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                boolean isHoliday = holidayService.isHoliday();
//
//                List<ParkingAlertConfig> t3Configs = parkingAlertConfigService.list(
//                        new QueryWrapper<ParkingAlertConfig>()
//                                .isNotNull(isHoliday ? "holiday_t3_period_hours" : "t3_period_hours")
//                                .isNotNull(isHoliday ? "holiday_t3_slot_change_limit" : "t3_slot_usage_limit")
//                                .eq("status", 1));
//
//                if (NullUtils.isEmpty(t3Configs)) {
//                    return;
//                }
//                List<String> placeIdStrings = whitelistSlotsService.list(new QueryWrapper<WhitelistSlots>().select("place_id"))
//                        .stream()
//                        .map(WhitelistSlots::getPlaceId)
//                        .collect(Collectors.toList());
//                List<Integer> whitelistPlaceIds = placeIdStrings.stream()
//                        .flatMap(str -> Arrays.stream(str.split(",")))
//                        .map(String::trim)
//                        .map(Integer::parseInt)
//                        .collect(Collectors.toList());
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(8, null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                LocalDateTime now = LocalDateTime.now();
//                Set<Integer> mapNeedNotify = new HashSet<>();
//                t3Configs.forEach(config -> {
//                    // 查询超过阈值的map
//                    List<ViewVo2> alertList = parkingAlertConfigService.getChangedPlacesInT3Period(
//                            config.getMap(),
//                            isHoliday ? config.getHolidayT3PeriodHours() : config.getT3PeriodHours(),
//                            isHoliday ? config.getHolidayT3SlotChangeLimit() : config.getT3SlotUsageLimit(),
////                            "minute"
//                            "hour"
//                    );
//                    if (!NullUtils.isEmpty(alertList)) {
//                        for (ViewVo2 place : alertList) {
//                            Integer placeId = Integer.valueOf(place.getPlaceName());
//                            if (whitelistPlaceIds.contains(placeId)) {
//                                return;
//                            }
//
//                            DeviceAlarms newDeviceAlarms = new DeviceAlarms();
//                            newDeviceAlarms.setDeviceId(placeId);
//                            newDeviceAlarms.setMap(config.getMap());
//                            newDeviceAlarms.setEquipmentType(8);
//                            newDeviceAlarms.setAlarmType(8);
//                            newDeviceAlarms.setState(0);
//                            newDeviceAlarms.setPriority(1);
//
//                            DeviceAlarms existingAlarm = existingAlarmsMap.get(placeId);
//                            boolean isNewAlarm = existingAlarm == null;
//                            if (existingAlarm != null) {
//                                newDeviceAlarms.setId(existingAlarm.getId());
//                            }
//                            if (isNewAlarm || existingAlarm.getStartTime() == null) {
//                                newDeviceAlarms.setStartTime(LocalDateTime.now());
//                                // 记录需要发送通知的地图
//                                mapNeedNotify.add(Integer.valueOf(place.getMap()));
//                            }
//                            deviceAlarmsList.add(newDeviceAlarms);
//                        }
//                    }
//                });
//
//                if (!deviceAlarmsList.isEmpty()) {
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//
//                if (!mapNeedNotify.isEmpty()) {
//                    alarmNotificationUtil.sendAlarmNotifications(mapNeedNotify,CODE,COUNT,8, PRIORITY);
//                }
//
//            } else {
//                log.error("Failed to acquire lock for the startSubTask method.");
//            }
//        } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    /**
//      * 先判断当天是否为节假日
//      * isHoliday为true（周末和节日）
//      * isHoliday为false（工作日和调休）
//    */
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @GetMapping("/placeWarningTaskT2")
//    public void placeWarningTaskT2() {
//        String lockKey = "lock:placeWarnT2";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                boolean isHoliday = holidayService.isHoliday();
//
//
//                List<ParkingAlertConfig> t2Configs = parkingAlertConfigService.list(
//                        new QueryWrapper<ParkingAlertConfig>()
//                                .isNotNull(isHoliday ? "holiday_t2_period_hours" : "t2_period_hours")
//                                .isNotNull(isHoliday ? "holiday_t2_slot_change_limit" : "t2_slot_change_limit")
//                                .eq("status", 1));
//
//                if (NullUtils.isEmpty(t2Configs)) {
//                    return;
//                }
//
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(7, null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                LocalDateTime now = LocalDateTime.now();
//                Set<Integer> mapNeedNotify = new HashSet<>();
//                t2Configs.forEach(config -> {
//                    // 获取变化的具体车位信息
//                    List<ViewVo2> changedPlaces = parkingAlertConfigService.getChangedPlacesInT2Period(
//                            config.getMap(),
//                            isHoliday ? config.getHolidayT2PeriodHours() : config.getT2PeriodHours(),
//                            isHoliday ? config.getHolidayT2SlotChangeLimit() : config.getT2SlotChangeLimit(),
////                   "minute"
//                            "hour"
//                    );
//
//                    if (!NullUtils.isEmpty(changedPlaces)) {
//                        for (ViewVo2 place : changedPlaces) {
//                            if (changedPlaces.size() > (isHoliday ? config.getHolidayT2SlotChangeLimit() : config.getT2SlotChangeLimit())) {
//                                Integer placeId = Integer.valueOf(place.getPlaceName());
//
//                                DeviceAlarms newDeviceAlarms = new DeviceAlarms();
//                                newDeviceAlarms.setDeviceId(placeId);
//                                newDeviceAlarms.setMap(config.getMap());
//                                newDeviceAlarms.setEquipmentType(7);
//                                newDeviceAlarms.setAlarmType(7);
//                                newDeviceAlarms.setState(0);
//                                newDeviceAlarms.setPriority(1);
//
//                                DeviceAlarms existingAlarm = existingAlarmsMap.get(placeId);
//                                boolean isNewAlarm = existingAlarm == null;
//                                if (existingAlarm != null) {
//                                    newDeviceAlarms.setId(existingAlarm.getId());
//                                }
//                                if (isNewAlarm || existingAlarm.getStartTime() == null) {
//                                    newDeviceAlarms.setStartTime(LocalDateTime.now());
//                                    // 记录需要发送通知的地图
//                                    mapNeedNotify.add(Integer.valueOf(place.getMap()));
//                                }
//
//                                deviceAlarmsList.add(newDeviceAlarms);
//                            }
//                        }
//                    }
//                });
//
//                if (!deviceAlarmsList.isEmpty()) {
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//
//                if (!mapNeedNotify.isEmpty()) {
//                    alarmNotificationUtil.sendAlarmNotifications(mapNeedNotify,CODE,COUNT,7, PRIORITY);
//                }
//            }
//       } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @GetMapping("/placeWarningTaskT1")
//    public void placeWarningTaskT1() {
//        String lockKey = "lock:placeWarnT1";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                List<ParkingPlace> placeList = parkMapper.getPlaceListByPlaceId(null, null, null, null);
//
//                List<String> placeIdStrings = whitelistSlotsService.list(new QueryWrapper<WhitelistSlots>().select("place_id"))
//                        .stream()
//                        .map(WhitelistSlots::getPlaceId)
//                        .collect(Collectors.toList());
//
//                List<Integer> whitelistPlaceIds = placeIdStrings.stream()
//                        .flatMap(str -> Arrays.stream(str.split(",")))
//                        .map(String::trim)
//                        .map(Integer::parseInt)
//                        .collect(Collectors.toList());
//
//                List<Integer> mapList = placeList.stream().map(ParkingPlace::getMap).distinct().collect(Collectors.toList());
//
//
//
//                Map<Integer, ParkingAlertConfig> alertConfigMap = parkingAlertConfigService.list(
//                        new QueryWrapper<ParkingAlertConfig>().in("map", mapList)
//                        .isNotNull("t1_threshold_hours")
//                        .eq("status", 1))
//                        .stream().collect(Collectors.toMap(ParkingAlertConfig::getMap, Function.identity()));
//
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(6, null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                List<DeviceAlarms> alarmsToEnd = new ArrayList<>(); // 新增：用于存储需要结束报警的设备
//
//                if (NullUtils.isEmpty(alertConfigMap)) {
//                    return;
//                }
//                LocalDateTime now = LocalDateTime.now();
//
//                Set<Integer> mapNeedNotify = new HashSet<>();
//
//                placeList.forEach(place -> {
//                    if (whitelistPlaceIds.contains(place.getId())) {
//                        return;
//                    }
//
//                    ParkingAlertConfig one = alertConfigMap.get(place.getMap());
//                    if (!NullUtils.isEmpty(one)) {
//                        long timeThreshold = one.getT1ThresholdHours();
//                        DeviceAlarms existingAlarm = existingAlarmsMap.get(place.getId());
//
//                        DeviceAlarms newDeviceAlarms = new DeviceAlarms();
//                        newDeviceAlarms.setDeviceId(place.getId());
//                        newDeviceAlarms.setMap(place.getMap());
//                        newDeviceAlarms.setEquipmentType(6);
//                        newDeviceAlarms.setAlarmType(6);
//                        newDeviceAlarms.setState(0);
//                        newDeviceAlarms.setPriority(1);
//
//
//                        if (place.getUpdateTime() == null || Duration.between(place.getUpdateTime(), now).toMinutes() > timeThreshold) {
//                            boolean isNewAlarm = existingAlarm == null;
//                            if (existingAlarm != null) {
//                                newDeviceAlarms.setId(existingAlarm.getId());
//                            }
//                            if (isNewAlarm || existingAlarm.getStartTime() == null) {
//                                newDeviceAlarms.setStartTime(LocalDateTime.now());
//                                // 记录需要发送通知的地图
//                                mapNeedNotify.add(place.getMap());
//                            }
//                            deviceAlarm(newDeviceAlarms, existingAlarm == null ? null : existingAlarm.getPriority());
//                            deviceAlarmsList.add(newDeviceAlarms);
//                        } else if (existingAlarm != null) {
//                            // 如果 updateTime 是最新的，且存在现有报警，则结束报警
//                            existingAlarm.setState(1);
//                            existingAlarm.setEndTime(now);
//                            deviceAlarm(newDeviceAlarms, existingAlarm.getPriority());
//                            alarmsToEnd.add(existingAlarm);
//
//                        }
//                    }
//
//                });
//
//                if (!deviceAlarmsList.isEmpty()) {
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                    parkingService.batchUpdateOrInsert(alarmsToEnd, DeviceAlarmsMapper.class, function);
//                }
//
//                if (!mapNeedNotify.isEmpty()) {
//                    alarmNotificationUtil.sendAlarmNotifications(mapNeedNotify,CODE,COUNT,6, PRIORITY);
//
//                }
//
//            } else {
//                log.error("Failed to acquire lock for the startSubTask method.");
//            }
//        } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    private void sendAlarmNotifications(Set<Integer> mapIds, Integer equipmentType, Integer priority) {
//        for (Integer mapId : mapIds) {
//            try {
//                List<AlarmPersonnelBindings> bindings = alarmPersonnelBindingsService.getBindingsByCondition(mapId, equipmentType, priority);
//
//                if (NullUtils.isEmpty(bindings)) {
//                    log.warn("No maintenance staff found for map: {}, equipmentType: {}, priority: {}",
//                            mapId, equipmentType, priority);
//                    continue;
//                }
//
//                // 先检查并扣除短信配额
//                if (!smsQuotaService.deductQuota(mapId, 1)) {
//                    log.warn("Failed to deduct SMS quota for map: {}", mapId);
//                    continue;
//                }
//
//                Map<String, Object> templateParams = new HashMap<>();
//                templateParams.put("mapName", bindings.get(0).getMapName());
//                templateParams.put("desc", bindings.get(0).getAlarmTypeNames());
//
//                // 发送短信给每个维护人员
//                for (AlarmPersonnelBindings binding : bindings) {
//                    try {
//                        aLiYunSmsService.sendMessage(binding.getPhone().trim(),"SMS_474890660",templateParams);
//                    } catch (Exception e) {
//                        log.error("Failed to send SMS to phone: " + binding.getPhone(), e);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("Error sending alarm notifications for map: " + mapId, e);
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @Async(value = "SubAsyncExecutor")
//    @GetMapping(value = "/startSubTask")
//    public void startSubTask() {
//        String lockKey = "lock:subTask";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                Instant start = Instant.now();
//                List<DeviceVo> subList = subService.substationBatteryTimeWarningLevelsQuery();
//                log.error("substationBatteryTimeWarningLevelsQuery执行时间：" + Duration.between(start, Instant.now()).toMillis() + "毫秒");
//                log.error("subList大小：" + subList.size());
//
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(1,null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                List<Integer> idsToUpdateOffline = new ArrayList<>();
//
//                subList.forEach(sub -> {
//                    DeviceAlarms deviceAlarms = processDevice(sub, existingAlarmsMap, 1, idsToUpdateOffline);
//                    if (deviceAlarms != null) {
//                        deviceAlarmsList.add(deviceAlarms);
//                    }
//                });
//                if (!idsToUpdateOffline.isEmpty()) {
//                    BiFunction<Integer, SubMapper, Void> function = (id, mapper) -> {
//                        if (id != null) {
//                            mapper.updateSubForOffline(id);
//                        }
//                        return null;
//                    };
//                   parkingService.batchUpdateOrInsert(idsToUpdateOffline, SubMapper.class, function);
//
//                }
//                if(!deviceAlarmsList.isEmpty()){
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//
//                log.error("subList处理时间：" + Duration.between(start, Instant.now()).toMillis() + "毫秒");
//            } else {
//                log.error("Failed to acquire lock for the startSubTask method.");
//            }
//        } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @Async(value = "SubAsyncExecutor")
//    @GetMapping(value = "/startGatewayTask")
//    public void startGatewayTask() {
//        String lockKey = "lock:gatTask";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                List<DeviceVo> gatewayList = gatewayService.gatewayBatteryTimeWarningLevelsQuery();
//
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(2, null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                List<Integer> idsToUpdateGatewayState = new ArrayList<>();
//
//                gatewayList.forEach(gateway -> {
//                    DeviceAlarms deviceAlarms = processDevice(gateway, existingAlarmsMap, 2,idsToUpdateGatewayState);
//                    if (deviceAlarms != null) {
//                        deviceAlarmsList.add(deviceAlarms);
//                    }
//                });
//
//                if (!idsToUpdateGatewayState.isEmpty()) {
//                    BiFunction<Integer, SubMapper, Void> function = (id, mapper) -> {
//                        if (id != null) {
//                            mapper.updateSubForOffline(id);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(idsToUpdateGatewayState, SubMapper.class, function);
//
//                }
//
//                if(!deviceAlarmsList.isEmpty()){
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//
//            } else {
//                log.error("Failed to acquire lock for the startSubTask method.");
//            }
//        } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 0/5 * * * ?")
//    @Async(value = "SubAsyncExecutor")
//    @GetMapping(value = "/startInfraredTask")
//    public void startInfraredTask() {
//        String lockKey = "lock:infTask";
//        RLock lock = redissonClient.getLock(lockKey);
//        boolean lockAcquired = false;
//        try {
//            lockAcquired = lock.tryLock(time, TimeUnit.SECONDS);
//            if (lockAcquired) {
//                Instant start1 = Instant.now();
//                List<DeviceVo> infraredList = tagMapper.infraredBatteryTimeWarningLevelsQuery();
//                Instant end1 = Instant.now();
//                log.error("infraredList SQL耗时：" + Duration.between(start1, end1).toMillis() + "毫秒");
//
//                Map<Integer, DeviceAlarms> existingAlarmsMap = deviceAlarmsService.getExistingAlarmsForDevices(3, null);
//                List<DeviceAlarms> deviceAlarmsList = new ArrayList<>();
//                List<Integer> idsToUpdateGatewayState = new ArrayList<>();
//
//
//                Instant infraredListStart = Instant.now();
//                infraredList.forEach(infrared -> {
//                    DeviceAlarms deviceAlarms = processDevice(infrared, existingAlarmsMap, 3, idsToUpdateGatewayState);
//                    if (deviceAlarms != null) {
//                        deviceAlarmsList.add(deviceAlarms);
//                    }
//                });
//
//                if(!idsToUpdateGatewayState.isEmpty()){
//                    // 批量更新或插入操作
//                    BiFunction<Integer, TagMapper, Void> function = (id, mapper) -> {
//                        if (id!= null) {
//                            mapper.updateInfraredStateBecomesLowPower(id,0,null);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(idsToUpdateGatewayState, TagMapper.class, function);
//                }
//
//                if(!deviceAlarmsList.isEmpty()){
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//
//
//                if(!deviceAlarmsList.isEmpty()){
//                    // 批量更新或插入操作
//                    BiFunction<DeviceAlarms, DeviceAlarmsMapper, Void> function = (device, mapper) -> {
//                        if (device.getId() == null) {
//                            mapper.insert(device);
//                        } else {
//                            mapper.updateById(device);
//                        }
//                        return null;
//                    };
//                    parkingService.batchUpdateOrInsert(deviceAlarmsList, DeviceAlarmsMapper.class, function);
//                }
//                log.error("infraredList 处理结束，耗时：" + Duration.between(infraredListStart, Instant.now()).toMillis() + "毫秒");
//            } else {
//                log.error("Failed to acquire lock for the startSubTask method.");
//            }
//        } catch (Exception e) {
//            log.error("Error during startSubTask execution", e);
//            throw new RuntimeException(e);
//        } finally {
//            if (lockAcquired) {
//                lock.unlock(); // 释放锁
//            }
//        }
//    }
//
//    private DeviceAlarms processDevice(DeviceVo deviceVo, Map<Integer, DeviceAlarms> existingAlarmsMap, int equipmentType,List<Integer> idsToUpdateOffline) {
//        DeviceAlarms existingAlarm = existingAlarmsMap.get(deviceVo.getId());
//        DeviceAlarms newDeviceAlarms = new DeviceAlarms();
//        newDeviceAlarms.setDeviceId(deviceVo.getId());
//        newDeviceAlarms.setNum(deviceVo.getNum());
//        newDeviceAlarms.setMap(deviceVo.getMap());
//        newDeviceAlarms.setEquipmentType(equipmentType);
//        newDeviceAlarms.setAlarmType(1);
//        newDeviceAlarms.setState(0);
//        newDeviceAlarms.setPriority(deviceVo.getLevel());
//
//        if (existingAlarm != null) {
//            newDeviceAlarms.setId(existingAlarm.getId());
//        }
//        if (existingAlarm == null || existingAlarm.getStartTime() == null) {
//            newDeviceAlarms.setStartTime(LocalDateTime.now());
//        }
//
//        switch (deviceVo.getLevel()) {
//            case 1:
//            case 2:
//            case 3:
//                idsToUpdateOffline.add(deviceVo.getId());
//                newDeviceAlarms.setPriority(deviceVo.getLevel());
//                deviceAlarm(newDeviceAlarms, existingAlarm == null ? null : existingAlarm.getPriority());
//                return newDeviceAlarms;
//            case 4:
//                if (equipmentType == 1) {
//                    subService.updateSubLessThanBatterySub(deviceVo.getId());
//                }
//            case 12:
//                if (existingAlarm != null) {
//                    newDeviceAlarms.setState(1);
//                    newDeviceAlarms.setEndTime(LocalDateTime.now());
//                    deviceAlarm(newDeviceAlarms, existingAlarm.getPriority());
//                    return newDeviceAlarms;
//                }
//            default:
//                log.warn("Unknown level: " + deviceVo.getLevel() + " for device ID: " + deviceVo.getId());
//                break;
//        }
//        return null;
//    }
//
//    private void deviceAlarm(DeviceAlarms deviceAlarms, Integer lastTimePriority, DeviceAlarms lastTimeDeviceAlarms) {
//        if (!NullUtils.isEmpty(lastTimePriority) && !NullUtils.isEmpty(deviceAlarms.getPriority()) && !deviceAlarms.getPriority().equals(lastTimePriority) && deviceAlarms.getState() == 0) {
//            deviceAlarms.setStartTime(LocalDateTime.now());
//        }
//        if (NullUtils.isEmpty(lastTimeDeviceAlarms) || !NullUtils.isEmpty(lastTimeDeviceAlarms.getEndTime())) {
//            deviceAlarmsService.save(deviceAlarms);
//        } else {
//            deviceAlarmsService.updateById(deviceAlarms);
//        }
//    }
//
//    @Scheduled(cron = "0 0/15 * * * ?")
//    @GetMapping(value = "/queryInfraredOfflineUpdatePlaceStateTask")
//    public void queryInfraredOfflineUpdatePlaceStateTask() {
//        //        Instant start1 = Instant.now();
//        List<Infrared> infraredList = tagMapper.queryInfraredOfflineUpdatePlaceStateTask();
//        //        Instant end1 = Instant.now();
//        //        Duration elapsedTime1 = Duration.between(start1, end1);
//        //        log.error("infraredList SQL：" + elapsedTime1.toMillis() + "毫秒");
//
//        //        Instant infraredListStart = Instant.now();
//        infraredList.forEach(infrared -> {
//            if (!infrared.getStatus().equals(infrared.getState())) {
//                tagMapper.updateInfraredStateBecomesLowPower(infrared.getId(), null, 1);
//                tagMapper.updatePlace(new String[]{String.valueOf(infrared.getPlace())}, 1);
//            }
//        });
//        //        Instant infraredListEnd = Instant.now();
//        //        Duration infraredListTime = Duration.between(infraredListStart,infraredListEnd);
//        //        log.error("infraredList end ：" + infraredListTime.toMillis() + "毫秒");
//        // return "success";
//    }
//
//    @Scheduled(cron = "*/3 * * * * *")
//    public void deleteInfraredMapIsNull() {
//        tagMapper.deleteInfraredMapIsNull();
//    }
//
//    @Scheduled(cron = "*/3 * * * * *")
//    public void deleteDeviceAlarmsEnd() {
//        List<DeviceAlarms> device = deviceAlarmsService.list(new QueryWrapper<DeviceAlarms>()
//                .select("id")
//                .eq("state", 0)
//                .isNotNull("end_time"));
//        deviceAlarmsService.removeBatchByIds(device);
//    }
//}
