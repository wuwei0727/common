package com.tgy.rtls.data.mapper.Camera;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CameraConfigMapper extends BaseMapper<CameraConfig> {
    List<CameraConfigResponse> getAllOrFilteredCameras(@Param("serialNumber") String serialNumber, @Param("name") String name, @Param("map") String map, @Param("networkState") String networkState, @Param("floorName") String floorName, @Param("desc") String desc, @Param("mapids") String[] maps);

    CameraConfigResponse getCameraById(@Param("id") Integer id);

    List<CameraConfigResponse> getAvailableCameras(@Param("map") Integer map,@Param("floor") String floor,@Param("placeName") String placeName);

    @Select("SELECT c.id,c.serial_number,c.`name`,c.`map`,c.`floor`,c.x,c.y " +
            "FROM camera_config c " +
            "LEFT JOIN camera_area_info ca ON ca.id = c.area_id " +
            "LEFT JOIN camera_parking_space cps ON cps.area_id = ca.id " +
            "WHERE cps.place_name = #{placeName} LIMIT 1")
    CameraConfig findByPlaceName(@Param("placeName") String placeName);
    List<CameraVehicleCapture> findRecordsBySerialNumber(@Param("serialNumber") String serialNumber);
}
