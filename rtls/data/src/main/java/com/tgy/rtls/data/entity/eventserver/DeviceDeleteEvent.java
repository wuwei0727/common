package com.tgy.rtls.data.entity.eventserver;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Arrays;
import java.util.List;

@Getter
public class DeviceDeleteEvent extends ApplicationEvent {
    private final List<String> deviceIds;
    private final Integer equipmentType;

    @Builder
    public DeviceDeleteEvent(Object source, String deviceIds, Integer equipmentType) {
        super(source);
        this.deviceIds = Arrays.asList(deviceIds.split(","));
        this.equipmentType = equipmentType;
    }
}