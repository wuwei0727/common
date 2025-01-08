package com.tgy.camerademo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.camerademo.entity.CameraConfig;
import com.tgy.camerademo.mapper.CameraConfigMapper;
import com.tgy.camerademo.service.CameraConfigService;
import org.springframework.stereotype.Service;

@Service
public class CameraConfigServiceImpl extends ServiceImpl<CameraConfigMapper, CameraConfig> implements CameraConfigService {

}
