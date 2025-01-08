package com.tgy.rtls.web.controller.video;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVo;
import com.tgy.rtls.data.entity.video.VideoPlaceStatus;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.Camera.CameraDataService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.data.service.park.impl.DockingServiceImpl;
import com.tgy.rtls.data.service.test.testPlusService;
import com.tgy.rtls.data.service.video.VideoPlaceStatusService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;


/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-12-12 15:21
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/camera")
@RequiredArgsConstructor
@Slf4j
public class CameraController {
    private final ParkingService parkingService;
    private final RedissonClient redissonClient;
    private final VideoPlaceStatusService videoPlaceStatusService;
    private final DockingServiceImpl dockingService;
    private final ParkMapper parkMapper;
    private final CameraDataService cameraDataService;
    private final PlaceVideoDetectionService placeVideoDetectionService;
    private final testPlusService t;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Value("${rtls.video.t0.time}")
    private Integer videoT0Time ;

    @RequestMapping(value = "/getAllParkingPlaces")
    public List<ParkingPlace> getAllParkingPlaces(String mapId) {
        return placeVideoDetectionService.getAllPlaceByMap(mapId,null);
    }

    @PostMapping("/test-error")
    public Map<String, Integer> testError() {
        Map<String, Integer> response = new HashMap<>();
        response.put("Code", 0);
        return response;
    }

