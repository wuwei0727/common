package com.tgy.camerademo.common;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.camerademo.entity.CameraConfig;
import com.tgy.camerademo.entity.DeviceAlarms;
import com.tgy.camerademo.service.CameraConfigService;
import com.tgy.camerademo.service.DeviceAlarmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private CameraConfigService cameraConfigService;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;

    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        String[] split = expiredKey.split(",");
        // 检查split数组长度
        if (split.length < 2) {
            return;
        }
        String deviceType = split[0];
        String deviceId = split[1];
        if ("heartbeat".equals(deviceType)) {
            handleCameraOffline(deviceId);
        } else {
            System.out.println("Unknown device type: " + deviceType);
        }
    }

    private void handleCameraOffline(String serialNumber) {
        CameraConfig cameraConfig = cameraConfigService.getOne(new QueryWrapper<CameraConfig>().eq("serial_number", serialNumber));
        if(!NullUtils.isEmpty(cameraConfig)) {
            cameraConfig.setUpdateTime(LocalDateTime.now());
            cameraConfig.setNetworkState(0);
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
