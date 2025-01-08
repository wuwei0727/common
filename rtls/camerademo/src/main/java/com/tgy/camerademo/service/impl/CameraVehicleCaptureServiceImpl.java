package com.tgy.camerademo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.camerademo.entity.CameraVehicleCapture;
import com.tgy.camerademo.mapper.CameraVehicleCaptureMapper;
import com.tgy.camerademo.service.CameraVehicleCaptureService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.Camera
*@Author: wuwei
*@CreateTime: 2024-09-20 17:44
*@Description: TODO
*@Version: 1.0
*/
@Service
public class CameraVehicleCaptureServiceImpl extends ServiceImpl<CameraVehicleCaptureMapper, CameraVehicleCapture> implements CameraVehicleCaptureService {
    @Autowired
    private CameraVehicleCaptureMapper mapper;
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public boolean isDuplicateAndMark(String serialNumber, String number) {
        String redisKey = "capture:" + serialNumber + ":" + number;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);
        if (bucket.isExists()) {
            return true; // 已经处理过，视为重复数据
        }
        bucket.set("processed",2, TimeUnit.MINUTES);
        return false;
    }

    @Override
    public void updateHeartbeatStatus(String redisKey, LocalDateTime now) {
        // 获取 Redis 中对应心跳的键
        RBucket<String> heartbeatBucket = redissonClient.getBucket(redisKey);
        // 更新心跳时间，并设置过期时间为 15 分钟
        heartbeatBucket.set(now.toString(), 5, TimeUnit.MINUTES);
//        heartbeatBucket.set(now.toString(), 15, TimeUnit.MINUTES);
    }

    @Override
    public CameraVehicleCapture getPlaceById(String license, Integer id, List<String> serialNumbers) {
        return mapper.getPlaceById(license,id,serialNumbers);
    }
}
