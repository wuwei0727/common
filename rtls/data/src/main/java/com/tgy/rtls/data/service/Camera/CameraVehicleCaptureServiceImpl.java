package com.tgy.rtls.data.service.Camera;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.Camera.CarInfoResponse;
import com.tgy.rtls.data.entity.eventserver.VehicleData;
import com.tgy.rtls.data.mapper.Camera.CameraVehicleCaptureMapper;
import com.tgy.rtls.data.service.Camera.impl.CameraVehicleCaptureService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class CameraVehicleCaptureServiceImpl extends ServiceImpl<CameraVehicleCaptureMapper, CameraVehicleCapture> implements CameraVehicleCaptureService{
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
    public List<VehicleData> getPlaceByLicense(String license,Integer map,List<CarInfoResponse.CarInfo> placeName) {
        return mapper.getPlaceByLicense(license,map,placeName);
    }

    @Override
    public CameraVehicleCapture getPlaceById(String license, Integer id, List<String> serialNumbers, LocalDateTime baseTime, Integer intervalValue) {
        return mapper.getPlaceById(license,id,serialNumbers,baseTime,intervalValue);
    }

    @Override
    public List<CameraVehicleCapture> getPlaceById2(String license, List<Integer> id, List<String> serialNumbers,Integer map) {
        return mapper.getPlaceById2(license,id,serialNumbers,map);
    }

    @Override
    public List<CameraVehicleCapture> getAllOrFilteredCameraVehicleCapture(String license, String serialNumber,String name,String placeName, String map, String desc, String start, String end,String floorName,  String[] mapids) {
        return mapper.getAllOrFilteredCameraVehicleCapture(license,serialNumber,name,placeName,map,desc, start, end,floorName,  mapids);
    }
    //这是一个main方法，程序的入口
    public static void main(String[] args){
        LocalDateTime dateTime = Instant.ofEpochMilli(1728446929000L).atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println("dateTime = " + dateTime);
    }
}
