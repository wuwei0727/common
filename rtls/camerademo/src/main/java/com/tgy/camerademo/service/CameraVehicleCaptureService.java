package com.tgy.camerademo.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.camerademo.entity.CameraVehicleCapture;

import java.time.LocalDateTime;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.Camera.impl
*@Author: wuwei
*@CreateTime: 2024-09-20 17:44
*@Description: TODO
*@Version: 1.0
*/
public interface CameraVehicleCaptureService extends IService<CameraVehicleCapture> {

    boolean isDuplicateAndMark(String serialNumber, String number);
    void updateHeartbeatStatus(String redisKey, LocalDateTime now);

    CameraVehicleCapture getPlaceById(String license, Integer id, List<String> serialNumbers);
}
