package com.tgy.rtls.web.controller.park.floorLock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.floorLock.*;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.vip.FloorLockMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.floorLock.impl.CarPlateService;
import com.tgy.rtls.data.service.park.floorLock.impl.PlaceUnlockRecordsService;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
import com.tgy.rtls.data.service.sms.VerificationCodeService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park.floorLock
 * @Author: wuwei
 * @CreateTime: 2024-07-18 14:21
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("lockDevice/")
public class LockDeviceController {
    @Autowired
    private PlaceUnlockRecordsService placeUnlockRecordsService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ParkMapper parkMapper;
    @Autowired
    private UserCompanyMapService userCompanyMapService;
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private TimePeriodAdminService timePeriodAdminService;
    @Autowired
    private CarPlateService carPlateService;
    @Autowired
    private VerificationCodeService verificationCodeService;
    @Autowired
    private FloorLockMapper floorLockMapper;



    @PostMapping("addPlaceUnlockRecords")
    public CommonResult<Object> addPlaceUnlockRecords(@RequestBody PlaceUnlockRecords placeUnlockRecords, HttpServletRequest request) {
        try {
            Map_2d map2d = parkMapper.getCurrentInfraredMapName(placeUnlockRecords.getPlaceId(), Math.toIntExact(placeUnlockRecords.getMapId()));
            if(!NullUtils.isEmpty(map2d)&&(!NullUtils.isEmpty(map2d.getName())||!NullUtils.isEmpty(map2d.getPlaceName())||!NullUtils.isEmpty(map2d.getCompanyName()))){
                placeUnlockRecords.setMapName(map2d.getName());
                placeUnlockRecords.setCompanyName(map2d.getCompanyName());
                placeUnlockRecords.setPlaceName(map2d.getPlaceName());
            }
            placeUnlockRecords.setIsExclusiveUser(1);
            placeUnlockRecords.setUnlockTime(LocalDateTime.now());
            placeUnlockRecordsService.save(placeUnlockRecords);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("check")
    public CommonResult<Object> check(@RequestBody RequestDTO requestDTO) {
        try {
            log.error("authenticateAndUnlock → requestDTO={}", requestDTO);
            if(!NullUtils.isEmpty(requestDTO.getCompanyId())) {
                UserCompanyMap user = userCompanyMapService.getOne(new QueryWrapper<UserCompanyMap>()
                        .eq("map_id", requestDTO.getMapId())
                        .eq("company_id", requestDTO.getCompanyId())
                        .eq("user_id", requestDTO.getUserId()));
                if(NullUtils.isEmpty(user)){
                    long cpCount = parkMapper.getPlaceCountByCompany(requestDTO.getMapId(), requestDTO.getCompanyId());
                    long ucCount = userCompanyMapService.count(new QueryWrapper<UserCompanyMap>()
                            .eq("map_id", requestDTO.getMapId())
                            .eq("company_id", requestDTO.getCompanyId()));
                    if(ucCount>=cpCount){
                        LocalDate today = LocalDate.now();
                        int dayOfWeek = today.getDayOfWeek().getValue();
                        List<TimePeriodAdmin> timePeriods = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>()
                                .eq("company_id", requestDTO.getCompanyId())
                                .eq("day_of_week",dayOfWeek));
                        // 获取当前时间
                        LocalTime now = LocalTime.now();
                        boolean isWithinExclusiveTimePeriod = false;
                        for (TimePeriodAdmin timePeriod : timePeriods) {
                            if (now.isAfter(timePeriod.getStartTime()) && now.isBefore(timePeriod.getEndTime())) {
                                isWithinExclusiveTimePeriod = true;
                                break;
                            }
                        }
                        if (isWithinExclusiveTimePeriod) {
                            return new CommonResult<>(200,"当前时间不在开放时段内，请稍后再试。",false);
                        }
                        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),true);
                    }
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),true);
                }
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),true);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("authenticateAndUnlock")
    public CommonResult<Object> authenticateAndUnlock(@RequestBody RequestDTO requestDTO) {
        try {
            log.error("authenticateAndUnlock → requestDTO={}", requestDTO);

            if (NullUtils.isEmpty(requestDTO.getPhone())) {
                return new CommonResult<>(310, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }
            if(!verificationCodeService.verifyVerificationCode(requestDTO.getPhone(), requestDTO.getCode())){
                return new CommonResult<>(310, LocalUtil.get(KafukaTopics.VCODE_ERROR));
            }
            if(!NullUtils.isEmpty(requestDTO.getCompanyId())){
                CarPlate carPlate = carPlateService.getOne(
                        new QueryWrapper<CarPlate>().eq(!NullUtils.isEmpty(requestDTO.getPhone()), "phone_number", requestDTO.getPhone())
                                .eq(!NullUtils.isEmpty(requestDTO.getMapId()), "map_id", requestDTO.getMapId())
                                .eq(!NullUtils.isEmpty(requestDTO.getCompanyId()), "company_id", requestDTO.getCompanyId())
                );

                if(!NullUtils.isEmpty(carPlate)){
                    long cpCount = parkMapper.getPlaceCountByCompany(requestDTO.getMapId(), requestDTO.getCompanyId());
                    long ucCount = userCompanyMapService.count(new QueryWrapper<UserCompanyMap>()
                            .eq("map_id", requestDTO.getMapId())
                            .eq("user_id", requestDTO.getUserId()));
                    if(ucCount<=cpCount){
                        UserCompanyMap userCompanyMap = new UserCompanyMap();
                        Map_2d map2d = parkMapper.getCurrentInfraredMapName(null, Math.toIntExact(requestDTO.getMapId()));
                        UserCompanyMap user = userCompanyMapService.getOne(new QueryWrapper<UserCompanyMap>()
                                .eq("map_id", requestDTO.getMapId())
                                .eq("user_id", requestDTO.getUserId()));
                        if(!NullUtils.isEmpty(user)){
                            userCompanyMap.setId(user.getId());
                        }

                        userCompanyMap.setUserId(requestDTO.getUserId());
                        userCompanyMap.setPhone(requestDTO.getPhone());
                        userCompanyMap.setLicensePlateId(carPlate.getId());
                        userCompanyMap.setLicensePlate(requestDTO.getLicensePlate());
                        userCompanyMap.setMapId(requestDTO.getMapId());
                        userCompanyMap.setMapName(map2d.getName());
                        userCompanyMap.setCompanyId(carPlate.getCompanyId());
                        userCompanyMap.setCompanyName(carPlate.getCompanyName());
                        if(userCompanyMapService.saveOrUpdate(userCompanyMap)){
                            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),1);
                        }
                    }
                    return new CommonResult<>(310,"当前公司白名单用户超限,请联系管理员！！！");
                }
            }


//            recordUnlockInfo(requestDTO);

            LocalDate today = LocalDate.now();
            int dayOfWeek = today.getDayOfWeek().getValue();
            List<TimePeriodAdmin> timePeriods = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>()
                    .eq("company_id", requestDTO.getCompanyId())
                    .eq("day_of_week",dayOfWeek));
            // 获取当前时间
            LocalTime now = LocalTime.now();
            boolean isWithinExclusiveTimePeriod = false;
            for (TimePeriodAdmin timePeriod : timePeriods) {
                if (now.isAfter(timePeriod.getStartTime()) && now.isBefore(timePeriod.getEndTime())) {
                    isWithinExclusiveTimePeriod = true;
                    break;
                }
            }
            if (isWithinExclusiveTimePeriod) {
                return new CommonResult<>(310,"当前时间不在开放时段内，请稍后再试。");
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),0);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getTimePeriodInfo")
    public CommonResult<Object> getTimePeriodInfo(@RequestParam Long mapId,@RequestParam Long companyId) {
        List<TimePeriodAdmin> timePeriodAdminList= timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>()
                .eq(!NullUtils.isEmpty(mapId),"mapId",mapId)
                .eq(!NullUtils.isEmpty(companyId),"company_id",companyId));
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), timePeriodAdminList);
    }

    @GetMapping("/isExclusiveUser")
    public CommonResult<Object> isExclusiveUser(@RequestParam Long userId, @RequestParam Long mapId) {
        UserCompanyMap company= userCompanyMapService.getOne(new QueryWrapper<UserCompanyMap>()
                .select("user_id,license_plate,map_id,company_id")
                .eq(!NullUtils.isEmpty(mapId),"map_id",mapId)
                .eq(!NullUtils.isEmpty(userId),"user_id",userId));
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), company);
    }

    @GetMapping("getCompanyPlace")
    public CommonResult<Object> getCompanyPlace(RequestDTO requestDTO) {
        try {
            List<Integer> placesId = parkMapper.getPlaceById2(requestDTO.getMapId(), requestDTO.getCompanyId());
            if(NullUtils.isEmpty(placesId)){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            }
            List<FloorLock> locks = floorLockMapper.getFloorLocksByMapAndPlaceIds(requestDTO.getMapId(), placesId);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),locks);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getTimePeriodAdminInfoById")
    public CommonResult<Object> getTimePeriodAdminInfoById(Integer id,Integer num) {
        try {
            if(!NullUtils.isEmpty(id)){
                TimePeriodAdmin timePeriodAdmin = timePeriodAdminService.getTimePeriodAdminInfoById(id);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),timePeriodAdmin);
            }
            List<TimePeriodAdmin> data = timePeriodAdminService.getTimePeriodAdminInfo(null, null, null, null,num,null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),data);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    private void recordUnlockInfo(RequestDTO requestDTO) {
        // 实现记录开锁信息的逻辑
        PlaceUnlockRecords placeUnlockRecords = new PlaceUnlockRecords();
        placeUnlockRecords.setLicensePlate(requestDTO.getLicensePlate());
        placeUnlockRecords.setPhone(requestDTO.getPhone());
        placeUnlockRecords.setUserId(requestDTO.getUserId());
        placeUnlockRecords.setMapId(requestDTO.getMapId());
        placeUnlockRecords.setMapName(requestDTO.getMapName());
        placeUnlockRecords.setCompanyId(requestDTO.getCompanyId());
        placeUnlockRecords.setCompanyName(requestDTO.getCompanyName());
        placeUnlockRecordsService.save(placeUnlockRecords);
    }
}
