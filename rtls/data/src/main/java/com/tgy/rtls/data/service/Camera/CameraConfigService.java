package com.tgy.rtls.data.service.Camera;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;

import java.util.List;

public interface CameraConfigService extends IService<CameraConfig> {
    List<CameraConfigResponse> getAllOrFilteredCameras(String serialNumber, String name, String map, String networkState, String floorName, String desc, String[] maps);

    CameraConfigResponse getCameraById(Integer id);

    boolean addCamera(CameraConfig config);

    boolean updateCamera(CameraConfig config);

    boolean deleteCameraByIds(List<Integer> ids);

    List<CameraConfigResponse> getAvailableCameras(Integer map, String floor, String placeName);

    List<CameraVehicleCapture> findRecordsBySerialNumber(String serialNumber);

    CameraConfig findByPlaceName(String placeName);
}
