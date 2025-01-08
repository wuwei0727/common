package com.tgy.camerademo.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgy.camerademo.common.CommonResult;
import com.tgy.camerademo.common.FileUtils;
import com.tgy.camerademo.common.NullUtils;
import com.tgy.camerademo.entity.*;
import com.tgy.camerademo.service.CameraConfigService;
import com.tgy.camerademo.service.CameraVehicleCaptureService;
import com.tgy.camerademo.service.DeviceAlarmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.camerademo.controller
 * @Author: wuwei
 * @CreateTime: 2024-09-29 11:46
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class CameraEventController {
    private final CameraConfigService cameraConfigService;
    private final DeviceAlarmsService deviceAlarmsService;
    private final CameraVehicleCaptureService cameraVehicleCaptureService;

    @Value("${file.url}")
    public String url;
    @Value("${file.uploadFolder}")
    private String uploadFolder;

    @PostMapping("/ttt")
    public boolean ttt(String place, Integer map, Integer floor, Double x, Double y, Integer radius) {
        List<CameraConfig> list = cameraConfigService.list(new QueryWrapper<CameraConfig>().eq("map", map).eq("floor", floor));
        List<String> nearbyDevices = new ArrayList<>();

        if (list != null && list.size() > 0) {
            for (CameraConfig config : list) {
                double dis = Math.sqrt(Math.pow(x - Double.parseDouble(config.getX()), 2) + Math.pow(y - Double.parseDouble(config.getY()), 2));
                if (dis < radius) {
                    nearbyDevices.add(config.getSerialNumber());
                }
            }
            if (!nearbyDevices.isEmpty()) {
                // 这里你可以对 nearbyDevices 进行进一步的处理，比如返回给前端或者执行其他逻辑
                System.out.println("Nearby devices: " + nearbyDevices);
                CameraVehicleCapture capture = cameraVehicleCaptureService.getPlaceById(null, null, nearbyDevices);
                if (!NullUtils.isEmpty(capture)) {
                    System.out.println("Nearby devices:1 " + capture);
                    capture.setPlace(place);
                    //LocalDateTime转string yyyy-mm-dd HH:mm:ss
                    capture.setPlaceRecordTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    cameraVehicleCaptureService.updateById(capture);
                }

                return true;
            }
        }
        return false;
    }


    @PostMapping("/capture")
    public ResponseEntity<HeartbeatResponse> receiveCaptureEvent(@RequestBody Event request) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString = objectMapper.writeValueAsString(request);
        log.info("传入事件:" + jsonString);
        log.info("Car License Number img: {}", Optional.of(request).map(Event::getInfo).map(Event.Info::getCaptureImage).map(Event.CaptureImage::getPicture).orElse(null));
        log.info("Operator: {}, Device Serial Number: {}, Platform ID: {}, Event ID: {}, Time: {}, Car License Number: {}",
                request.getOperator(),
                request.getDeviceInfo().getSerialNumber(),
                request.getDeviceInfo().getPlatformId(),
                request.getInfo().getEventId(),
                request.getInfo().getTime(),
                Optional.of(request).map(Event::getInfo).map(Event.Info::getCarLicenseAttriInfo).map(Event.CarLicenseAttriInfo::getNumber).orElse(null));

        ZonedDateTime now = ZonedDateTime.now();
        String serialNumber = request.getDeviceInfo().getSerialNumber();

        String timeStr = request.getInfo().getTime();
        String number = (request.getInfo().getCarLicenseAttriInfo() != null)
                ? request.getInfo().getCarLicenseAttriInfo().getNumber()
                : null;
        // 构建心跳响应
        HeartbeatResponse response = new HeartbeatResponse();

        response.setOperator(request.getOperator() + "-Ack");
        switch (request.getOperator().toLowerCase()) {
            case "heartbeat":
                DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                String isoString = now.format(formatter1);

                handleHeartbeatEvent(request, response, serialNumber, isoString);
                break;
            case "carlicensesnapshot":
                HeartbeatResponse.Info info = new HeartbeatResponse.Info();
                info.setEventIds(request.getInfo().getEventId());
                response.setInfo(info);
//                if (cameraVehicleCaptureService.isDuplicateAndMark(serialNumber, number)) {
//                    HeartbeatResponse.Result result = new HeartbeatResponse.Result();
//                    result.setErrorNo(0);
//                    result.setDescription("ok");
//                    response.setResult(result);
//                    return new ResponseEntity<>(response, HttpStatus.OK);
//                }
                if (NullUtils.isEmpty(request.getInfo().getCarLicenseAttriInfo()) || NullUtils.isEmpty(number)) {
                    // 设置错误信息
                    HeartbeatResponse.Result numberError = new HeartbeatResponse.Result();
                    numberError.setErrorNo(0);
                    numberError.setDescription("ok");
                    response.setResult(numberError);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
                String base64Str = request.getInfo().getCaptureImage().getPicture();
                String[] parts = base64Str.split(",");
                if (parts.length == 2) {
                    base64Str = parts[1]; // 提取编码的部分
                }

                handleCarLicenseSnapshotEvent(request, response, serialNumber, number, timeStr, base64Str, uploadFolder);
                break;

            default:
                // If the operator is unknown, set an error or a default response
                HeartbeatResponse.Result errorResult = new HeartbeatResponse.Result();
                errorResult.setErrorNo(1);
                errorResult.setDescription("Unknown operator");
                response.setResult(errorResult);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        HeartbeatResponse.Result result = new HeartbeatResponse.Result();
        result.setErrorNo(0);
        result.setDescription("ok");

        response.setResult(result);

        // 返回响应
        return new ResponseEntity<>(response, HttpStatus.OK);

    }


    private void handleHeartbeatEvent(Event request, HeartbeatResponse response, String serialNumber, String isoString) {
        HeartbeatResponse.Info responseInfo = new HeartbeatResponse.Info();
        responseInfo.setEventId(request.getInfo().getEventId());
        responseInfo.setTime(isoString);
        responseInfo.setHeartbeatInterval(20);
        responseInfo.setEventSendMode("realTime");

        HeartbeatResponse.Info.Strategy strategy = new HeartbeatResponse.Info.Strategy()
                .setPassengerStaticsInterval(2)
                .setHeartBeatInterval(60)
                .setEnableElectronicDefence(true)
                .setCrossBorderDetectEnable(true)
                .setOffDutyDetectEnable(true)
                .setPassengerFlowStaticsEnable(true)
                .setCryScreamDetectEnable(true)
                .setPetDetectEnable(true)
                .setFallDetectEnable(true)
                .setSnapshotEnable(true)
                .setPersonInfoEnable(true)
                .setPersonDetectEnable(true)
                .setCarLicenseSnapshotEnable(true)
                .setCarDetectEnable(true);

        responseInfo.setStrategy(strategy);
        response.setInfo(responseInfo);
        String redisKey = "heartbeat," + request.getDeviceInfo().getSerialNumber();
        cameraVehicleCaptureService.updateHeartbeatStatus(redisKey, LocalDateTime.now());
        CameraConfig cameraConfig = cameraConfigService.getOne(new QueryWrapper<CameraConfig>().eq("serial_number", serialNumber));
        if (!NullUtils.isEmpty(cameraConfig) && cameraConfig.getNetworkState() == 0) {
            cameraConfig.setNetworkState(1);
            cameraConfig.setUpdateTime(LocalDateTime.now());
            cameraConfigService.updateById(cameraConfig);
        }

        DeviceAlarms device = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                .ne("state", 1)
                .eq("serial_number", request.getDeviceInfo().getSerialNumber())
                .eq("equipment_type", 5)
                .isNull("end_time"));
        if (!NullUtils.isEmpty(device)) {
            device.setState(1);
            device.setEndTime(LocalDateTime.now());
            deviceAlarmsService.updateById(device);
        }

    }

    private void handleCarLicenseSnapshotEvent(Event request, HeartbeatResponse response, String serialNumber, String number, String timeString, String base64Str, String uploadFolder) throws IOException {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(timeString);

        // 查询 CameraConfig
        CameraConfig cameraConfig = cameraConfigService.getOne(new QueryWrapper<CameraConfig>().eq("serial_number", serialNumber));
        if (NullUtils.isEmpty(cameraConfig)) {
            // 设置错误信息
            HeartbeatResponse.Result numberError = new HeartbeatResponse.Result();
            numberError.setErrorNo(0);
            numberError.setDescription("ok");
            response.setResult(numberError);
            return;
        }

        CommonResult<Object> result = FileUtils.uploadBase64ImageToFastDFS(base64Str, uploadFolder);

        CameraVehicleCapture capture = new CameraVehicleCapture();
        capture.setEventId(request.getInfo().getEventId());
        capture.setSerialNumber(serialNumber);
        capture.setNumber(number);
        capture.setCameraRecordTime(offsetDateTime.toLocalDateTime());
        capture.setImgUrl(String.valueOf(result.getData()));
        if (result.getCode() == 200) {
            capture.setImgPathLocal(url + result.getData());
        }
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<CameraVehicleCapture> queryWrapper = Wrappers.<CameraVehicleCapture>lambdaQuery()
                .eq(CameraVehicleCapture::getSerialNumber, serialNumber)
                .eq(CameraVehicleCapture::getNumber, number)
                .orderByDesc(CameraVehicleCapture::getCreateTime)
                .last("LIMIT 1");
        CameraVehicleCapture latestRecord = cameraVehicleCaptureService.getOne(queryWrapper);
        if (NullUtils.isEmpty(latestRecord)||latestRecord.getCreateTime().plus(5, ChronoUnit.MINUTES).isBefore(now)) {
            cameraVehicleCaptureService.save(capture);
        }
    }

}
