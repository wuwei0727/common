package com.tgy.rtls.web.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.entity.view.UserVo;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.service.view.VariableOperationalDataService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.tgy.rtls.data.common.ParkingUtil.processRecords;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2022-09-14 13:37
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@CrossOrigin
@RequestMapping(value = "/view/")
public class ViewController {
    @Resource
    private VariableOperationalDataService variableOperationalDataService;

    @Resource
    private ViewMapper viewMapper;
    @Resource
    private AppletsWebSocket appletsWebSocket;
    @Resource
    private DeviceAlarmsService deviceAlarmsService;
    @RequestMapping(value = "/getDeviceAlarmsTypeConfigById")
    public CommonResult<Object> getDeviceAlarmsService(Integer id,Integer type) {
        List<DeviceAlarms> list = deviceAlarmsService.list(new QueryWrapper<DeviceAlarms>()
                .eq("equipment_type",type)
                .eq("state", 0)
                .eq("device_id", id)
                .isNull("end_time"));

        LambdaUpdateWrapper<DeviceAlarms> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.set(DeviceAlarms::getState,1)
                .set(DeviceAlarms::getEndTime, LocalDateTime.now())
                .eq(DeviceAlarms::getEquipmentType,type)
                .eq(DeviceAlarms::getState, 0)
                .eq(DeviceAlarms::getDeviceId, id)
                .isNull(DeviceAlarms::getEndTime);
        deviceAlarmsService.update(null,lambdaUpdateWrapper);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), list);
    }

    @RequestMapping(value = "/getWithinThreeMonthsNewUsers")
    @ApiOperation(value = "新增用户数")
    public CommonResult<Object> getWithinThreeMonthsNewUsers(String time, String mapId,String start, String end) {
        long userTotal;
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (start != null && end != null) {
            // 使用start和end参数
            userTotal = getUserTotal(mapId, null, start, end);
        } else {
            // 使用time参数
            userTotal = getUserTotal(mapId, time, null, null);
        }
        List<UserVo> viewVo2s = Stream.of(userTotal).map(UserVo::new).collect(Collectors.toList());
        return new CommonResult<>(200, LocalUtil.get("获取成功"),viewVo2s);
    }

    @ApiOperation(value = "用户检索前10停车场",notes = "NO")
    @RequestMapping(value = "/getTop10ParkingPlaces")
    public CommonResult<Object> getTop10ParkingPlaces() {
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getTop10ParkingPlaces());
    }

    @ApiOperation(value = "用户检索前10商家")
    @RequestMapping(value = "getTop10Business")
    public CommonResult<Object> getTop10Business(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getTop10Business(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getTop10Business2(time, mapId,start,end));
    }

    @ApiOperation(value = "车位使用次数总数")
    @RequestMapping(value = "getPlaceUseTotal")
    public CommonResult<Object> getPlaceUseTotal(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceUseTotal(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceUseTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "/车位空闲总时长")
    @RequestMapping(value = "getPlaceIdleTotalDuration")
    public CommonResult<Object> getPlaceIdleTotalDuration(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceIdleTotalDuration(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceIdleTotalDuration2(time, mapId,start,end));
    }

    @ApiOperation(value = "车位预约总数")
    @RequestMapping(value = "getReservationTotal")
    public CommonResult<Object> getReservationTotal(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getReservationTotal(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getReservationTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "平台车位利用率")
    @RequestMapping(value = "getPlatformPlaceUtilizationRate")
    public CommonResult<Object> getPlatformPlaceUtilizationRate(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlatformPlaceUtilizationRate(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlatformPlaceUtilizationRate2(time, mapId,start,end));
    }

    @ApiOperation(value = "停车场车位利用率")
    @RequestMapping(value = "getMapPlaceUtilizationRate")
    public CommonResult<Object> getMapPlaceUtilizationRate(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getMapPlaceUtilizationRate(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getMapPlaceUtilizationRate2(time, mapId,start,end));

    }

    @ApiOperation(value = "停车场车位空闲率")
    @RequestMapping(value = "getPlaceAvailabilityRate")
    public CommonResult<Object> getPlaceAvailabilityRate(String time, String mapId,String start,String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceAvailabilityRate(time,start,end));
        }
        List<ViewVo2> placeAvailabilityRate2 = viewMapper.getPlaceAvailabilityRate2(time, mapId,start,end);
        if(placeAvailabilityRate2.get(0)==null){
            return new CommonResult<>(200, LocalUtil.get("获取成功"), new ArrayList<>());
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), placeAvailabilityRate2);
    }

    @ApiOperation(value = "车位导航总数")
    @RequestMapping(value = "getPlaceNavigationTotal")
    public CommonResult<Object> getPlaceNavigationTotal(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
//        if (start != null&&end != null) {
//            start = start + "00:00:00";
//            end = end + " 23:59:59";
//        }

        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceNavigationTotal(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceNavigationTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "车位导航使用率")
    @RequestMapping(value = "getPlaceNavigationUseRate")
    public CommonResult<Object> getPlaceNavigationUseRate(String time, String mapId,String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getPlaceNavigationUseRate(time, start,end));
        }
        List<ViewVo2> placeNavigationUseRate2 = viewMapper.getPlaceNavigationUseRate2(time, mapId, start,end);
        if(placeNavigationUseRate2.get(0)==null){
            return new CommonResult<>(200, LocalUtil.get("获取成功"), new ArrayList<>());
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), placeNavigationUseRate2);
    }

    @ApiOperation(value = "每小时空车位数")
    @RequestMapping(value = "getPerHourNullPlaceNumber")
    public CommonResult<Object> getPerHourNullPlaceNumber(String mapId) {
        if(NullUtils.isEmpty(mapId)){
            return new CommonResult<>(200, LocalUtil.get("获取成功"),viewMapper.getPerHourNullPlaceNumber());
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"),viewMapper.getPerHourNullPlaceNumber2(mapId));
    }

    @ApiOperation(value = "位置分享总数")
    @RequestMapping(value = "getLocationShareTotal")
    public CommonResult<Object> getLocationShareTotal(String time, String mapId,String start,String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getLocationShareTotal(time,start,end));

        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getLocationShareTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "反向寻车总数")
    @RequestMapping(value = "getReverseCarSearchTotal")
    public CommonResult<Object> getReverseCarSearchTotal(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getReverseCarSearchTotal(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getReverseCarSearchTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "月活跃用户数")
    @RequestMapping(value = "getActiveUserNumber")
    public CommonResult<Object> getActiveUserNumber(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getAllUserTotalNumByMonth0(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getAllUserTotalNumByMonth2(time, mapId,start,end));

    }

    @ApiOperation(value = "用户搜索总数")
    @RequestMapping(value = "getUserSearchTotal")
    public CommonResult<Object> getUserSearchTotal(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getUserSearchTotal(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getUserSearchTotal2(time, mapId,start,end));
    }

    @ApiOperation(value = "空闲车位数",notes = "NO")
    @RequestMapping(value = "getIdlePlaceNumber")
    public CommonResult<Object> getIdlePlaceNumber(@RequestParam(defaultValue = "1") String time) {
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getIdlePlaceNumber(time));
    }

    @ApiOperation(value = "地图用户总数")
    @RequestMapping(value = "getMapUsersTotal")
    public CommonResult<Object> getMapUsersTotal(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getMapUsersTotal(time, mapId,start,end));
    }

    @ApiOperation(value = "多地图用户总数")
    @RequestMapping(value = "getManyMapUsersTotal")
    public CommonResult<Object> getManyMapUsersTotal(String time, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getAllUserInfo(time,start,end));
    }


    @ApiOperation(value = "访问总次数")
    @RequestMapping(value = "getCumulativeUseFrequency")
    public CommonResult<Object> getCumulativeUseFrequency(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getCumulativeUseFrequency2(time, mapId,start,end));
    }

    @ApiOperation(value = "多地图访问总次数")
    @RequestMapping(value = "getManyMapCumulativeUseFrequency")
    public CommonResult<Object> getManyMapCumulativeUseFrequency(String time, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getCumulativeUseFrequency(time,start,end));
    }

    @ApiOperation(value = "设备统计")
    @RequestMapping(value = "getDeviceCount")
    public CommonResult<Object> getDeviceCount(@RequestParam(defaultValue = "1") String time, String mapId) {
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getCarBitAndHardwareInfo2(time, mapId));
    }

    @ApiOperation(value = "活跃用户数")
    @RequestMapping(value = "getActiveUserNumber2")
    public CommonResult<Object> getActiveUserNumber2(String time, String mapId, String start, String end) {
        if (time != null && (start != null || end != null)) {
            return new CommonResult<>(400, LocalUtil.get("参数错误：统计时间区间 和 (开始时间至结束时间) 不能同时使用"));
        }
        if (NullUtils.isEmpty(mapId)) {
            return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getManyMapActiveUserNumber(time,start,end));
        }
        return new CommonResult<>(200, LocalUtil.get("获取成功"), viewMapper.getActiveUserNumber(time, mapId,start,end));
    }

    @RequestMapping(value = "getPlaceUseRecords")
    public Map<String, Integer> getPlaceUseRecords(String map, int t) throws Exception {
        List<PlaceUseRecord> placeUseRecords = this.viewMapper.getPlaceUseRecords(map);
        Integer placeTotal = this.viewMapper.getPlaceTotal(map);
        System.out.println("processRecords(placeUseRecords, t); = " + processRecords(placeUseRecords, t));
//        System.out.println("processRecords2(placeUseRecords, t); = " + processRecords2(placeUseRecords, t));
//        System.out.println("processRecords3(placeUseRecords, t); = " + processRecords3(placeUseRecords, t));
//        System.out.println("processRecords5(placeUseRecords, t); = " + processRecords5(placeUseRecords, t));

//        log.error("getPlaceUseRecords → map={}", processedRecords);
        return processRecords(placeUseRecords, t);

    }



    @RequestMapping(value = "getUserTotal")
    public Long getUserTotal(String map, String time, String start, String end) {
        List<UserVo> userTotalList = this.viewMapper.getUserTotal(map, null);
        return variableOperationalDataService.countNewUsers(userTotalList, map, time,start,end);
    }

}
