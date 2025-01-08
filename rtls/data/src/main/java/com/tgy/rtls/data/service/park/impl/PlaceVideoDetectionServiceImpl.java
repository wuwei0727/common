package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVideoDetection;
import com.tgy.rtls.data.mapper.park.PlaceVideoDetectionMapper;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-31 15:58
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class PlaceVideoDetectionServiceImpl extends ServiceImpl<PlaceVideoDetectionMapper, PlaceVideoDetection> implements PlaceVideoDetectionService {
    @Autowired
    private PlaceVideoDetectionMapper placeVideoDetectionMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired
    private ParkingService parkingService;
    private static final String LAST_UPLOAD_KEY = "a_service:last_upload_time,"; // Redis 中的键

    @Override
    public List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(String map, String ip, String placeInquireAddress, String licenseInquireAddress, String desc,String status, String[] mapids) {
        return placeVideoDetectionMapper.getAllGuideScreenDeviceOrConditionQuery(map,ip,placeInquireAddress,licenseInquireAddress, desc,status, mapids);
    }

    @Override
    public void addPlaceVideoDetection(PlaceVideoDetection placeVideoDetection) {
        placeVideoDetectionMapper.addPlaceVideoDetection(placeVideoDetection);

    }

    @Override
    public void updatePlaceVideoDetection(PlaceVideoDetection placeVideoDetection) {
        placeVideoDetectionMapper.updatePlaceVideoDetection(placeVideoDetection);

    }

    @Override
    public void delPlaceVideoDetection(String[] split) {
        for (String id : split) {
            placeVideoDetectionMapper.delPlaceVideoDetection(id);

        }
    }

    @Override
    public List<PlaceVideoDetection> getPlaceVideoDetectionById(Integer id) {
        return placeVideoDetectionMapper.getPlaceVideoDetectionById(id);
    }

    @Override
    public List<PlaceVideoDetection> getPlaceVideoDetectiontByMap(String map,String id,String update) {
        return placeVideoDetectionMapper.getPlaceVideoDetectiontByMap(map,id,update);

    }

    @Override
    public List<ParkingPlace> getAllPlaceByMap(String maps,String placeId) {
        return placeVideoDetectionMapper.getAllPlaceByMap(maps,placeId);
    }

    @Override
    public List<PlaceVideoDetection> getAllGuideScreenDeviceTest(String map) {
        return placeVideoDetectionMapper.getAllGuideScreenDeviceTest(map);
    }
    @Override
    public List<ParkingPlace> getAllPlaceByMapTest(String maps) {
        return placeVideoDetectionMapper.getAllPlaceByMapTest(maps);
    }

    @Override
    public ParkingPlace getPlaceByPlaceNamesTest(String mapId, String placeName) {
        return placeVideoDetectionMapper.getPlaceByPlaceNamesTest(mapId, placeName);
    }

    @Override
    public void updateLastUploadTime(String map) {
        RBucket<String> uploadTimeBucket = redissonClient.getBucket(LAST_UPLOAD_KEY+map);
        String currentTime = LocalDateTime.now().toString();
        Integer serviceStatusTime = placeVideoDetectionMapper.selectOne(new QueryWrapper<PlaceVideoDetection>().eq("map", map)).getServiceStatusTime();
        uploadTimeBucket.set(currentTime);
        uploadTimeBucket.expire(!NullUtils.isEmpty(serviceStatusTime)?serviceStatusTime:10, TimeUnit.MINUTES);
        placeVideoDetectionMapper.update(new UpdateWrapper<PlaceVideoDetection>().eq("map", map).set("status", 1));
        updateDeviceAlarm(map);
    }

    public void updateDeviceAlarm(String id) {
        deviceAlarmsService.lambdaUpdate()
                .set(DeviceAlarms::getState, 1)
                .set(DeviceAlarms::getEndTime, LocalDateTime.now())
                .eq(DeviceAlarms::getMap, id)
                .eq(DeviceAlarms::getDeviceId, id)
                .eq(DeviceAlarms::getNum, id)
                .eq(DeviceAlarms::getEquipmentType, 9)
                .eq(DeviceAlarms::getState, 0)
                .isNull(DeviceAlarms::getEndTime)
                .update();
    }

    @Override
    public Long getLastUploadTime() {
        RBucket<Long> uploadTimeBucket = redissonClient.getBucket(LAST_UPLOAD_KEY);
        return uploadTimeBucket.get();
    }

    @Override
    public void updateStatusToError(String map) {
        placeVideoDetectionMapper.update(new PlaceVideoDetection() {{setStatus(0);}},new LambdaUpdateWrapper<PlaceVideoDetection>().eq(PlaceVideoDetection::getMap, map));
        parkingService.updatePlaceTests(map,LocalDateTime.now());
    }
}
