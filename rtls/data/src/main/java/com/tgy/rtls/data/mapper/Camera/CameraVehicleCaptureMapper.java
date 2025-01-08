package com.tgy.rtls.data.mapper.Camera;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.Camera.CarInfoResponse;
import com.tgy.rtls.data.entity.eventserver.VehicleData;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.Camera
*@Author: wuwei
*@CreateTime: 2024-09-20 17:44
*@Description: TODO
*@Version: 1.0
*/
public interface CameraVehicleCaptureMapper extends BaseMapper<CameraVehicleCapture> {
    List<VehicleData> getPlaceByLicense(@Param("license") String license, @Param("map") Integer map, @Param("placeName") List<CarInfoResponse.CarInfo> placeName);

    CameraVehicleCapture getPlaceById(@Param("license") String license, @Param("id") Integer id, @Param("serialNumbers") List<String> serialNumbers,
                                      @Param("baseTime") LocalDateTime baseTime, @Param("intervalValue") Integer intervalValue);

    List<CameraVehicleCapture> getPlaceById2(@Param("license") String license, @Param("ids") List<Integer> id, @Param("serialNumbers") List<String> serialNumbers, @Param("map") Integer map);

    List<CameraVehicleCapture> getAllOrFilteredCameraVehicleCapture(@Param("license") String license, @Param("serialNumber") String serialNumber, @Param("name") String name, @Param("placeName") String placeName, @Param("map") String map, @Param("desc") String desc, @Param("start") String start, @Param("end") String end, @Param("floorName") String floorName, @Param("mapids") String[] mapids);
}