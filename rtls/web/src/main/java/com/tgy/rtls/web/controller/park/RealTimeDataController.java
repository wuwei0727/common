package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.PlaceChargeRecordData;
import com.tgy.rtls.data.entity.park.PlaceUseRecordData;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkingRecordMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.park.ParkingService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/park")
public class RealTimeDataController {
    @Autowired
    private ParkingService parkingService;
    @Autowired(required = false)
    private BookMapper bookMapper;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private ParkingRecordMapper parkingRecordMapper;

    @RequestMapping(value = "/getPlaceUseData")
    @ApiOperation(value = "获取车位使用数据", notes = "111")
    public CommonResult<Object> getPlaceUseData(Integer map, Integer day, String content) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List data = parkingService.getPlaceUseRecord(map, day, content);
            res.setData(data);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPlaceChargeData")
    @ApiOperation(value = "获取充电数据", notes = "111")
    public CommonResult<Object> getPlaceChargeData(Integer map, Integer day, String content) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List data = parkingService.getPlaceChargeRecord(map, day, content);
            res.setData(data);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/queryPlaceUseData")
    @ApiOperation(value = "获取车位使用数据", notes = "111")
    public CommonResult<Object> queryPlaceUseData(String start, String end, Integer map, String content) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (start != null && end != null) {
                end = end + " 23:59:59";
                start = start + " 00:00:00";
                List<PlaceUseRecordData> data = parkingRecordMapper.findPlaceUserRecordByTime(map, start, end, content);
                res.setData(data);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/queryPlaceChargeData")
    @ApiOperation(value = "获取充电数据", notes = "111")
    public CommonResult<Object> getPlaceChargeData(String start, String end, Integer map, String content) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<PlaceChargeRecordData> data = parkingRecordMapper.findPlaceChargeRecordByTime(map, start, end, content);
            res.setData(data);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPlaceMapFeeAndFlow")
    @ApiOperation(value = "获取充电数据", notes = "111")
    public CommonResult<Object> getPlaceMapFeeAndFlow(Integer map, Integer day, String content) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<Object> data = parkingService.getPlaceMapFeeAndFlow(map, day, content);
            res.setData(data);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


}
