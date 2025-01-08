package com.tgy.rtls.web.camera;

import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
import com.tgy.rtls.data.entity.Camera.CameraPlace;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

// 处理类
@Slf4j
public class CameraProcessor {
    
    public void processWithMockData(Integer placeId, Integer map, String placeName, 
                                  String floor, String x, String y,
                                  CameraPlace existingBinding,
                                  List<?> mockData) {
        if (existingBinding != null) {
            log.info("处理已绑定摄像头: placeId={}, cameraId={}", placeId, existingBinding.getCameraVehicleCaptureId());
            processExistingCamera(existingBinding.getCameraVehicleCaptureId(), placeId, existingBinding.getCameraVehicleCaptureId(), (List<CameraVehicleCapture>)mockData);
        } else {
            log.info("处理未绑定摄像头: placeId={}, placeName={}", placeId, placeName);
            findNearestCamera(placeId, map, placeName, floor, x, y, (List<CameraConfigResponse>)mockData);
        }
    }
    
    private void processExistingCamera(Long serialNumber, Integer placeId, Long cameraId, List<CameraVehicleCapture> records) {
        log.info("处理已存在的摄像头记录: serialNumber={}, records={}", serialNumber, records.size());
        processRecords(records, placeId, cameraId);
    }
    
    private void findNearestCamera(Integer placeId, Integer map, String placeName, 
                                 String floor, String x, String y, 
                                 List<CameraConfigResponse> cameras) {
        log.info("查找最近的摄像头: cameras={}", cameras.size());
        
        CameraConfigResponse nearestCamera = cameras.stream()
                .peek(camera -> {
                    double distance = Math.sqrt(
                        Math.pow(Double.parseDouble(camera.getX()) - Double.parseDouble(x), 2) + 
                        Math.pow(Double.parseDouble(camera.getY()) - Double.parseDouble(y), 2)
                    );
                    camera.setDistance(distance);
                })
                .filter(camera -> camera.getDistance() <= Double.parseDouble(camera.getRadius()))
                .min(Comparator.comparingDouble(CameraConfigResponse::getDistance))
                .orElse(null);
                
        if (nearestCamera != null) {
            log.info("找到最近的摄像头: cameraId={}, distance={}", nearestCamera.getCameraId(), nearestCamera.getDistance());
        }
    }
    
    public void processRecords(List<CameraVehicleCapture> records, Integer placeId, Long cameraId) {
        if (records == null || records.isEmpty()) {
            log.info("没有需要处理的记录");
            return;
        }

        int n = records.size();
        log.info("处理记录: 数量={}", n);
        
        if (n < 4) {
            if (n == 1) {
                CameraVehicleCapture record = records.get(0);
                log.info("处理单条记录: recordId={}", record.getId());
                record.setPlace(placeId.toString());
                record.setUniqueFlag(1);
                record.setPlaceRecordTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                log.info("处理多条记录(n<4): n={}", n);
                // 模拟更新记录
                for (CameraVehicleCapture record : records) {
                    record.setUniqueFlag(record.getUniqueFlag() + 1);
                }
            }
        } else {
            log.info("处理大量记录(n>=4): n={}", n);
            // 模拟更新记录
            for (CameraVehicleCapture record : records) {
                record.setPlace(placeId.toString());
                record.setUniqueFlag(4);
            }
        }
    }
}