    @RequestMapping(value = "/updatePlaceDataByPlaceId")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public CommonResult<Object> updatePlaceDataByPlaceId(@RequestBody List<PlaceVo> list){
        try {
            placeVideoDetectionService.updateLastUploadTime(String.valueOf(list.get(0).getMap()));
            List<VideoPlaceStatus> videoPlaceStatusList = prepareVideoPlaceStatusList(list);
            if(NullUtils.isEmpty(videoPlaceStatusList)){
                return null;
            }
            videoPlaceStatusService.saveOrUpdateBatch(videoPlaceStatusList);

            List<PlaceVo> placeList = preparePlaceList(list);
            if(!NullUtils.isEmpty(placeList)){
                long start = System.currentTimeMillis();
//                Integer integer = parkingService.batchUpdateUsers(placeList);
                BiFunction<PlaceVo, ParkMapper, Void> function = (place, mapper) -> {
                    if (place.getId() == null) {

                    } else {
                        mapper.updateBatchById2(place);
                    }
                    return null;
                };
                int integer =parkingService.batchUpdateOrInsert(placeList, ParkMapper.class, function);

                long end = System.currentTimeMillis();
                log.error ("integer执行完时间是："+(end-start)/1000);
                if(integer>0){
                    log.error("成功");
                    return new CommonResult<>(200, "success");
                }else {
                    log.error("失败");
                    return new CommonResult<>(200, "fail");
                }
            }
            return new CommonResult<>(200, LocalUtil.get("处理超声+视频检测车位的空闲状态---->success"));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    private List<VideoPlaceStatus> prepareVideoPlaceStatusList(List<PlaceVo> list) {
        List<VideoPlaceStatus> videoPlaceStatusList = new ArrayList<>();
        for (PlaceVo placeVo : list) {
            VideoPlaceStatus one = videoPlaceStatusService.getOne(
                    new QueryWrapper<VideoPlaceStatus>().lambda()
                            .eq(VideoPlaceStatus::getPlace, placeVo.getId())
                            .eq(VideoPlaceStatus::getMap, placeVo.getMap())
            );

            VideoPlaceStatus placeStatus = new VideoPlaceStatus();
            if (one != null) {
                placeStatus.setId(one.getId());
            }
            if(NullUtils.isEmpty(placeVo.getName())){
                return new ArrayList<>();
            }
            placeStatus.setPlace(placeVo.getId());
            placeStatus.setName(placeVo.getName());
            placeStatus.setMap(placeVo.getMap());
            placeStatus.setConfigWay(placeVo.getConfigWay());
            placeStatus.setState(placeVo.getState());
            placeStatus.setLicense(placeVo.getLicense());
            placeStatus.setAddTime(LocalDateTime.now());
            placeStatus.setUpdateTime(placeVo.getUpdateTime());

            videoPlaceStatusList.add(placeStatus);
        }
        return videoPlaceStatusList;
    }

    private List<PlaceVo> preparePlaceList(List<PlaceVo> list) {
        List<PlaceVo> placeList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (PlaceVo v : list) {
            List<ParkingPlace> place = parkingService.getPlaceListByPlaceId(null,null,v.getName(),v.getMap());
            if (!NullUtils.isEmpty (place)&&!NullUtils.isEmpty (place.get(0).getConfigWay())&&(place.get(0).getConfigWay()==2||place.get(0).getConfigWay()==3)) {
                ParkingPlace parkingPlace = place.get(0);
                int isDetectionException;//默认正常

                if (parkingPlace.getUpdateTime() != null) {
                    Duration duration = Duration.between(parkingPlace.getUpdateTime(), now);
                    isDetectionException = duration.toMinutes() > 5 ? 0 : 1;

                    int currentDetectionException = parkingPlace.getDetectionException();

                    if (isDetectionException == 0 && currentDetectionException == 1) {
                        v.setDetectionException(0);
                        v.setExceptionTime(now);
                    }
                }


                if (place.get(0).getConfigWay() == 2) {
                    // 更新视频状态的车位信息
                    v.setConfigWay(2);
                    placeList.add(createUpdatedPlaceVo(v, now));
                } else if (place.get(0).getConfigWay() == 3) {
                    if (v.getState() == 1) {
                        placeList.add(createUpdatedPlaceVo(v, now));
                    } else {
                        // 处理超声+视频检测车位的空闲状态
                        v.setConfigWay(place.get(0).getConfigWay());
                        updateParkingSpaceToIdle(v, now);
                    }
                }
            }
        }
        // 在所有处理完成后统一调用记录更新
        for (PlaceVo place : placeList) {
            parkingService.addOrUpdatePlaceRecord(place);
        }

        return placeList;
    }

    private PlaceVo createUpdatedPlaceVo(PlaceVo v, LocalDateTime now) {
        PlaceVo newPlace = new PlaceVo();
        newPlace.setId(v.getId());
        newPlace.setMap(v.getMap());
        newPlace.setName(v.getName());
        newPlace.setState(v.getState());
        newPlace.setConfigWay(v.getConfigWay());
        newPlace.setLicense(v.getLicense());
        newPlace.setUpdateTime(now);
        newPlace.setDetectionException(v.getDetectionException());
        newPlace.setExceptionTime(v.getExceptionTime());
        newPlace.setThirdPartyUpdateTime(now);
        return newPlace;
    }

    private void updateParkingSpaceToIdle(PlaceVo v, LocalDateTime now) {
        List<Infrared> infrareds = parkMapper.getInfraredByTime(16, v.getId());
        if (!NullUtils.isEmpty(infrareds)&&infrareds.size() > 1) {
            // 直接在 manyInfrared 方法中完成状态判断和更新
            manyInfrared1(v, infrareds, now);
        }else if (infrareds.size() == 1) {
            // 如果只有一个检测器，判断时间差和状态
            Infrared infrared = infrareds.get(0);
            LocalDateTime batteryTime = infrared.getBatteryTime().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            Duration duration = Duration.between(batteryTime, now);

            if (duration.toMinutes() < videoT0Time) {
                if (infrared.getStatus() == 1) {
                    parkMapper.updatePlaceById(null, (short) 1, infrared.getLicense(), String.valueOf(infrared.getMap()), infrared.getPlaceName(),v.getDetectionException(),v.getExceptionTime());
                    v.setState((short) 1);
                } else {
                    parkMapper.updatePlaceById(null, (short) 0, "null", String.valueOf(infrared.getMap()), infrared.getPlaceName(),v.getDetectionException(),v.getExceptionTime());
                    v.setState((short) 0);
                }
            } else {
                // 超时，按原车位信息更新
                parkMapper.updatePlaceById(null, v.getState(), v.getLicense(), String.valueOf(v.getMap()), v.getName(),v.getDetectionException(),v.getExceptionTime());
            }
        } else {
            // 如果没有红外数据，更新车位为空闲状态
            parkMapper.updatePlaceById(null, (short) 0, "null", String.valueOf(v.getMap()), v.getName(),v.getDetectionException(),v.getExceptionTime());
            v.setState((short) 0);
        }
        parkingService.addOrUpdatePlaceRecord(v);

    }


    private void manyInfrared1(PlaceVo v, List<Infrared> infrareds, LocalDateTime now) {
        // 如果有多个检测器，检查所有检测器的状态
        boolean allInfraredsFree = true;
        for (Infrared infrared : infrareds) {
            if (infrared.getStatus() == 1) {
                allInfraredsFree = false;
                break;
            }
        }

        if (allInfraredsFree) {
            // 所有检测器状态为空闲，更新车位为空闲状态
            parkMapper.updatePlaceById(null, (short) 0, "null", String.valueOf(v.getMap()), v.getName(),v.getDetectionException(),v.getExceptionTime());
            v.setState((short) 0);
        } else {
            // 至少一个检测器状态为占用，更新车位为占用状态
            parkMapper.updatePlaceById(null, (short) 1, v.getLicense(), String.valueOf(v.getMap()), v.getName(),v.getDetectionException(),v.getExceptionTime());
            v.setState((short) 1);
        }
            parkingService.addOrUpdatePlaceRecord(v);
    }


    @RequestMapping(value = "/tsety1")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public void tsety1() {
        List<PlaceVo> placeVoList = parkMapper.getPlace(178);
        long start = System.currentTimeMillis();
//        t.saveOrUpdateBatch(placeVoList);
//        parkingService.batchUpdateUsers(placeVoList);

        BiFunction<PlaceVo, ParkMapper, Void> function = (place, mapper) -> {
            if (place.getId() == null) {

            } else {
                mapper.updateBatchById2(place);
            }
            return null;
        };

        // 调用通用批量更新或插入方法
        parkingService.batchUpdateOrInsert(placeVoList, ParkMapper.class, function);
        long end = System.currentTimeMillis();
        log.error ("placeVoList size是："+placeVoList.size());
        log.error ("dockingService.updateBatchById执行完时间是："+(end-start)/1000);
    }

    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired(required = false)
    TagMapper tagMapper;
    @RequestMapping(value = "/tsety")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public void tsety(Integer infrared) {
        PlaceVo vo =new PlaceVo();
        vo.setId(26803);
        vo.setName("东A-143");
        vo.setState((short) 1);
        vo.setMap(178);
        vo.setConfigWay(2);
        parkingService.addOrUpdatePlaceRecord(vo);
    }


}
