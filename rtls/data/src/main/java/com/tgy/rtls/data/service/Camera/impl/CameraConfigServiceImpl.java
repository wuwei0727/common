package com.tgy.rtls.data.service.Camera.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.Camera.*;
import com.tgy.rtls.data.mapper.Camera.CameraConfigMapper;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.park.impl.CameraAreaInfoService;
import com.tgy.rtls.data.service.park.impl.CameraCoordinatesService;
import com.tgy.rtls.data.service.park.impl.CameraParkingSpaceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CameraConfigServiceImpl extends ServiceImpl<CameraConfigMapper, CameraConfig> implements CameraConfigService {
    @Autowired
    private CameraConfigMapper mapper;

    @Autowired
    private CameraAreaInfoService cameraAreaInfoService;

    @Autowired
    private CameraParkingSpaceService cameraParkingSpaceService;

    @Autowired
    private CameraCoordinatesService cameraCoordinatesService;


    @Override
    public List<CameraConfigResponse> getAllOrFilteredCameras(String serialNumber, String name, String map, String networkState, String floorName, String desc, String[] maps) {
        return mapper.getAllOrFilteredCameras(serialNumber, name, map, networkState, floorName, desc, maps);
    }

    @Override
    public CameraConfigResponse getCameraById(Integer id) {
        return mapper.getCameraById(id);
    }

    @Override
    public List<CameraConfigResponse> getAvailableCameras(Integer map, String floor, String placeName) {
        return mapper.getAvailableCameras(map, floor, placeName);
    }

    @Override
    public List<CameraVehicleCapture> findRecordsBySerialNumber(String serialNumber) {
        return mapper.findRecordsBySerialNumber(serialNumber);
    }

    @Override
    public CameraConfig findByPlaceName(String placeName) {
        return mapper.findByPlaceName(placeName);
    }

    @Override
    @Transactional
    public boolean addCamera(CameraConfig config) {
        // Step 2: Save Area Info
        CameraAreaInfo areaInfo = new CameraAreaInfo();
        areaInfo.setAreaName(config.getAreaName());
        areaInfo.setMap(config.getMap());
        areaInfo.setFloor(config.getFloor());
        cameraAreaInfoService.save(areaInfo);

        Long areaId = areaInfo.getId();
        config.setAreaId(areaId);

        // Step 3: Save Camera Config
        boolean savedCamera = mapper.insert(config)>0;
        if (!savedCamera) {
            throw new RuntimeException(LocalUtil.get(KafukaTopics.ADD_FAIL));
        }

        // Step 4: Save Parking Spaces
        if (config.getPlaceList() != null && !config.getPlaceList().isEmpty()) {
            String[] places = config.getPlaceList().split(",");
            List<CameraParkingSpace> parkingSpaces = new ArrayList<>();
            for (String place : places) {
                CameraParkingSpace parkingSpace = new CameraParkingSpace();
                parkingSpace.setAreaId(areaId);
                parkingSpace.setPlaceName(place.trim());
                parkingSpaces.add(parkingSpace);
            }
            cameraParkingSpaceService.saveBatch(parkingSpaces);
        }

        // Step 5: Save Camera Coordinates
        if (config.getVertexInfo() != null) {
            List<CameraCoordinates> coordinatesList = new ArrayList<>();
            for (int i = 0; i < config.getVertexInfo().size(); i++) {
                CameraConfig.VertexInfo vertexInfo = config.getVertexInfo().get(i);
                String areaQuFen = String.valueOf(i + 1); // Assign areaQuFen sequentially for vertices
                for (CameraConfig.VertexInfo.Point point : vertexInfo.getPoints()) {
                    CameraCoordinates coordinates = new CameraCoordinates();
                    coordinates.setAreaId(areaId);
                    coordinates.setFloor(vertexInfo.getFloor());
                    coordinates.setX(point.getX());
                    coordinates.setY(point.getY());
                    coordinates.setAreaQuFen(areaQuFen); // Set areaQuFen for each vertex
                    coordinatesList.add(coordinates);
                }
            }
            cameraCoordinatesService.saveBatch(coordinatesList);
        }

        return true; // If all steps succeeded
    }



    @Override
    @Transactional
    public boolean updateCamera(CameraConfig config) {
        // Step 2: Update basic camera configuration
        boolean updated = mapper.updateById(config)>0;
        if (!updated) {
            throw new RuntimeException(LocalUtil.get(KafukaTopics.UPDATE_FAIL));
        }

        // Step 3: Update or create area information
        CameraAreaInfo areaInfo = new CameraAreaInfo();
        areaInfo.setId(config.getAreaId()); // Update based on areaId
        areaInfo.setAreaName(config.getAreaName());
        areaInfo.setMap(config.getMap());
        areaInfo.setFloor(config.getFloor());

             cameraAreaInfoService.updateById(areaInfo);

        // Step 4: Update parking space information
        if (!StringUtils.isEmpty(config.getPlaceList())) {
            // Delete existing parking space data
            cameraParkingSpaceService.remove(new QueryWrapper<CameraParkingSpace>().eq("area_id", config.getAreaId()));

            // Insert new parking space data
            String[] places = config.getPlaceList().split(",");
            List<CameraParkingSpace> parkingSpaces = new ArrayList<>();
            for (String place : places) {
                CameraParkingSpace parkingSpace = new CameraParkingSpace();
                parkingSpace.setAreaId(config.getAreaId());
                parkingSpace.setPlaceName(place.trim());
                parkingSpaces.add(parkingSpace);
            }
            cameraParkingSpaceService.saveBatch(parkingSpaces);
        }

        // Step 5: Update vertex information
        if (config.getVertexInfo() != null && !config.getVertexInfo().isEmpty()) {
            // Delete existing vertex data
            cameraCoordinatesService.remove(new QueryWrapper<CameraCoordinates>().eq("area_id", config.getAreaId()));

            // Insert new vertex data
            List<CameraCoordinates> coordinatesList = new ArrayList<>();
            for (int i = 0; i < config.getVertexInfo().size(); i++) {
                CameraConfig.VertexInfo vertexInfo = config.getVertexInfo().get(i);
                String areaQuFen = String.valueOf(i + 1); // Assign sequential areaQuFen

                for (CameraConfig.VertexInfo.Point point : vertexInfo.getPoints()) {
                    CameraCoordinates coordinates = new CameraCoordinates();
                    coordinates.setAreaId(config.getAreaId());
                    coordinates.setFloor(vertexInfo.getFloor());
                    coordinates.setX(point.getX());
                    coordinates.setY(point.getY());
                    coordinates.setAreaQuFen(areaQuFen); // Set areaQuFen for each vertex
                    coordinatesList.add(coordinates);
                }
            }
            cameraCoordinatesService.saveBatch(coordinatesList);
        }

        return true; // If all steps succeeded
    }

    @Override
    @Transactional // Ensure the delete operation and related actions are atomic
    public boolean deleteCameraByIds(List<Integer> ids) {
        // Step 1: Convert IDs to a List
        List<Long> areaIds = getAreaIdsByCameraIds(ids); // Fetch related area IDs
        if (!areaIds.isEmpty()) {
            // 删除车位信息
            cameraParkingSpaceService.remove(new QueryWrapper<CameraParkingSpace>().in("area_id", areaIds));
            // 删除坐标信息
            cameraCoordinatesService.remove(new QueryWrapper<CameraCoordinates>().in("area_id", areaIds));
            // 删除区域信息
            cameraAreaInfoService.removeByIds(areaIds);
        }
        // Step 2: Remove cameras in batch
        return mapper.deleteByIds(ids) >0;
    }


    private List<Long> getAreaIdsByCameraIds(List<Integer> cameraIds) {
        // 使用 MyBatis-Plus 的 QueryWrapper 查询指定 cameraId 的所有 areaId
        return this.list(new QueryWrapper<CameraConfig>().in("area_id", cameraIds)).stream().map(CameraConfig::getAreaId).collect(Collectors.toList());
    }


    public List<CameraParkingSpace> getParkingSpacesByAreaIds(List<Long> areaIds) {
        return cameraParkingSpaceService.list(new QueryWrapper<CameraParkingSpace>().in("area_id", areaIds));
    }


    public List<CameraCoordinates> getCoordinatesByAreaIds(List<Long> areaIds) {
        return cameraCoordinatesService.list(new QueryWrapper<CameraCoordinates>().in("area_id", areaIds));
    }



}
