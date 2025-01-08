package com.tgy.camerademo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.camerademo.entity.CameraVehicleCapture;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CameraVehicleCaptureMapper extends BaseMapper<CameraVehicleCapture> {
    CameraVehicleCapture getPlaceById(@Param("license") String license, @Param("id") Integer id, @Param("serialNumbers") List<String> serialNumbers);
}
