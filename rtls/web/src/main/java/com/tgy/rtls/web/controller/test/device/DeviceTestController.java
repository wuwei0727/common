package com.tgy.rtls.web.controller.test.device;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.test.device
 * @Author: wuwei
 * @CreateTime: 2024-11-15 11:01
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/device")
public class DeviceTestController {
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;

    @GetMapping("/update-alarm-state")
    public ResponseEntity<?> updateAlarmState(Integer id,Integer num) {
        try {
            // 查询符合条件的告警记录
            DeviceAlarms infraredDevice = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                    .eq("equipment_type", 2)
                    .eq("alarm_type", 1)
                    .eq("state", 0)
                    .eq(!NullUtils.isEmpty(id),"device_id", id)
                    .eq(!NullUtils.isEmpty(num),"num", num)
                    .isNull("end_time"));

            Map<String, Object> result = new HashMap<>();

            if (!NullUtils.isEmpty(infraredDevice)) {
                // 更新告警状态
                LocalDateTime now = LocalDateTime.now();
                LambdaUpdateWrapper<DeviceAlarms> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper.set(DeviceAlarms::getState, 1)
                        .set(DeviceAlarms::getEndTime, now)
                        .eq(DeviceAlarms::getEquipmentType, 2)
                        .eq(DeviceAlarms::getAlarmType, 1)
                        .eq(DeviceAlarms::getState, 0)
                        .eq(!NullUtils.isEmpty(id),DeviceAlarms::getDeviceId, id)
                        .eq(!NullUtils.isEmpty(num),DeviceAlarms::getNum, num)
                        .isNull(DeviceAlarms::getEndTime);
                boolean updated = deviceAlarmsService.update(null, lambdaUpdateWrapper);

                result.put("success", true);
                result.put("message", "告警状态更新成功");
                result.put("updated", updated);
                result.put("originalAlarm", infraredDevice);

            } else {
                result.put("success", false);
                result.put("message", "未找到符合条件的告警记录");

            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "更新告警状态失败");
            error.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



}
