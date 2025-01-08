package com.tgy.rtls.web.controller.camera;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.eventserver.Event;
import com.tgy.rtls.data.entity.eventserver.HeartbeatResponse;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.Camera.impl.CameraVehicleCaptureService;
import com.tgy.rtls.data.service.park.impl.ParkingServiceImpl;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.web.util.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class CameraEventController {
    private final CameraConfigService cameraConfigService;
    private final CameraVehicleCaptureService cameraVehicleCaptureService;
    private final RedissonClient redissonClient;
    private final DeviceAlarmsService deviceAlarmsService;
    private final ParkingServiceImpl parkingService;
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${file.url}")
    public String url;

    @PostMapping("/capture")
    public ResponseEntity<HeartbeatResponse> receiveCaptureEvent(@RequestBody Event request) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        String jsonString = objectMapper.writeValueAsString(request);
//        log.info("传入事件:" + jsonString);
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
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
                String isoString = now.format(formatter);

                handleHeartbeatEvent(request, response, serialNumber,isoString);
                break;
            case "carlicensesnapshot":
                HeartbeatResponse.Info info = new HeartbeatResponse.Info();
                info.setEventIds(request.getInfo().getEventId());
                response.setInfo(info);

                if (cameraVehicleCaptureService.isDuplicateAndMark(serialNumber, number)) {
                    HeartbeatResponse.Result result = new HeartbeatResponse.Result();
                    result.setErrorNo(0);
                    result.setDescription("ok");
                    response.setResult(result);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }

                if (NullUtils.isEmpty(request.getInfo().getCarLicenseAttriInfo()) || NullUtils.isEmpty(number)) {
                    // 设置错误信息
                    HeartbeatResponse.Result numberError = new HeartbeatResponse.Result();
                    numberError.setErrorNo(0);
                    numberError.setDescription("ok");
                    response.setResult(numberError);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }


                String base64Str  = request.getInfo().getCaptureImage().getPicture();
                String[] parts = base64Str.split(",");
                if (parts.length == 2) {
                    base64Str = parts[1]; // 提取编码的部分
                }

                handleCarLicenseSnapshotEvent(request, response, serialNumber, number, timeStr,base64Str,uploadFolder);
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


    private void handleHeartbeatEvent(Event request, HeartbeatResponse response,String serialNumber, String isoString) {
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
        updateHeartbeatStatus(redisKey, LocalDateTime.now());

        CameraConfig cameraConfig = cameraConfigService.getOne(new QueryWrapper<CameraConfig>().eq("serial_number", serialNumber));
        if(!NullUtils.isEmpty(cameraConfig)&&cameraConfig.getNetworkState()==0){
            cameraConfig.setNetworkState(1);
            cameraConfig.setUpdateTime(LocalDateTime.now());
            cameraConfigService.updateById(cameraConfig);
        }

        DeviceAlarms device = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                .ne("state", 1)
                .eq("serial_number", request.getDeviceInfo().getSerialNumber())
                .eq("equipment_type", 5)
                .isNull("end_time"));
        if(!NullUtils.isEmpty(device)){
            device.setState(1);
            device.setEndTime(LocalDateTime.now());
            deviceAlarmsService.updateById(device);
        }

    }

    private void handleCarLicenseSnapshotEvent(Event request, HeartbeatResponse response, String serialNumber, String number, String timeString,String base64Str, String uploadFolder) throws IOException {
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
        if(result.getCode()==200){
            capture.setImgPathLocal(url+result.getData());
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
    private void updateHeartbeatStatus(String redisKey, LocalDateTime now) {
        // 获取 Redis 中对应心跳的键
        RBucket<String> heartbeatBucket = redissonClient.getBucket(redisKey);
        // 更新心跳时间，并设置过期时间为 15 分钟
        heartbeatBucket.set(now.toString(), 15, TimeUnit.MINUTES);
    }


    @PostMapping("/ttt")
    public boolean ttt(String place,Integer map,Integer floor,Double x,Double y,Integer radius,LocalDateTime baseTime, Integer intervalValue) {
        List<CameraConfig> list = cameraConfigService.list(new QueryWrapper<CameraConfig>().eq("map",map).eq("floor",floor));
        List<String> nearbyDevices = new ArrayList<>();

        if (list != null && list.size() > 0) {
            for (CameraConfig config : list) {
                double dis = Math.sqrt(Math.pow(x-Double.parseDouble(config.getX()), 2) + Math.pow( y-Double.parseDouble(config.getY()), 2));
                if(dis<radius){
                    nearbyDevices.add(config.getSerialNumber());
                }
            }
            if (!nearbyDevices.isEmpty()) {
                // 这里你可以对 nearbyDevices 进行进一步的处理，比如返回给前端或者执行其他逻辑
                System.out.println("Nearby devices: " + nearbyDevices);
                CameraVehicleCapture capture = cameraVehicleCaptureService.getPlaceById(null, null, nearbyDevices,baseTime,intervalValue);
                if(!NullUtils.isEmpty(capture)){
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


    @PostMapping("/ttt1")
    public boolean ttt1(Integer place, Integer map, String floor, String x, String y, Integer radius) {
        String base64Image = "data:image/jpg;base164,/9j/2wBDAAEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAgIBAQEBAQECAgIBAQEBAgICAgEBAgICAgICAQICAgICAgL/2wBDAQEBAQECAgICAQEBAQICAgIBAQECAgICAgEBAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgL/wAARCABgAUADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9iIbjZKn7qPehG0yRLlCMEqhbLdRknvW1FPBkZt7dlO0bPJjK8AAZAXsowM5IHA4rk0kZgH+ZkzjkldxfruAHYnqSSTyTzmpvPdAoP8IzubjAHYAZPGepIJ7967pRi3eF4X15lvzN6rdX2lfW92+qPcrRxFpL2s7XU170vVppvW10+jTbu3ds8v8A2l9H0m58NDUoIIoZW0meK6nu/NksZ57m6tYl2iGI7XtInmJUuxleWFkXMbFv55fG2ryXV1crbSbLya6fUWjt1cvYte3MkkcS284URFlk3eSzoydZAWOR/SD8QZRrXg/U7XyHa7tU82ONGjXz0DK29JnjmA8iQRySI1vMJYxJGp3lWH4DfGHwVP4Y8ba5Pcx32+/uIr4tJbQxbonDRDZFBLJGBG9lMF2eWhhUMgwjNX3vA2KwcOejWwntqrnHEUqkrtuDfKoq92k5yTk7fDf4nofC8RYWvOaqyxFSjF0p0XBNr3pK7k9dXyKSipXXNLvqfoF+xP4v/t3wCPD7TLIsEuuJZq0KfaVjXVLqaZZmSaRVe2kvUjMPlYQBDHLJvJr6a1OHUNNuR/aOjW17o9q7GDUpJozeQkrwkNk0Xzb2+VmM9uBwfm5NfkP+zp8UNT+GXi9fMEw0ZL63uMW9qkr2lwRi7Lu/DLrNitnBJG8iRFo4XRvOC1+1T+LfB/xE8LQav4T1jS9TtLu3RpDbTu8kBubZJEieGRYpFYJKpO6DgkiTDqwHzOZ0PqOKqUcXBYhuc2ppytQpVU3GclBw5ZKMlJwu4JtKSdnb2MFCeKpc+Ebw9XldSmpzS9uoq7gpNSer5vffvNXa3u/MtQ1DwZfOsOoaOWmljumniuND0+5X7PGFBkcxzb2a43RBVjS4YswycZYRw6b4ADWcFlpljBdR5+wL9iubNgxU8COSR4yQJWwTyCWI+avEPE+k+IPtksdnPdxmR2V7pXVGiQIwwhMiNyS2SqD3FYenv4y0bUFvLS+WF4NkqLLZxXHnzRtJmOSZ13hZgfnAf5gT5nWsa1J06dOeHxqqe++SheUKtWHu2nyc0ouN3y78+8rW5mYUkqznCrh6mqtUlG8lFty5lzWTct23td/Fffc+JUvjnw15lx4EfRTcy3U8DjWre9uTEJbO4MfkRWs9irGK7WJn82+tkKAjccnPzS3xf+LlpcmaPwn4aYTNFDrS3Vv9iZgsMgmTSi+takWdnhk3QzaiwTzSVeXflPu2HxnpGt2q/wBt6TLZ38gEF/Haxy3doEkAzKAHjlPy9QscjY+4zmsBfDHw9llknOlQzpG6XLxtZXMX2vBJM0ySyKzuyEKxcCRsYkyMY1qY+hVpxjjMHCnUTjH2tNLmxNOMk5OTbneSScV7NQi23Kbb5rjwkoVJfV69apC0p041XJ8rfNZLV6czbblKWzta9zlfg78Wtc1G3lsPGegW1payRxJNd2tw8s0bOxysdksIUpbR8My3TsW2+WSd2/2LxP4a+GPieEPcarpEzpb2c6TS3E1hIVDtLCHnJhPm20sxPzbJInc+YQxIrze50fw7ZRE6IGWPDusR+1vt3k4VpLlATs6cvK4/5aHJyeWeTW7gmGOG2NnGcTLKXfd3aN8N3yMYQZP3ie/luKTm6dV+y5t+ZOT1TUZL3mm92rOPNe8pNXOqVVyjGFRQclepF21u9JWm/eaV39q+rbWp3sngf4fiWOKS/wBLSZrONUshrFlJdXEOZCssapObiSNgjEurSKdq+act83F+LLuz8GWd/fabZJqb2UAkkto7iSw+WMpuaO5gt7hl8mHcxZIWJKnBBJYY8z6xaOk9naQySxyqUhu3dYSofJAKspyR3zwTk5Neh6adJ8UWS/2nZR6DqvlwrdMl0LqFmLZIhnP2fcVwdpKIQW6vjNephlhounVxkfaxc262FlWUG66am58iftFCVN/xLNympU29VKfLVjWTqRpTdKaSnh5Km5e49FGUpOzafNJ3eiaet2fMmnftD37XUl9pnhS6s7driN7iIXl3qD3aFJEeTWLy3sLJCZLmSNk/eCRSR+8mZwW9O8P/ABy8rUS2peEdWtnuoYEupLS4e8E1pJGfLdY57mzZ1U7SA0LlFQmAyMcD1Gz+F3hqTUzqX9v6kxkHkyLaXOi4uoo1m8qG5kbTrl82bTRsrechJUg4VgBrXvwr8OiWS7S9jldf3uy4eC4lQLsaMSos3JDrIxYRW4OQQpOS21ethKk+ajiI4RqMo0eWFSdStSUWqcZuclGU3G6clZJ2sro5oU6iTVWg6ylLnqtzXLTqSfNUcVZyS5veSbberb11sQaB4b8Z2UEqyCdJng3wSXIS7Es8ZAtZo2mjPmMHKKGWbJOVDEhqyovgn4YheG2u4Dbx2ktxNapdfZ7pDK8CJvL6wl3Jm1At5MrcZV1RpGOeecu7OG1ucpbNemGRTG0SF0icbsTAurcowDcbSCeuetVdPkEuYtLikBJJZowcO4DMQoByW2DJyCT61hSljac2/b1MJ7XlqOcqjj9YsukpuEVKlGUn70rqa5VK8k3cqdKS1UcQ4tpJpt05OTbbtdtTkl0ta7aaTOwufhX4b8J2t5c2LTSLdTJcSLcXcVykXzSFTEkdrAAqxmJF+d8rFGWy4Z2821z4/wCm+DBP4dvdG8f2hMNzDFf6Vrttp9peQw5YAyaf4htblftH2i4dQLW4YK5eRULEV7ToqWd9aw2moWMkUUpUI0asogIXhmBwep5+U461sXPwT0nUE32/iFbSJn86KQW+64tj5cmGjme4jYsA7oCsludpO1xkgy8TQftfrlKji6lTm5Mc5SnKE4u8WlGbU072kqjcXf7aSRpyVVySo1J0FDV0EklKnJtycnKPTV3+Lo7Ns+arD9r7R9QvnW00jxtqF3a3ka2msLcXcyXLNHDutZ9QlR2imhQhpIZLiRpots8UDeYEPungL4gaL8UjNO7XWn3kjxWnl6o0luZ5MYaKxN1NI8oilyFG1GY5AjyHA6XSvgBo4C+fq0L3EEkz2n2PT4rOaJLkO0xgnuLu/mWS9MjGaUXEzSLt3EFdxz7/AOGWm6FdSPGFXc6MYllYj9xJlDHl2OUI+Ylss3zZzmvO/wBnvaF8PdRhNyu586jecm7pRUm3yxTlfTq7vvpxvapJxqRcmpWb1i3zdE7vvLdat+fqx+Buj6hbrcqZZxPHMftMtpYpHL5tuU37ojbuxaIAOyzxiRAFcYFNT9n7QY4HkM22OMtIVbS9OVbhhnDXEqOspVPLy3+kZ4Kq6r8tcLb3epPbzRLYzSIASQ0pmRmVv+WaTFtrPycqVGT15q3p8t5a3bmXR5xPdJ/pBa4ViYC8u0NON4blpCFLEAscdzWSw2MjaUcXSjaThTacZWne/wBmbeurc7uO7cm0be3wUuZSws037/vudpUdv5dZPz1u7tWd3z/iDU9P+H95Pef2Zc619mSdEt9FbE0qpGT5CsXIG9iB8xkVc8nGTXCN+1ppFlf2iSeDvEWn/a44p0N/r0iSR28asFkQf2Y1ojQyFwJhetGSQtq7sxr6Ot/A2keIHtrttbGiJBFs+x3UEFwpfzssEllntgCyZTlpezKCQQ3VH4JaMUb/AIqGye1vUje6t/7JtZLe8eOONHZoBq6qwXyzzscg/wCsZmLMdHVwMZRjOLTb5ZVFK8qjTafK5qUYKTknLScuaLtLllcKUMYuadCrBc0nCN1ZQhJfau2217yveL3bTs7/ADBbftF6drGovpc/gbV7eOe58htSF1ZXlzY3Fw7uY7+AzW5YNZsJZJfPd2y8kcTf6se96B4d8J+KrWO8/teCBJ4U3PC9srxq6hiCt4p2sqgZyEJPDZqze/ATTzFK48TQY5+zeToQLRwk7vLknudev2w52ZKLDhiWO41Rt/C8nhwPbWqLdqqLGtyAYFdEBxuUyyhcsXz++kJz8xzmsnOnOMlQqOlJct5PppeV+aVpu9nFWbk23o7p7RhWpyhLFw9srtQpp8/ucz1bjdxa31undtyerOwh+EfhM7kutfvLeCSXdOgudJslvBuiA86UoiAI/wAzN5EjZI3OACTxfjbwV4a0DzrqzvRdTfZyZ2S6GrpcCFWKBLmN5DuZf3ZCSGPONmMEnY0+01eInz9PiU5kaBo7gzeYjMcEKFUgsPmIyevXOa6yw8K/8JJPDb6xGLeIusQntyEdM4BL+aQDk4zuOOTgg81ytVdv7RjKKk28O1BSqSbtpZtu+rlo3rZSNsV7OckqOWVKM5yc4zcZt8j196T89UnfV73ueJaN+0T8S/D6S6X4a8AXBHmpFZ6vdvqypdR21xBHOLdtIu7tdlyZUMQuIreVXJaQhfkHner/ALV37QM801hcfCi+igi82V49Q129RDbtLPtjm+zC0lmkMapmRL/UwVLObaN5I8/s74C/Z7+H9t4dBuIEvornzmYvZ6eskUyNGqxCbypkVbPyY8ILTKyBpEcSPI7eRfEX9nf4bXF4120P2XyhNFGBBplzEkUkcwaKOO+0y4GwtcXDEBBhpGaJg3zH5inn3D1XF4ijPBSlUg71MQ6fNH2ujV24ylLrpvre9739aeV5nCnTlWzJUOZPlo3k/wB0k2vhsoyk9dd29b7n5n+AvFw+IUzf8J1o83h+/kX7TL5DPPaTzT3k7bY7gy3btKqGGZt7XeZHZnY8A/Qk/gP4UXUOZmN1I4iZnbW9Z/tL9zvJRZLWa2dA7H/VosKNgFVOM1oa58M/Dnh65lk8PWTTeWf3XmPJMYldpCTF5xG0gMchQBjCrworI07TdY85Vdolt1kJ2pACzLtPyeY7ZHzck4yT0PXP09aeHcI1ama0MHCPvKi2lKqm9mpptfOS92TZ4NCpOFadOeAqYmTUrylSc1Gd9Zc3NGTe73fvPXfXJ1Pwr8N9F0+8m0rTRb3MNsk7zxw6pdXIjjZissgvpJk3gJtaXMcxTPmu2M1wXg++EF+j6dbfZ4ridZpZpObjZg8mQgdiRgrnk4OSc/VukeE7y8sJDGLed/meVZmUKqgfcGQclz6gA9M+vzD8VtX8OfDG31W81S7s9FjihnnWO5n8kTSvEXWK1jw0jtcOw2IkcrOWH2dWyM75PmeFr1Zxy3HLHVdKco0051XUclfSOutuZtp3Stq3cjM8FVhCM69CNKnzON5WglPlcm2m2767t6N33vf4a/4KBfEASrY+GodauzayWVtPqfhq2vbIWerXtpNfywXWox2t6lwYbMTWbSRXVvLBIwWRPmWMv+TvhiK0m8XeHLDLSNNqUSGBzGrmC4tbiOaWK5uFYRS/ZnuZFl3oTIuJGAd2Hvfx2+KE3xP8Vale2DzSeGYI5G01rjTJtPkWO/FqZYUt9S07T7lHQW1qsjPE5lYy+U7RgKlP9nT4caj4j+J9hcyQXUVvZy2t3bxRyyxQXFndQX7Pc6/HHexv9msb2yUqhigleV/9BZ3yh/oPC5rhcDkdenVUsBiatGrhXTUUquKly1fZTnTcVNSXPeTqq8Yxcudx1f5u8DUrY+E5cuLpqaxVCXO5RheUPaR5ryWk1eKhrJz1tZp/0I/speGfDel+DbG0n01vtdr9j0u61Bi2Lt7XT7eW5uIYLi8km87UrxpJnkmVDL5sXzvbxwqv2C66EG8yztJoXkLEpdw6W8czsyszlLa3Xl2BYne5J5fPWvnHwBZz6P4V0qLy5LUiOKa2icJFLFEDlGKJ0LRsDlvnIIEoByK79dQuZ2EzyhpE6MqhSMewPPfJwM5PrX82zw8J1ataWJxUnVk5SgqjtBrR2sl1u7Pq3q72X9C4KNOnTp0adGMJxjCMlq5Ko1eSls5Scm9ZXad99353p3+kRqeQwyMggg8dx6/jz+taMkCr8zvztIIHU57kD+f9a5nw3crdRiNWwQ2GAJO4+/8A+vqTn37X7BcMd21h0weSOeoJz3z19/xrpdem5S5a8PN3u2t1q3f1v183rhi8LOScoYe0emvvabtpXi76v4nLq3uc1NCHDoygq4IIIyCCeck+oJz0zn8a+Yvih+z/AKZ4paS9jsIr1H3tNAZms7mFyDtkiuoLi3lCYLI/l+dIyMwKsGcN9c3FpIhIwwbbubOAOPQsDnnqevXHNU3hbkkHoOue/Xt79678JmmKoy58NXqUJaxTTvzJ76tu9/O9n13Z4eY8P0cQuXEUqdVxbnTcteVu7ur3Wy1u299ep+Sutfsvz2sjtp0cmnG3V3w81/q8iXLFWMiz63ILktcQM5DfbgI9iiNNp2rT0L4PePPC1zqbaXN5duxhkfUTqQ0vUb6G6jkZruCbw7HcXaR20lvA0gaWBkmkLWxKrJj9aW06KRw7IN5Y5wAWyAenGc/jzniqY0eygQRLbRICCuBGpcoWJKMQAdoJPy9Pxr1sfxDWxN3jKOHqNrfkXPOs1rN1I/vGtvccmrRW/Ld+NheF8ZSv7Go7uXLShzv+DfmtZy5ebeTesnzNvqz8ntTtvjFp269TVNd1JZi1ybM65Jcx/djXYJtWW/BkiUsS8NzbLKyuEWRiM8Rd6j8b9LH2m6vLueW4uEtbB9WuzbQ2kEAlDX0gg8P6b5m2K7DSQSNPJK20xs0iIW/Xu/8ADWkXEbxyWkQRwysqRqE2yLhlkVQNwdcfe3cYrmB4L0CN0K2sREcmVDwB2z5YX5ixbdhBjJX1OTk58rC4ujB/7vSrxb/2jn5uapDnjK0Zqzg7KznBxl781KUk7HVLJ8fzN0JVFuo1U7ckmpJ3TfNJe9dqSs90ur/Le08V/GfTtPS/uE1X7d5sIeAWGjzQXMUCB/Ot4/E1s05+0wKGkjDSXE25mtopGbaH6f8AFT4sWrRQTaWsksVqs95c6xo9kIiVEhkiSDR30ZZZJdhceVNbiIYMy4ZVf9N9S8EeGZkk83S4J0mDK5ZCE3fOdwRSoVgzv84AY5I3YJzzD/DzwyGDpp6xyEszvGPLeUhYQpkmiKOxiWGMAmRjgY5BOe2hisBUnKdfLkov2kqUYVKiVOclaKinVk0oSble8m3veOhyV8tzdKKpznUs4xjOTTcoX95Lrdq71Svunrc/Py/+OHjvTUtFfTvDwie2urqeee0uxbTyo9m0cMkS3MFzbmOOeZXKXbu0ikAciq9j+0n47MjQ/wDCH6FfzXLT29qltpXiJ9PK2DwPPfI+n313M8hW+tY3E0ltHGfLljVt7LX3vN8NfDN0xuJIZkn+ZBOFjeU4bIV2uFl+WOb5wqhAr5ZMHmsO8+GHhVkY3CX6hzGbsW97NFJfeUc7Zb1cXA8zahcrdKzEEq6kknX/AITZUVF4StOu+RVa0pr3UpuU5xWvNOScYyctLJtcspNnF9SzONVzqVacKd5S9kvelNNKKu3e0Yy5puzu5Pd2ufHl7+0P4ktp7hJ/BmjxpavtuvtN5qTttDgs9uosIHzbWs1rk4vAS3nTqqtsWO3/AGjNbuII7mH4f6bHbywLGtwL7XNXe9v55JFiSx02y06zlWKVoXKs11fGbJVWiK75PsS1+E3g7U57eM292kUbMygSpcXBjkl3mKPUr9bq4i/erbkmO4jcrFGrlsZPcz/BTwTdhbi60nSrqdJMJeajp9neXKxPcvI1kZ5UGUcSyINySnazBt6s2csTUwUnB4PB4lT5oqeH5nKFamnLnlF2lUbnaFrO0Um/ecnyuMcTByU61KUVzKFbS6u7ap+611vd3b6W1+IIv2kNWspkgk+H7RXksMMxeeTxVZafp0UqMxe+W78LRqzrF5TKq6vbIXkVJpV3AmX/AIakhT7bcXfgO6s5IV2yy3GqmW0aa3WQzO81tpzSCKF1Kq7oGADNOAFIr7gg+A/gSVFtxpNnNb2sirBYzTtJaWcDCXGmCJTEZLcmdj5U5uj0VThRWfD+zv4Js50njttMt2ginsRbPpNtdSQR3MflyW63ktw7qk0DFNklvKVyXVgxBHbgsPl8pTeb4fEUlyKtKFO9N0Zc3Nu1VdnHlTbg/wCJKUpOSscVariLf7LONW85Jcz5vat3u94q9+Z/EruNtnc+I4v2rLRI4bm4+G072Mxgtjcab4pubr/iZt5O+CNLrwNaRFT5rtuGoziMDN7Iu7dXRRftQaOzO9t8Pbu2ET3IkTWvErafNm3a32mRoPDFzHCsiSuS7TXo3PB5YdnYD7Ytf2cPBzRzN9itpPM+0StBBpttbRQPemQzSrcRbXbzpW3nzRcM250dmjbbWhb/ALP3gAzR3A0mCySIeV5dr9nSPKzSv5kzzwXW4iSYtnKsQsSE+XEiC6tTh2c5vB0cwpWf7mNSftoVUm7yqRUY1GrOz5ZKVnGTvad9IVMxSj7SdKUnaVR3UXB7ac3NFvVu8r68190z4x0X9rDRb22knHgYXCmJ7hF0/wAQXN8soSNt8cOoHw6kRkS4EWDJDYrIjO6n93mTsIP2oNCk/s0TeE9aMWpvPHbSWF6+pHdDEzosUZ0m1Dhk2FpHn09QXzEJEVnr6oj/AGf/AAEZmj+wWd8PMSULqIF5JMUgRD9onkUmZ5ApyHRYydu5Cyh6sL+zf4AlUrPoOgiZo2jS4j0m3+07GjgAeZnDM0ge1t2MgmSUsuEkAeTfyVsFl8vaOlisVQlGSqUaMqXN7Wk4tuS1U0nLb+IlFOTlJ6jeOx14RdCNSMryqVfaQTpVbrR7tyad200mz5TT9prScQNc+A9VikdYo5lPifTovsrzAL5GyHQ713ZbhhEEJhkkbOUTpV3UP2jtEs7GS7m8D6jcfZYkcWZ121tZJmkO7Kumk3zR/ZYwd4NrdtJIwEI5Jr6ql/Z18DCWcvZCcyTRyEPLcI+wQbGheGO6jiKt5asVNr5QYM6R+YzyNS/4Z2+HPzRf8I7pk8rq2TcK8izGSKRXuRapIEWZ4pp189I4pVRtkbBQK850qU+aV61NpuSpNOTdFN80nU91RUVpflim9Xa50U69VNKn7J3f8STiuWrZ2XvKV3J3bbbd3d3d2fLP/DXHgvSfKGqeC9W0u5uBM2261ma+sLWKCGWUz3uoaX4RnePfDGBGjaYpd2wXXDGta3/bH8A3iM8XhHUZ2ji854ptVubOWNAGy7QHwncySctbERxiWba5kCNGN7fRCfs3fC6SGSwi8L+HTZyw+RJpo03Zp7AIib57JLgxSOI4xh3tzIr5fzSxBXpo/wBnv4e3EyTS+HdAnvIo7G2juZNHtZZEtdMKNbwOsrusgtWRQpnS6/d5jYGMlK1+rZD7OPtv7RqYhyc1UilGFHDXi1dNylJzTbbv7r5ZXmrp9McXnM5p0Y4flt7JL3JSq35r7ax5Z63nq9dr3Pmhf2tfDEAjkPgLVYzIrO8EfiFUWApLAD581z4TSGNWiklkBe9MjIhAi3ZFdFp/7X3hee4+yQ+BtSiuY0uTIDrbTvIsJcj7Hc/8IpaW8wuIzCpY6hbOkomBikEY8z39f2c/h1FGiTeF9Fx58MsTnTrebE1vcTyo6NM0jZt7i5keMCRVX92u0rGijYs/gL4AV2L6Rpk9yJ/Mlvbuyt7y5MkkrMyyxTOUPnBtknyqZY/kuC+5t0YhZZpLCwx0L86dGdSLa933GpKGq5m+eLV7XtO7dtKKzRNxlLD2Uf4jcLS1d0+aW9no27Xb+fy+37amg/aPsCeAJGgBnMtyPFkcRggTyWDXb3OgWsPmMHk/dw3V3uK7jIgKl8a6/bbSOymlt/hNb3SwyoLm1h8Y3V/KLLCmW8Yf8I5p4j8oQ3jlWlugqKPMZjlh9sP+zt8OblxNc6F4fuZp/Lc3F/pVrfyLPH5pE8Ejqk0ckBlYwiO/VLc82iqSxfI8V/s//D1bXdHpNoJl851mb7M9wrXMkjMm5rcMoknIcPv84MMxShi5bhr4nIW6PJhsXiL2+uwquo4yxkW3J03F3hF3S97maabvJvToVPPVTnOWLo0lJutSqRlTc403/Ppdu99E9b211b+NIv27opEuI7D4ZQQxwEQQ6nPr8N9CZZt5hji0/Q/7TnaO4jWM+YYt4aVY1SQo8h0LX9u+8juYpB4H0fTfMuvKgjTV77UZlu4mmKqGS2tS/mPFFK6XNtobIpMbM53SL2GpfBPwa9/LLPA3mMJIjM0dpcXsER8v9xb3U0D4TK/MskNwCCe5JLbX4TeDracTCK5MsSosc6XlxHKzRW/lrPOqssUkkUZkKs0DBS7+WAp2jo/s7hh1HL6li6a5JTd5U5yqVHZxTk6cVCPO5SbtJ6pWveTxqT4ikk/axxUm4tTV1FUpO71UnzSs1dOWsryZ9J6J/wAFA7u18OwX0tgttLFbwSXXhY6Nr19Jqd7PM+YtM1fS2NvGqALLK1zLp0cSuUMl00RL+XeMv24fEWop9vstM0l5XurtYbG50jUbezt7O2uIkJ1GfUNSWWTzUE8yPBLYs0bJ5qI24VxcPwq8JLHMm6//AHz5aX7SRMiYOfJeUOocsz5byGDBiJA2a0E+GPgZBHGukERrObgpFI6STytMjMbqd/OkfzXiUsPMTAGINi4A8WWScORftMNgFCvJv65VfwynunHW973vKTu1Z6a373W4gnZTleMY+xouMlz295SVR31dno91q276ngjftY/G7VWnjOgaTZztJHlpfDUsVhs8iTNxpdqyNO8ZuH3ebc+Ir9ZWjIgjVA8Nea6r+01+05Dcxpps2jW0N/crFFHP8KWuF8lxI8tvp0kF5pjtMY2AS4+23/kYZjF90p9ln4feE1+0GOw8pbiQmcQsoeQkL8ryEE/KqADbsxywG5mJm/4QXwkIhbHSYJ4DGkaQXMcN1GFUhsOJ433MWUMWbc27JB5NephJZNCqpV8vwOaR/wCXlGtTd37k0le+usua7bblHTld78NfL84qL3qtehOf21Uu7aSu3q0946e9rrc+WvDn7R37QupO0N9q2taVaXdvPDcR6dpFrZ3txaSeZ5d9ptpuujbsWFiWke0kMjrN9nSGGZgPIfiz4Y+I3xVvrcz3HiEixmm+yLqGrHUJ9WF8IVH2iJdU8u3YPLeHzZg00SB0cqu1R+hsHhLw3acwaRaoqOWKhVRASF+6E6BQqYUbVGMgZLE71tpGhomwaHpWwoIw4tYSxVWyNz7Mn5ixJYsxJPPJzlgs4w+XVoVssy/DwnZ0oylTjaTnaM5Saeul+VPmtz3V2ztr5FjMVBwxtetL3vaL3nz3u5SvdttpxV7azfd6P8jk/ZXnkt40l1W4gukdGuZ9iSXHE4NwjWBeSFDBbtIkUjXlxvdVkZwpYH77+AfwGsfCFlbM9pHbwRQ28128lsqy61dTWs2/Ubx22zb/AC7jZHGZp4o4z+7G5A7fSsOkaVepHHPp2nvDC6yJC9nbGFXjfcsiRtHt3K+GyBndknJOa6iNViOcBgT33dT6ge/J5ye/NdGN4trYu31vETxFRVKlVObleHP8aTlrZ8t3Hm5Vfa51ZdwkqE41IwildScPdinCndxk19qTf2m1p56mwjmZlYMVRFChRwu0DqB6itCHfkcgr1/DBz35OTyT7/WqVqjyBQEKjqeOvB6jPbr1P61bcbAyKcg43A8EE89vXHtXnvFQurLV6Jbt3u7u19U/vd3r0+lWDryc6jq3laVSTau5atu7va+r301WujZ8Ffsh/FXWvHPhXSdQ1nw/d+HLyW3sZbzQtQurC6vNLu7qKYy2k8lvFbE+bPBLNGzwJKYp4xNiUSIn6g6FDaXliszwSKxQMAcBjgHIPHr3+bPXNfkp8ALPw1qklvq3grxNFKjx6ct5ZWupQzw29xciCZbSa1kCfeRoysr4BVsqV3Fa/Vn4e2Or28fkXztPbrC53l2kCysF+RCxOQHLYIY8Akn18LN40JTnUoRrYSTTqfVZqcHZJ/CpRfM3or3k9rp3Z62WV7UqdKdOVR6xnilJThUjdtqSTbur2bbcm7Pvajq2j/O8hH3tznJGFwSclhjp1Jx659+PMayOY4s+YM8EZyMZ3LnOep5/xr6AvNNilhkjIBBRkOQBgMCDzjqM565z1rz6bwwYpzJlWRCPKGG35O7O/JOcALz1yTnOazwOaQs1WqOHKrU+ZvWbT1d3s7+berTOjEYO8rxU43aqWfVt3abk7+8t+ru21rZ+cNbvlkJzIeR7Bc5YkDPc/U96gMSMMlFYhQrdWckjvxnqeSCD9ea7280vY2eCpByRklT1wARk5bH4nn1rDawXeX2fOSQThRnGeB0JAGTg9ycda6o45Su3J3u7yVk/aNu+u/W/NJ21vJO5wVKNNzdoct1aybe6utW7O922lZ63behyD2glG8DBL5JxvHynoC23gHnOc1ly2P3iAz4/uqVLqWzlVIyff19etegNaKB74zzkljnuMkc5yeB9arTWOAz7RyPlY5HTk7hkfidw/nRHHa6O2tk7tWlu+aXV6edm73kyJR19zTdczcnyXercnquzvre99WeXTWhK/MpL722h8lS3X+EdcDock846k1jyWJQvx3Prwc9ADz3J+8c89TXqlxZRljtXgks2MgE4ORjDElQep5OTzzWTcacCxKKwXaB2I3fOfmAOcnjt65yTXTTxj10lFJay191N6u/W+t3u9W9W2ZzpU5WSSjf4Z3WsetndtPS2nXXq0vMxaEoSR8pZsEYw27nALE9eTn8ueuDqcLrDLtV8jkAhWOMkkc8/nz6dTn1OaxIG5kTALZwpYE84OH55HP1rkNYsXMFxtV9zK24g8ZznK5x6c8j9a9bCY3mlZ89vis9eWLbupWu03vdvW+ru2ebjcPL2c3zuW/Kmm7bq9le7vJatrZtpvU+fPE2r6hp9rL/Z1w9vLJtPmBUYoyDdlRIsgzkDcWjbjI6muDPjDx/qJnmi1/XoWW2Mfnx21obZfkhGTbT28sU3mRqzbJbS6UO7ybd+0r6X4ktPLEsxjSVooJZXSRWwfIikYk8Akhdx4bJxwc17V4U8EeFprO7uHgeWwkQXR8zMbvNPEHLlcK4EabQI3O1eQQec/wBw+EeT8FU8poYvPcio5jUcZQeJlTlKUXUXvNTd5Ju9tUuay2e38ccfY3iurmVWlk2bvA043lUpbuajzKF3P3Vqua0lzSd23ZO/w1qPxM8c+H7mW51a/wBb1ZSfLjuYrtPC15a3ZDINsvhy00vfFdSGMzZgmLHY8Tg7s3/CnxF13xU0bwa5r9pbW+peVJp8V5dyWxvY72VpriTe0k1xLHNctL5ty87lZPmUMWFez/GHwVo+nxW+tR3d0Y7vENvCnkQsipPAr3EPlQIieUt1GDvAB3jJ3HJ6b9i3wJpHiHwL4mk1uHzdSl1CJ7pYpYQbe+u73VwltMs8E0oH2DTEuHZJBDO18txpxCszSf03xPgvC3B8PYvPKvD+GpKlTpYKrBUoSqTlOXs1TSnFc0m5cqTbXPLXW7P5/wAmzTxMr8SYbKP7cmo1frOKp1JLmUoUkqkqk5R/htuatpKTjGTi2tul1P4rWei+E4tTvdW1OC9tw8skVrDAFvZFWQCWKS4uookCqUmkEtwqxoH8/eeG+J9e/aC8Q3ur3Lf8JZ4i3x6f9lso7fU5rGSwJ1DfNNbw6R9midprm8+ySSsdVnl+WIvJCkMS9l+1dokug6/a2WmanjQoftKyxwKsFv8Aa5769Vobt2VV3Z06CSONJJUCYfiWQY8R+BHh5vEvxDhs/P1RLKwjlu7qSBnWzN3HcWMdqIpoUguo5YcSEw22o2jM7LMTtllnTxfB76PPhhRyetn+Mw+KzJYmhLNMJDE29rlVJRlLktGF6bTlK7SbeurtzHveI3jLxlPO8PkWDq0MrnCusDiasbyhj3Jpcy5pe9zKTnFSktYtNO7kfQeleI/i/qtnLf3vjj4maXItvKn2X7dBosMKyLJ/x82GlaRpBZ5bWQH99DP5asGicS/vBwOu/Gn4neF5Lay/4STxTqbfailnPquqa7PLFJczWryW1lPbapphdbqCCSHM+oSJH5pkt1EnzH6u1vVNB8O3D2hsbxpJrgRrDAJ5IoXlwSHluJHGMyDO+Yn/AHiCK+f9V0HTdc8b6JY3cF3bW8WpG6t5YJ0KSuY5GFrd2+yT92zRxMzKikYy7qhlzfC3Dfh/mKeIzDgOjg8NNLMo1VRjTlOShKclTqQTrTtzSTs/f55XVm4np5/juM8E40cs4w+tV6clg8TTnU57wlU5U5wmnGm+Xlal7zvFXbu5OlpXxA+MFxa2i2PjXxrpOnGwjt7HS5ofDY+zW5a9aSWHWLHRLTVrd3W5VGjfxFIyQRwSECNw7+Z3vxh+K+j319p2peMPibeqYWW3vk8aa/bagfOWN1mAF68c0sEk77J7kGZeY2Pkkivv2x0TRrawa0TTkgiuVUTLDBDCrSLaxxCbdFGjtILeGJTI2ZGA/ek5JrwPVPBGl674iXSrsuskDtNF5M0kMu1GyQsiBXwI8ZIeJxklWB5rweG6PhnUqZhOvwLl2Vx/fUq+Mhb2n1WlOTc22naafM5cvxpqUrt8q7uII8e0oYOWX8XYzHzbg44etBSp1a9T4lZcjVlpBN+7Ky1tzS83g8XfGO9srprX4ifEWN7q1W1S0vfEVzeWdti32nyFnN80TTOxkM8F27K8nmaeYSEC+u/CH4q+PYrzV9L8T/EDXNQv4VgQ/a9StriEXNuLzzGhOo2KyPcOXSWQqnmMiCO9eSKJAPSbXR7HT7R7b7JHIk/yvIcF0IX/AJZltxICjn5iT1JySa8EvvCx1fxpCsQkhms71pLchwHc3cbxi8hcNkC3jncEMMtkkjIGfgc/4d4Cz7D43C4LIcBk0YKWKwef06S9rRlFx1k703ao04ShvazUkfVZTjONcuxOExGNz3GZjzv2OMyibjGlUdRO7jK1SS9m25xnezu1Lmer9K8feOPiuZI28N+M/E1pNaxsjz/bbqdLmRfPMbXGmx32nWdwtgZ5ZU+0adqYaUIbhJFjVD4vZfGX402+sXFnefEfxa8MosB5BvZDbobYXQMkUzXD3McrloHmDXa28rKnk24Cyq/041itpCtneI9w5hWKS4QE7XCclmYk8gMclifU818lfFqKHwdqRvlijlW5RDb6ct9JF9sukmzudW81irxtGZJRvKln2KMCr8MeAOAsThlg6vDWCzmdajNYbFufv4lxcpX55zbpyk3dcrb5mtYrVZeIXEPF1Gq8Vh+IMTldOjWp1sXhoxUn7G69po6bc0k3daSnFNpybafpLfE342398LnRfiT8Q7a7mubuaWOwMN2lz9pWNleSy1w6vaBNIjQGIxWNkikN9ua5DeUTw98SfjHDfG11b4peO/Ekc7RW1yPFt3pupKiWz3B8uK0fSYIo5lknYtMtmLjKIJJCqbad8J/HWjazCoi0+5i01rG1ltrm2H2m3khvYp5cEbY5Awt44ZArAsBKnn7XYgVtc1DR4PFQ+yYVszztbyTM5tI7qK5AknZSwDIpViC7ncQc/wAVfOcacE8L4OjmOEy7gjLMDUp5dUzLC42zqV4Yik6nPGddScVaUeWX7xuTcudSjdP3eHs/zfETwuKxPEWNxsamNp4Ophp8lOm8PUUHGSpuKnzSU/aJNOSukmneZ9TadLLc2sc91I0k0qJIzscs5dQ28jPJOfTvxVxonbAC7yoJJBwWyc8569eeB7nvWP4OMkuj2ryqY3cygRg73RQUIVm3yAcE5G9j3yc89kYvlVgFJDFmBO1ipUk7iuAR05IHcmv4ExmLcajTVt3Df3k1J33t63k/evq3q/7Ey+jF0ISS+K1lZpaqzfW95Wk3zczbMVck4yQoyShOQM55/ixxnuT681PnONrHgAbuScA8jdz07kqffk5q+1vll/eJGX5QHAyfmySSSWYgHsAOuSTThAQpA+dhnB8tk3HHO9uO55788da4q2Oi72d2n7ybbvo/ie299db31W53rDR1+K7dnNN2lbmb5m7vWTs7PVvp1zTGUyd5yT/CC4JOeSBnJIwc/XnNHyqSC+CTk8OCD3KsPXuc55PetIw7c7VReQ5GCxycE53nPfOTzyeaVoiOnAx8x4XccZIDBTnIIz90+hzWCxibbtNtvmbV7uL1bbd3vZfN2b2NFQqNSulF6OLtdJLW91J633u3e+7vrllgTIpJGcdckltucndnOcc9zyT3pYHJlXaxJbOFOShKDl8jI4zkggZ525OatSRHLpjIAALAlm+ZSdp2jr1PXPXvmpIoSsUmd20R8bTxknhSG5JJyeue7EmuWri7/a1d9XZ+9K/Wzv33bdnorm0KFVu6ipPWys9Vrd7Jve60u9dWySC6KOD5jHnJiznOCeoJ79e/qK7LTjNeWdy1oJftICNhcu2wN8xUScbgm4/L8x7cnnh9quVKghgwB2cMCeu48fyOTXpXhhBGyxtIvmuN4QsPMQHvsHPzDJBPJ5IzyT52LrRppVY1Pei1Pklf3+V8zUlrdNWu97Lz19LC4WM1NXnBttzvzXUna7vpfRqTck9XLRaX6DS7OeK1QTmSRssxeU/vNjbSEY/7PJGT3/GrU8ccccs5ZdkQLNuGWBVcnIyScdf1Ga3ljwjtwSOMnnOAOeev+feuK16S4AmWBmVnRhv4wO+5gxHU9Tn1IyaMLmc8VLSVOi583NJfDFNrV/aWl3d6yd5XTbOifLQcZVaSxKvzTtq5R2967d5O63fvOLuldt/G3we+H3hPQL6DUPD1paWkBgWzRLSC1MX2db152jNxHFuAluJJHZDIylmBI+VNv6TeErwJDCmGIZVA5J/h6d8jHf8AnX5GfDr4R+OvCGq2GqQ+PtcvIY50+1Wl/Bp95ZtahZ1e3sxc2D3USXDXG9t2ovK0qJJ5mC4b9P8A4e6tF5MEck2Cqr8svL855GQMnHB57Zb1P0OL5KlJ/wC308aotxqRb5p2k3u7u81u773u1oz5PhuvCE6kMTh6mCS5Jcjv+8m29XzOPvSerlLdJq59A/ZRKhfjpwRnI4PX/HJP9cW6t1jJXbuyp/Eknrz15Oec/wBej0yRJYRnowznBGcjOeR3H9e9MvLeMlm9Twckg/5/yeTXxyqqNbkc5N6ysk730+K6163d/wC9Y+6spJOSjK8eVPlbfrzSbUr3b0b1TfmcDc6e0qjCg44OccHJ6c5/z+Ncze6YIyzFNuepHVsHOQcHPIJPPJ6k9/TVgBOCRySOCOevb19Sf8ax9ShDBwVyiggnBOeeRx798k5z710RxE4u0PaStJ3TevM5LVK75vx3bbd3flxNKELSjZvVRs9WrJtys2nbe70bbvrc83e3AxkjcASWyQWOT0U+vf8AzmrLbKyYwATuYszZyefTn8xk5OTkmugntyWwq5U9D/EOPcHPXv781nyWx34YZI5PYZ9cd+PY11SnP7Wj0lTv/e3b96177ptvuru556pxvrBzbd9fs3vrre93a+l99b3ZkNZLk9WGemRn8VPPbuPrnOTRk08ybgU5z8zbRnJOQw5z0HXHXvmul+zFxknkg5x1/Xr+XNRG0IfPzBz3BIP4jPp7VpGpLW04ya99RacnT5rvXprdt973vffR0V7yUbvVrVP2jbbettbvXz1bTucVLpuBJnLHdwevXHbbzzknjvzXJa1pkvlXHyspMe0kLkbefQZ5PXnrz716u9jucgnoCRy2cjPOD1OevXn1rIvrFmilQIWDIwDep+uD3Poc5+tepg8TT503FybimpPSPK3rLZptvdq7d1fq3yYzCRadmnJWm4t35bu+muid3vvt1d/jXxVbtbH5htLbAXKs27dMo2MmBu3AkH94hOcZ5Jr6T8H2Ef8AZchRI7i2u4o5CWhUM+UwF5TLBE4wWwP4OpJ8o+Iug3iabeXPlvPtYRGKJN7ZnlQAsAvI3H5jkHnPvVn4RfEwNb3Gia872uo6XLDbrazyg+datbQ+XdW++NGCEiRCr5YOjFcqQa/vvwowFbMeGYVsii8ZPCV3TxmHjU96jNzvCTheN48slP4re9zNSlv/AA7x9mWDwHEToZpVWDWNpN4PEVI/upzvLmTm1J87ajDlbvdNR0TZxvx60Q+Raq8cEIdbtdODLIFFwj2sjhxGGI+WNTu2vznJwcV3v7MWjXfhv4aeHZreOULr+oalqkwtn2qUu7swk+XOfl+wNZyhQX6BmtyFlVRk/HrxBa3A0aO3PmRQT8+VIhkka5iffcqYjIQE3QR4IRuZZMMAteo+C/GOiaf4T0q1u7hdPSwskWKS/mijiiVg8rtNdsyxIrSNOVyY0EeCMDNftXG9biOfCVDCYPJp5m8Tj6UMZhJNtwwkHUm5t3961VU5z3dr/a1Py/hLBZRDizF47EZhhcOqeXOGHxMPtYiUnH3n9luDktld6ubu7/Lf7UOgNe317qt3cS2eNalaVpIzcafdQ2jqkYuFVowry3JhlV9shBeRmU7WavH/AIF6Vbr42n1K4vZUk0+zlt9PgEsP2a+m1aSRHlmkUK5FsUsGhBlIkkfLAmM5+t/j3rfhrV/B9nqFvqNrqGlaxYtdQ6hpd3BcWd8Yx9phu4tXglaCW1EFrcvK6XMivGjRtkyKD+cHwm8T6V4I+KWkr++Sy1jVLu2sZrjU9QnjN1eidpIUneS9fbJb206tEVnjiVi+YsCSv2Lwqq43MOFMXg6SnSq4fD4nLp05RU1KrCLhUT5km72fxK7i1LS93+ZeJOIwuX8WZVjMbLB1KOJrUa9HESbUqdPmcqck/wB43KMpt+61eTs27u/3PqTWp8Qw2+otEhurxIIzLiUTGQEuY1kRl3CCNyCeTjCksea2s+C9NtfEkF/ZTWlzAkiIDdwK1yts2S6skZVSspVtpKbdyjfkgV0l/oen+INQs9Quo/tcUN5b6nbu8gaK3eHeU3tCV3tbpM2Q2cn5hzg1heNvGHhrwxrunx6jqlnaWWpvBpsVzeyw2dnb3TJPI5u767eJEjEYUu7uArd2ySv5VhMLmKWX0corYnA1o4OWGzLKoU3Kl9US+GMtGqrXOoOM7w5pO1S91+w476rFZjWzeVGth5Yn63gsxnKUaixMpK8nzN+5flckoNS+K8evq+my6PdXKwX1/BbyOjvAlxNCklxMRkpArsGdjuJKqHcjlQeTXiPiBTbeOLd7P5GBc3AjjjIuLWaSMeS7FC3yrG5ycbQxJB35rtjoEWoy2V38zRRJDfQ3UbCa3C/JIkscg8xW3jaVZd4YHgsDXnnjTxpoWgeJbK11a7+xPdK1lbXDfPZzyoN5heVC210Sd3bcEwNxZiQcfEZFkSlmE44LNo45VsNXwuYcPOjKlLDQjLEXqRh7Tnn7ScoxfNGzhT9pTa9o5P6fPMzUMNzYnC1cOqFeli8FnSqxqrFJ+yunU5LQ9zmbakneXLPZo9MtbvTLu5liuZBaKLfKlEMxZ+AVROuc5PbnJY968OubC5/4TTTp7W6vLYafrEV5dS2brDc3NqLiUrZXancjpcwR7HUiXO75CrYkHZLaxajcWt6DMBYt9ohlhYbdxHOSvd43YfeOQxyCCTXn/ifx1pHh3xno5uJ7ezOsTW2lwwyOkf2qS4kuON1w0SmWOO1uX3LMxIG0pkjd7WQ8K4mnKtQwHtK3tcHWw2KoOHNGOIkp3naqqivJt8qqSqRbVqkG1zPzcz4gU0quKw/L7PF06mDxtSXKp004STTpSi5Kzcee0Hd83M+Zo+gBd6TM0duVMU0lspiaS4iSSZmU4zBI29pM7yVG45ByOM18ZftD6ZP4gkt/7NluYre2u4IEa1jje4m1F4b6Pz1jeMkM63jKgaO5j+QvKAJHr6TeytdVubK/eWfEM8d1aTWzqjq0ThhuG1wSCvJIyMnawPNefa9PpeqeJxp8t1EImuYhcpODGRiSN9/nIcHygVL4P/XQj5q38N8vpZZi6ValXzHE8tCpi8dWrynOGEqwb9y/LyrmvKcoLlamrKm48nLPHcq+OwdejWWGpRxFaOEw9OnyxqYmnKUffb5lO7aUee7bhLm5ua7fOfD3QNO+HfgeEanJm9NtDf391cmMPLftHFGZHkTCnEUMTy7VhDzNK6IAwUeG+FfE93rfjHUb8Kz2WqarA7WotpbxE02XLmKCQIxZFW1eRArIYl2eadrgV9iePfD8NxocVrCgllVRLiPDsnOd4Jyf4n43fj3r5e8MeFD4b1y2nE10/ly6gltahPOUvcwCMJNNM07qY4mm27JIS7tyxDOr9mIhkua5bxFmNWc84r5hRq4TDYepJqDwesoxotK0YTklJySu3Lmld35vHqRzjAY7IcFhadTAYXB1IVMZXi26lTFpKLnVUnOdWSTaXvNyd9d2fe3gFkFotoT5rSS745FAO1niU7XG4ZUhAcjdyTnrXpn2BuXIYbcgj5ApyCfU8kEc8e/PXzv4caZJ5EcswUMluGeNmJkR3xhWwTyg4OCee55NexwWTMrEjK59CQP8/n71/kzxFFU8RUin7OUVyVoNyk42XK0/h1Wqk1u9Vvr/AKL5JFvB0JwcKycHVhbVq7d3r7123s31bd3c5g2zkssbSKDtyN2BgEkgkA5xn1B7n3Q2rMxG7GCSA3APBI3M+70z9cmuxFqFyoU9egxknk5z3/T9ao3FttJ4O7/az0OOpHf35565r5/2s53UbQ051JptyTT1ert1131b5rt29ZU7y92jJJpxjF6uUurasvPVaaHPC18zgfKAMYbk56nJye/IPGeSajazwxGVIyMqeRuA7E9c5+ueproljaMjA68Y5/E9OvqaRrcO33CAOmATnr17/X+Z61HO9qkuZPXnV9LNyXVvVXber3bbSZtToxbdlyt3nOLvdP4k3JbLXTd3bve9jmHt1DZKrx0IBJyR3JwM4J/vHk88nNgrHsC4cgjB2rgn6Ng9fU4/Pk7L2jH5nXBOQo6ke5J7kc8k/rmkW3ZRgjk47Dr7+vPv+NN1F7jVWM7NJXa0fvO9976tu7vd+rNfYS105b3m4SaadK7V9WubmW6d3du6vJt5FpZxQuZFXOMkCT52OD1I9QefUnr1rofDljIuox3LRsFeZ5ZHb5mbIc5ck5PzH39qLazAcbkPzP3Jx3PODn69f613Ok2u0Aso4I9SAPf8f/rVy4qs37SUnFXXJzvT3Hfmd2+Zyd7N337M6aOGi5KUKkocrcnCLspyd7395t93q9b31u1qArjlmAwec4GeuePc859ea4zxJCZLW4Z2cKQ3yxrvaQANwCWXn65z7139xauY3dQmwKSSzAYyOi5IJ569ye9eZ+LLm5i0mdIJ0QhWcIPmYspPPX5vccn0NY5TUipKcKlOk1UUPaSd/Zzcl711za735lZ6fOsxoupSqrn+rTsr1Pdeu7SabXN1V007t9z+ej4f/wDBRD9oyO2sv7e8D/DTxJLHaeVdX1pf6/4Yi1COaWFra+XSn07WVimltYrozxtq1yZyYzaTWKiRJftH4Y/8FJvDeq3kdp4s8A634TulmYGOG+tfEmnhUluB50d7GmhtbO8f2ItHPJf24d5gbtREvn/nf4F+HVn/AGPFPcpbzyzWtoJRb5LSrAZCWnQRQsGE89wFGyJlBLZYMDWjd6NZRanHHaQoWUyG6jbaHgjXIIm81Cdzq6uHLSAjIByef23C5bw7VcoQpzo1U5OrWhyckZJNqVXmu4rmbu+ZySvzXlqfgtbMuIKVRzrV3Pm5fZOV3LkktIx5ruUra3bT5m9FZ3/oV+Gv7X/w615ksbDxdod86NHDcae2u2Z1XTpZ2mVftOjXLxXcO+4+zJHGUufNEyvblgNzfQmn/HHwxqNrbNHcLL9oSN3i+SOVS5k+Qh5OGZonzGxEyHK3CK+RX8t1v4BtrmC8/s+0toTqkbDUktoo7RtQVlnUQ3rwpE06It3ckealwYzJIbcqJHB6ddY+Jfh7WHutJ8b+O7S4uLxJZLeHVHk0pbmdgZLi60+d2guRcNKVAnh8pQzmBVdt58CtwTRxNe+GdWMk+WcVJU05NRbqyjU5ZrdWcrt6XTbu/rKHiBWowvicPh6j0vNrV/E5axTV5ct3JN35dd3f+neH4r+E5CsjXtjGdxR5XvLaOGJlMWUmeSc7WBuLcbWCsWdccsM25PH+iTq0IngEv7zfb71DqqE/eJxgsFbGcZIbGSDX81dx8UPj1o9pZiL4q+L7mO1svsNu11NpzmF2juhJe3JurC8MzT+fCHiebyE8tTYJCDOJs2D9oL9pDRbC6iPja71EG0t1066uNI0RLuC50+aeWMTLaW0UQQC7uVeK1sNJaWDbDJOpw4eI8L8c4ynHHwbuuWk60PaT977KcldrWTd09d7toU/E7Cz+PC12pXjUrJ3jTVub3pcrd9+t73beqP6UH8VaI3mkyxholjMqo6s8fnAlWZFJb5wGIJUbsEjPWs1/EehSlAl7FM8rIB5UiO6l8kb1Vt2WHqM5zmv5sYf2qf2mbWaaVdS8LtbC3dbGyi8P6npLWsxvppReTXth4hmlnluLeeRJo7s6payTJFcC3R1IO7/w2z+0O9iYbjRvC8dyzzJPNMgvBd5jt/LmtGm0uzWEGQXTSLNZXxBbfHJKdhj563hznEI+0pYpVWleynTnUpU1J8zlHnnJylK97J3a7+8VR4+ytVYxq0qsV/Ep1mtZTaldqTkov4rvmd3d3bbbP6ORrWloCzXCBQvzDcrHdyMHknccE45PerKX2nNiSK6STdnG2aOQjOPvbXJ7+n4+v848/wC3P8dre4uDL4Z8OXIvnjEt/aX2pWmsNai5jYWkGmalFqVpHOLCJYRPE8cRILrbKHURvi/4KC/F6NoLuL4d6VDdCSWQWF3q0RspI1upxFFNfQaGkx+02vlbwi6dJaSmSXz72MKlEfD3iRSUIunWdW0IzupwSSbblU5uWErvVzkuq1aZ3UPEfhyEXUl9YpSTl8UYuVdP+WLvOXM07Wcrp3T1R/SJu05o1aS6hj8zIQs6IznPQKWyccZ6+pOTVaeCzYEi5iZc7GJOF3E5ALA9T19efrX883/Dxnx/ayzPe/DO2vUuI/3zaXrE8d7BcySsD/xN57yzlkjFn5DuFtZ3W4Z47a4aKMAbUf8AwUx15LhoLz4aeIobaG8ItrvS9R065kSwlt5PK+3S6/rd7LPJBM0UciRadZjeGuQzZ8k+S+BeKVKLo1atZVJSpwpypSmpKLmppSindxlGV43v7raVmr71+N8ilGT9vNWadXmjyRg6iTipXk3ZqXu9G2uqbP3Xk8M2OqrqlpMIZVlRJWjE5VtqyEiZTGyOD5iqQ4Yc9DnNeFal8ENG0zXhr1i11ZXpS4F1HFdTm11P7XdTSmW9gluJY3dJLh9snlKygIkWxI1Ffl3b/wDBThLM6frep/DPxPaX8unXU+qW+la0mrXmmCDeRplpeQ2EEN0s4tmmlje7CeY5it3LKjS6Op/8FLmnvYoLK4adQc3Fuvhq8tbFbCSOZzqLXepR2pkuUgliEkUWv6igdUFpAZPtJi/pfwe8ReIeGKVSGKwtd0K0nQxfs6cpe0xMIxhy62vzR5LPWLl3dmfy/wCN/hnk3F1TDSp4xYaph5xxaXPODjFSlLmjaW7kpNq94213d/0K8W/Bp/FLxM+oPp8cbq74W6edfLKlbm3Mc6DejLjazJGR/rASTnD8R/CO/wBU0aTSc6VeSzxJFJqNxHe2oYxTh18y2ieYqp2qrEXFxgksUYfKfz80b/gpdrqXgF7YXjWU0OmNP/bHhyyuk0ue4OpvI73uleKNJiWzhlu7O3YxWmvXsnlxTQCaNjGOvsP+CmPg0yJDr9j4ptvtc0kFrqGgeH213R7lVjkY6rb6hPcaJOkMTrOiiXw5E7yJmNrmLbPJ+94f6S+aVHCllvD+JxsE3PBySjLmxNuaStzRlOULOU4391ayurs/G6f0bMtpqpUxmfYnD1KkksSlWqx5sNfliqk/fTjJpOPPu5Sinex9G3fwq8bRaX/Y402ylit4EtoWm1qSdbC18uRzHbG8t7gsmGkCJHHKvms32kIrNJXmV1+zp4jtBM2mrMtyZXuZZ7fX5opLlrghpIGZp4FeBlVg9nLdRwyK8iKG3qDiv/wUv+B+wRTa54+lhDRwyRwfDnVZb1JjDAXkggvEg3Qs17bBXDmeRi5soG8uULZX/got+z+0Xmz6n8QYhG1sl1Hd/DnVIGtBcW93IZrnddPJJ9jFoqXC28V7JHLNErL80jRxkX0suK8JKqpcIYWnSr1JSqxpUpQ+sYmdp1JVU3ZuV1OUmtbtzbXvH02e/R74exksNVjm+Ip18JD2eGqTqzlKlhXaKVNy5nG3LZJNvRNNM9S8M+FviLo9mtnKrW9uI91vKt6yOsOUMdnf28d40iSW0N2YuA4Jgk8oqvlGTzzxT8JfH3inXWudShs2so/PihjudUsLuC4MtzG8V7I5RrqJ7ZLeRWiAk8wSRM8gkiyYX/4KHfs6pAyy+K/EFmqX8Gn3M974C8QO8NxdG28p5LTTbfUX8m4e6iAuM7Y1/faoLeFvMqg3/BQH4ASySpJqPjoS2wkJ8r4falJFM8LhGTT5re5na4Md3JHGzxwbCCZYDJGGYZYT6YGb08XPFUuCsLBTU+XERb53Uik5u7fI7OV5NSlK0o6a3OjF/R4ymvQVHF8QY+spTVSph5VpuMKbbaitXJKVn8afM7py3O00LSPi/wCE9BsdKsLS5MOFtIIYfEgNrp0xh8yW9vNNkk1D7NaRzNdFxbyakTKALKGR3XPnGtfCL4q+ItSiudbeRYbaa5j0ZoNa1O9sorWcwO2oz2sELCGW+kswziS5mniJ2dZirbK/t3/Au+FwdO1Lx/qj2iWszHS/hvqtwt3HevIqtZXt9Lp8E32cpEbrbdBreSQRTZkBFMP7dPwPVI3XWPHiXU0V7Immw+Apby8zZyujW11HBfXEccl1In+jj7YTP84iYPFKo8/AfSpzKjOrjcu8OOHViqmJliZ4mnSlKc8VJ6znJUpP28aM5JVG+Zucnbqeri/A3LKtCnhsdxZmmIoQorCLDVq025YVtOyXNGLpupGPMnK7SSu7u+vo/hr4q6Bp/wBhnt7i9ks4Y4bKa11aC3W7SaSb9zdzXl1aHiKFAGlijSMOu0Ahi3lOvfA/4geMbuKTVjDobyNFqY1G61Q65q9hdx3N472unXqrdvZqyk+UyWksG91MkSJnd2c/7dPwTaKeea98fww2zeVM118MfEUeHFyYiUSIXTSrHKGZ5LZNQUR/vEZhkrz1t+3B8JNU1FrOyk8dXVxIX8mwi8B6jaX9yrQwSQz28GsTabkXgmf5JX0+ePZIJImYRiXkxP0r+J+avPB8F5Pl+Lq/7RLGQ9pzUGv3ntOWdOzle85c0eVxte6ba6qfgNkc1SWN4ixWYYeEZ06OFlUSjDm5qbirTvH3fc+JyfNpZnq8Ok+ObKOZdQuLS73tM6y2NxNC7NMJmUTIYogCk2zzTvcENmFicg4Nh8JPFzarF4t1O+ha4ntCZrCDz4yl0Efyo4IreS6gdBHIQXklgkU4beQpV+L8RftV+FLWTfHp/j0wvGZILebwvYQzSSLcSoYBC2tSNgW6JcFg1yBFJlA0ySQr59P+35o+n2epWEeh3V1dQFFiEd3qkFvD5Ztt8cKan4Q0KS48yNb51lhu7uEo1v5TtKzwjy8q+kJxrhcPGcOFcJz1qPsa+MhQcY1qTdSMXGndu7SvUk377SaUU05deY+FPDuJrRdXOMXV9hWnXwtGWIk+So1ByTmmuZXb5U0+VSldy1R7/q2u+O9INxp0ktvDFHJiW3u4BcTIsckLlY5txDLLCkwx5mMSmRHyFq74csL7XdXhTUBBBGlwtwRGmVmT9242LiTI8xhuDlSwDc92/P8A1v8Abas7jU72YaZra2sn22W51O7t9RRo/Ojdllst/h9g4iLxxIENy8shJuPIjR5K6zSP25/hvZaXIftPj6S5sluYfsth4UvzcztbyYhvba71S30QyC+COzYkL26gm4jYsgfnzrxszOeEq4fDcIPL5V6NShUx1OFSk6WInF81RPlm7U5xmkpxg7P2i5k1I6sB4a4N1qVWrndbF+zrwxMY1n7SM6UZO0ZWlaXNGUbv3ry31uft14QsLKK0Ox4UbHzSyOivKByW6jq3OPX8z3ieTEgJlXDNtGGBBPoMZycdep6n3r8M4v8AgpR4bgjuLW20PxVKbaWCK2e10yDU7q+eB7lLido7fxJY+XBLPbxRrNcW2nMGl3XscIBLXNS/4KY6ZbyW0llovjOWB3upJRpXhaW8tZrGKZFhe/Er2lzBJfF5C/kXt6UiBIkC7JW/jTMcg4kr1ZVZYK8a9RSw/Mm+aVnJRU3BqTqR97lu5Wd97t/1XlvEfDFGCoUsU6k4QjTnCNr05O/M3bZuTfxJu91e92/3Gcwsi/v0X5sscjHB7nOTzj61QuTYAky3cSsNwTc6rudFZmVQSSxVQxPU+ua/Ci6/4Kb3sitKnw3+Is4gSdpLj7Pod1cXdxBprSottod3OZYftl75luZkvtS8klXjtJFc5yLn/gpNq11HFdRfDLxTEwWQG3n1K0WF1aaJTn7ZGt0srCOKb9xpljE2ZRIjfMH46PCXEqledOjhm0+WGvuwlZ+82u7+07pzWnvRv0Li/JXaNKvyptylLlkm53bsnZv39X7ravf3t7/u5Jc2Mflg3sSFzgI+PMIOemT3w3uece7Uv9NZXC32JQ/liIjDsxDHEY6nO1j69z15/Ay5/wCCiXitzLaaZ8NdZlszdFnk1PXINNv4Ratp22CDUrE3ZlOsY1CdydKaGNJY7bzPkknrHuv2/finqsFxF/wrmxtIJUhuVgvPEi3v2W4ke4B0+WJYbglbIwQOk8V5LJcrcZuDFNbMZ8o8I8Q1LOpKLTlKEZxhJqa0b1Ufet717O97ti/1zyFcynO8205RcXaMG3q3zc17XSdrve92z+gY6rpSRSF7uOZoS3n+U8bvGQhcI6qSQWTnkc8kE9apnXtIjCN5jbmDEDcpG3I5znuf1z1r+eq6/bo+Nt0XXTvAnhHTlPlbGTUdRuroW2JVkgiuJNP81AsJgVCtkzn995quskYTzjVP23f2gUvLab+yPBmnmG9Y3ttNe+KpZ5tOlW0jkNjbRXNjaW0s0FszIH0/VF8zNzzM8ip0Pw94grzToVXBy/eJ2tJxWkrqbik25JJtt277nm4rjzJldy9q1GUfZwcm9ZbuTcm57txejT+8/pb/AOEr0K28pJJ9jSuuB5gLOST8qoSGJzjoGPOenNasvxG0TSUWORLjzPmyI1jdyiuqhjG8iufMdiB8pJIO3LGv5gE/a9/aiv7ZBd+JvCcUrw39sLe38Iy3Wm2TTXEkkFzZNqGtvMkumBLYIZJ5vNIkF8ZFnkD8hrfxw+P2sw3kV/8AE3V9OW6lmnuYtBsdN0uO6ke5dhJcm/TWC0kcEsysTcMHbbLGodI9mcfD3Mak5U8Ri3Fxadd/CoK7t70mo2lrZtu7crdWXLj7AQfPTwlTmUXShrzc8ftX1nJ+970tGkr3d2f0seNP2nvCfh5bhNRurmyW3t72cR3cMGnr5Wn3Nksxlur27hEeIb2OVWlEEZyomkjLZr4d+IP/AAUN+DvhG8MGoahNquoByLrT7Gaxnug88EcsUdhG2poZ4ZHN1E9xBbXuyaF0aINnH4P6nqfinXLmGPxN4x8Ua2Ipg1qt7qjyFF8q4UiWW3gszOrSXt9MPMD+XNO8kDAhCuzo3hKwihKWX2ayTaUEcceyaSSQkmWe6LyNJJI+SS6M7Es0zszGvucP4fZJhYSji8XVxc3rGjSvUjzuS5lOreKSXNLmnyzs05WlytnyWK8Qcyr1HLC4OhhKdnCTm7VJRd5XjCUX7z+zdaX1d27/AEh4S8f+I7bSYLqLRpw8i7bu31DzPMlMsaSJNazR/M0dyC7IxhQ537QQu5rJ8c3+rtsuYLS2vluJpb4aeCluJHittwtpLma8nIlkjcPHJKwj2qtv8m2sv4b6zpF/o4tIZoRPpVvpljbu80d3cvM9nLGAU37iRHYzFmMsbsQ5nCOSD6TaeFNHvb2KeNY2njeGS4WJ0SB3j3YULEskvmlhDna8aur5YvJv3enTjCk589KcpS991YwV60ZqNlNVIuLhd8zi5Kcufl5lZyfh5lSlOpD2tR1lFJ0qspNJJJtuFmpe0u93ey962uvW+HLibNoRJKqmCGXawZizNj93KDhsrySdy5OQ3ofYNOghvo900MW5UVB8o3N3w+9SflIBznk9a8mW0XT5lEc0YbE0kSqRsfaUBD4cn5HfDYwd2c9zXY6frc0M8kcyDOwNGkUhZpFPJcgjK7cHknBGce/qV1WqxglhqvNJc8cTOMffvzN3S55+9JqO94yafNZJy8ajKMZSc69G6lKKowlKXLre3NLlTlvJu13eSs9WaOp2cLmZCEWNG25K4YlgMgk55ySeM56g8mvPLyxiCsgO7cXVmdTgM2TyOufUcHrk5NelahqOmrAslysxdwcRhS5cMD6dxg5LevHJrgr67iZpXJKWpJ2FisaKCMneynO4EEct1zg5rqhPFU6T51GNpOFeE25Sw9OKjFuFlLmTs3HmnG0pa81zOUKM6nuppyXuVUklVqN81nzta6+9aMm4qV7WZwd5pcQQxRwSrL5g4LYid36tvZVJAyx4x329zWRfWLyRsgQJtGWEkDLIXUjG5H2kZGDzywIZSQ2T6I82iywKn9qq0zxyTSB5GTZDGnPkfIysqMw3kyEgZPcmsyH+z5Axa9hkjYfLsUtvGDhnGWycAnoPUmsP7Qmmp1pTqO6iqMOaKr4msk+ZJv342fuuSWrlNNqcW9KuAs5U+SnB3dbmqWnyU4u6u1tzS1bTleKSve7PN7yxZbcBoi8q7Hj+QlgTwWXI656H685BJxm0/wA6FZFQO2HbaCN3RuqqMZlIbGXbv716XqKWUMTCP9/JMhjt1jaR5JGUkgIPrknPcnc3rzOf3UySsyzDJeOFy2FiPEasgGfLIOTg5yeSOT6FPFud4wjUi42lUqtbXSkradY6v3bWldva/jTwdRSnKtZRqNzpUOZXioyk58yvK+j0bbd7py95nEvbJHGZBbv0IK+Xho2DLnJUsDyc5DE5BIzzUZt0kUssLIxPyq8auzMi5ztMo+8AcFpByfnFdTcW4aPfKqxm4ycNIq+azN94jk5OVPKk8ndTYbdJADIqRzFXRYjIuwsq5G1gcEONvzdTyCc5rsljKUZOM63LL+NKN3rSt7zUrSjfmlom029Xrql9WbXtHKfvP2NCppJU5tPlum+aStd3tPvdrV8+dLjkiKvHE+0hsMiHBB6oh3YPckO3U9azm0W2mcqYguR87qqx5UZO0MRgnjBBJPcmu2jW3eKRnXbuK8RBXQLgHDHnAAzk4Gc9ck1A1lJG6ShQIXBC7sM5wDy4QccbcZPU/jW0ceoxk1V5Ic3tIxk3olezb2lLR23fMr73Zj9Um6l1NOUouErPWb11u9dea+stders+f0rwQNQ+aRFEARC8fmM0W/aGMciRbt2JFHykbS2HbOKs6r4ftYHuEFvACUliw0aBFAAwikZyGJUgDIJyVOcmvS/C/7uWa02KBMDG7APsjeRQV87AJG5icHBye+TWbrel3VtLIvkrOm0ooQPIELH7rF2UnBOTnb1yuTmu9ZpVlKUeSrTSXPPkd71Fe95N/E113a0fW/P9VoQUvY1Y3k40qalKV5tprRX/n5nrpdXTu2zyWK3t/MCNHbyOm5IyBG7tjYrMz7TwdozyCTt4JGa6HSrS2mmmjZreTACPGYhiB4ckAllPZyw3bM7iVJzkxzWrhXmhiMWJRGYl+fyuWLAjLYIYck4P973S0VoZFuF81JAcupLszfMDuIHVgSD91zg9cVU8bibSnSrShpKEZObSqSbas5Lmau0r2u01o3ct4OjFJVo6OXPKCXNKLS5m9Zq91K936SdvefTWthbRym1ja0YSOZJ5LqXyVU7juVYmALyM7JwQ3IJkJ4NXotKtXvHsJHs2cgBGsniuMmGXHliN4nABYAMNpIyQCGGRjasbi3iS6gWcvIuyWeNmRkWRZM4dCpGdrEkdAcjnmuZt7+SK6R45XWNiCCqZeMjaQ8SMrlmRyxBZW+YknOTXBHE1JWnUq1J8qmpYdcvP7VNtSi3JRV7cq5mlJ63dnbop4e6ao0lyya5asqjcfZ7OMklzS0vJv3patPe7+iLH4XeGMWskgWSWO4+2Wp1G8BZbnc5zGg8p1jtmeRW8p7MSxkx3hdTivQdP+D3g+4eG1i1GXy4oZwiRXGnXFxHKc7Wh1F0kkAtQ6kRsku7jduy5bw2614R2kRt9QvFmkRMyQtP5keGUsIyfNMaszY7cklgeTXTaX4u1HTljeB3udwY/vXeM2sbsT8krAO25idqglVB+YYxjyq+JzSV60M3xNKTq8kaTWkfd6T5tdPdu5cza0uk29qWGwnP+8wkJcicpyk53nJSlZKLstOZzenX3mj16/8AhR4HiijSG50a1ltsR+RPqJmkd9peWWe4le5m8+W4DSIkgCxF2a22BkUR6D8KPhrFqcV/Zy+EWliEfly3N7okuy6cSO9xbzXGkTXRknitJctDqE3yRgQKixcfJ3ijx/4mgubmYXMjWrXE0vlxywRi2j3k+WTDYxyNkrCpH2zcVDYlUHFZuheO/EMkkEUmpywJNM8zCCCxG+VreclSslq6IqxbQI2gdQI125Ybjzzy/NYxhKpn+In7T99GOzlRqO8pKcpO75ZuT7t8l7vXZU8LNzgsDLCr3uaTqXj9Y1i01GTmpSkoytfVNzu2mfoXqvhT4erpsTabr3hO7kv7iWZ7ewuotRu7t7uEKJ03PPMsTw2LksY4bcuRtMckxEnwV450zToNXuLWGdb23jeaKOU70OIp50LIkpZlWRoy6fvZMggxsQRXU32q39/pzwXE8j2JC3N1au4a2lkRgUnmiwA379Vk2+XHGJAH27gDXDG3tGEswZBsjBKKm4bQrYKjB6YHPzD3zXXl9WlQjVeJzuWLUl7LCRcryjTvaK+JL3ppxfK9vf527mcsJiW1ClgaVFayxE4xcPivJ6tTlOSi73lZ8z+HV83lWs2iCcNtjSOU7Nmx9xwpLHecjaTghi/JJDZPNYAjtIxvMRCBz1Tk/N3IzySGyDg9ycmvQtZtkRvmZY1MIZX80NGSzYxnj5i5UAFcknIBzk8g8CbXiBwwyfMXDqrE8g4PJIzkH15yTz7EJwmpOVap7j5aiTd5TupaPW75WnJu6cnbo74+yq2XPCo3JvkV9FB3S3m2veWmq927s02hVCfKQM5UhVl8wgKy4K4ABxsJGPm69c9b3lxqsZMQMq/dRRkANgFlJON2wlueR1HNQ2Nq+7LuFIbdlcsCob+AyAH5seoJycnnJ6trNW8oL5cgUF8Hh23DPViCT0zkHnoeufGq4vCxm1HESk5J76KMVfTf+InLVX5rvWykubvp4au4Jui4NSclBSa51zaOUle8be9dv3urbUrZtrb7y7OFSPAzyrSFmzxwoOVKn+JjzmpZbWH78cEp4XzH2kD5wwKBihyykZcZBGQSRnJ29M8olymP3Z2TsUVnWTg7WbnG0Yz3zyOprRkt3dWRlURgyKC0kYy27IKg7t3mJnlgnUg7iTXnSxMYVHJyvtF8zbUab15tE3zSlo7yje10nfXvhhatSDtBtLWpJXvOV3o+aSdne6spayd2m3bgbnSmuEjRS8RG50IBYODyVcDBHzDk8nPPNWbDwyLGJ7gSSSPIitJukMgWQzTNhN6xnakTwoQVJ+XcCSxrfXaMfMm4u67d3zkKwy23jkZ5+vvU73tqYJd7kojGOJtsiLLKuQUSQghipVgcF++eprlxGKqQlGXutNRlh6ikuWlScXdXavo1OS95SndJfDc1o0uaMoqWvM41VK8pyqqad27u/MnHmvdR1bvdnLRwSRzqW5d2d41b5SeemdzZwQ3c+/PNal1olvfQb5Y4QwXZKy7V4XLAE9eGLHOGGSQTyazZpLeV/MjkUF1BQNlQWcjK7h3DFhyevOSOa6TTb23SE2kjRAqPLdSgkaUBuQN7/MwJJI3EtztznJbxVKXJOz54pqOsb4ebspPlcruEuZpSlzcrjyrlu0Y4ilCEZxruPLKSU4zdvbvWSTe3Ndc0ray5m5N214e5s4oFJOZE3BRgbx5gzhlRVbJADdcgnnrWPdyMI3iVQP7km1Sm4E/KAcfeGc59yOc13usG2ZkaFVdIk2ymMFAVDnIVgMAoqtwQW/Hk+a318nnSxhiVOfKK85TnkHDBto5YsVzksDWn1tzcpwhGXNHm+sate0UnBOVrO6bTu2o2kt1dteyslGftLqXL7C6UpLl52k+azu9Wldyd7tttHGXwZbqR18xgrFmMgyQckuELZIBJznjPO2uu8K649rG13AEbcsoVnj3sfLA4IAypfc4OXBwD/eNZEsYlukkZ/wCFHVUkCllIyGWRhyVIyeM89cHdXX6XawtDLIsaITuLHC75ZGIZmbA+Yygtknr2OamrVoS51X+sz5Wm6UVdPmur812o05vlcUpRSmpO85SNIUa9nKjCnZxlOeJfMvZWbkuaNruabfNKSvK+qWp//9k=";
        try {
            String base64Str = base64Image.split(",")[1]; // This extracts only the base64 portion
            CommonResult<Object> result = FileUtils.uploadBase64ImageToFastDFS(base64Str, "E:/data/");
            System.out.println("result = " + result);
        return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}