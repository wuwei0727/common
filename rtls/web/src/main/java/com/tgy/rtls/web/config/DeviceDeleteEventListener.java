package com.tgy.rtls.web.config;

import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.service.equip.SubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class DeviceDeleteEventListener {
    
    @Resource
    private SubService deviceAlarmsService;
    
    @EventListener
    public void handleDeviceDeleteEvent(DeviceDeleteEvent event) {
        try {
            deviceAlarmsService.endAllAlarms(event.getDeviceIds(), event.getEquipmentType());
            log.info("设备{}报警已结束", event.getDeviceIds());
        } catch (Exception e) {
            log.error("结束设备{}报警失败", event.getDeviceIds(), e);
        }
    }
}