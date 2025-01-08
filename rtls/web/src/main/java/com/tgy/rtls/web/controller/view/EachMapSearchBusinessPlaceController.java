package com.tgy.rtls.web.controller.view;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.view.*;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.park.RecommConfigService;
import com.tgy.rtls.data.service.promoter.PromoterQrCodeService;
import com.tgy.rtls.data.service.view.*;
import com.tgy.rtls.web.aspect.MyPermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/3/7 - 15:29
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/emsbp")
public class EachMapSearchBusinessPlaceController {
    @Autowired
    private EachMapSearchBusinessService businessService;
    @Autowired
    private EachMapSearchPlaceService placeService;
    @Autowired
    private LocationSharingLogService sharingLogService;
    @Autowired
    private UserSearchLogService userSearchLogService;
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private UserActiveSelectPlaceService userActiveSelectPlaceService;
    @Autowired
    private PromoterQrCodeService promoterQrCodeService;
    @Autowired
    private RecommConfigService recommConfigService;

    @RequestMapping(value = "/addUserActiveSelectPlace")
    public CommonResult<Object> addUserActiveSelectPlace(@RequestBody NaviLogVo naviLogVo) {
        try {
            UserActiveSelectPlace place = new UserActiveSelectPlace();;
            List<ParkingPlace> placeNames = recommConfigService.getPlaceByPlaceNames(Integer.valueOf(naviLogVo.getMap()), naviLogVo.getPlaceName());
            if("1".equals(naviLogVo.getType())){
                if(!NullUtils.isEmpty(placeNames)){
                    place.setPlace(String.valueOf(placeNames.get(0).getId()));
                }
                place = place.setMap(naviLogVo.getMap())
                    .setMapName(naviLogVo.getMapName())
                    .setPlaceName(naviLogVo.getPlaceName())
                    .setDesc("用户选择车位导航");
            }else if("2".equals(naviLogVo.getType())){
                place = place.setMap(naviLogVo.getMap())
                        .setMapName(naviLogVo.getMapName())
                        .setUserId(naviLogVo.getUserId())
                        .setDestId(naviLogVo.getDestId())
                        .setShangJiaName(naviLogVo.getShangJiaName())
                        .setDesc("商家推广导航");
            }
            userActiveSelectPlaceService.save(place);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addEachMapSearchBusinessPlace")
    public CommonResult<Object> addEachMapSearchBusinessPlace(@RequestBody EachMapSearchVO eachMapSearchVO) {
        try {
            if("1".equals(eachMapSearchVO.getType())){
                EachMapSearchPlace place = new EachMapSearchPlace()
                        .setMap(eachMapSearchVO.getMap())
                        .setMapName(eachMapSearchVO.getMapName())
                        .setPlace(eachMapSearchVO.getPlace())
                        .setPlaceName(eachMapSearchVO.getPlaceName());
                placeService.insert(place);
            }else if("2".equals(eachMapSearchVO.getType())){
                EachMapSearchBusiness business = new EachMapSearchBusiness()
                        .setMap(eachMapSearchVO.getMap())
                        .setMapName(eachMapSearchVO.getMapName())
                        .setBusinessId(eachMapSearchVO.getBusinessId())
                        .setBusinessName(eachMapSearchVO.getBusinessName());
                businessService.insert(business);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addLocationSharingLog")
    public CommonResult<Object> addLocationSharingLog(@RequestBody LocationSharingLog locationSharingLog) {
        try {
            LocationSharingLog log = null;
            if(!NullUtils.isEmpty(locationSharingLog.getMap())){
                LambdaQueryWrapper<LocationSharingLog> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(LocationSharingLog::getMap,locationSharingLog.getMap());
                log = sharingLogService.getOne(queryWrapper);
            }else {
                LambdaQueryWrapper<LocationSharingLog> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(LocationSharingLog::getMap,locationSharingLog.getMap());
                log = sharingLogService.getOne(queryWrapper);
            }
            LocationSharingLog sharingLog = new LocationSharingLog()
                    .setMap(locationSharingLog.getMap())
                    .setMapName(locationSharingLog.getMapName())
                    .setCount(!NullUtils.isEmpty(log)?log.getCount()+1:locationSharingLog.getCount());
            if(!NullUtils.isEmpty(log)){
                sharingLog.setId(log.getId());
            }
            if(sharingLogService.saveOrUpdate(sharingLog)){
                userSearchLogVo userSearchLogVo = new userSearchLogVo()
                        .setLslogid(sharingLog.getId());
                sharingLogService.insertLocationLog(userSearchLogVo);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addUserSearchTotal")
    public CommonResult<Object> addUserSearchTotal(@RequestBody UserSearchLog userSearchLog) {
        try {
            UserSearchLog log = null;
            if(!NullUtils.isEmpty(userSearchLog.getMap())){
                LambdaQueryWrapper<UserSearchLog> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserSearchLog::getMap,userSearchLog.getMap());
                log = userSearchLogService.getOne(queryWrapper);
            }else {
                LambdaQueryWrapper<UserSearchLog> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(UserSearchLog::getMap,userSearchLog.getMap());
                log = userSearchLogService.getOne(queryWrapper);
            }
            UserSearchLog sharingLog = new UserSearchLog()
                    .setUserId(userSearchLog.getUserId())
                    .setMap(userSearchLog.getMap())
                    .setMapName(userSearchLog.getMapName())
                    .setCount(!NullUtils.isEmpty(log)?log.getCount()+1:userSearchLog.getCount());
            if(!NullUtils.isEmpty(log)){
                sharingLog.setId(log.getId());
            }
            if(userSearchLogService.saveOrUpdate(sharingLog)){
                userSearchLogVo userSearchLogVo = new userSearchLogVo()
                        .setUslogid(sharingLog.getId());
                userSearchLogService.insertUserSearchLog(userSearchLogVo);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @MyPermission
    @RequestMapping(value = "/getMapName")
    public CommonResult<Object> getMapName(String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),map2dService.getMapName(mapids));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("selectOne")
    public CommonResult<Object> selectOne(Integer id) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),promoterQrCodeService.selectByPrimaryKey(id));
    }


}
