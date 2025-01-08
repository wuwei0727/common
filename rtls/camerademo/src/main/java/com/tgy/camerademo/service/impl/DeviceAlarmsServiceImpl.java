package com.tgy.camerademo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.camerademo.entity.DeviceAlarms;
import com.tgy.camerademo.mapper.DeviceAlarmsMapper;
import com.tgy.camerademo.service.DeviceAlarmsService;
import org.springframework.stereotype.Service;

@Service
public class DeviceAlarmsServiceImpl extends ServiceImpl<DeviceAlarmsMapper, DeviceAlarms> implements DeviceAlarmsService {

}
