package com.tgy.rtls.data.service.Camera.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.Camera.CarInfoResponse;
import com.tgy.rtls.data.entity.eventserver.VehicleData;

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
public interface CameraVehicleCaptureService extends IService<CameraVehicleCapture>{

    boolean isDuplicateAndMark(String serialNumber, String number);

    List<VehicleData> getPlaceByLicense(String license,Integer map,List<CarInfoResponse.CarInfo> placeName);

    CameraVehicleCapture getPlaceById(String license, Integer id, List<String> serialNumbers, LocalDateTime baseTime, Integer intervalValue);
    List<CameraVehicleCapture> getPlaceById2(String license, List<Integer> id, List<String> serialNumbers,Integer map);

    List<CameraVehicleCapture> getAllOrFilteredCameraVehicleCapture(String license, String serialNumber,String name,String placeName,String map, String desc, String start, String end,String floorName,  String[] mapids);
}
