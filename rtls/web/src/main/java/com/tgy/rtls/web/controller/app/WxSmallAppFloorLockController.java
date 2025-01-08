package com.tgy.rtls.web.controller.app;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import com.tgy.rtls.data.service.user.SysUserService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import com.tgy.rtls.web.util.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-07-25 15:51
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/wxSmallAPP")
@Api(value = "微信小程序登录")
@Slf4j
public class WxSmallAppFloorLockController extends BasesController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private FloorLockService floorLockService;
    @Autowired
    private TimePeriodAdminService timePeriodAdminService;
    @Autowired
    private AppletsWebSocket appletsWebSocket;
    @Autowired
    private ParkingService parkingService;
    @Autowired(required = false)
    private ParkMapper parkMapper;

    @RequestMapping("/timePeriodConfig")
    @ResponseBody
    public CommonResult<Object> timePeriodConfig(TimePeriodAdmin timePeriodAdmin) {
        Session session = SecurityUtils.getSubject().getSession();
        String sessionId = (String) session.getAttribute(Constant.USER_WXJSESSION_ID);
        logger.info("获取会话ID：" + sessionId);
        if (sessionId == null) {
            return new CommonResult<>(401, LocalUtil.get("登录过期,请重新登录"));
        }
        return new CommonResult<>(200, LocalUtil.get("校验成功"));
    }

    @PostMapping("addTimePeriodConfig")
    public CommonResult<Object> addTimePeriodConfig(@RequestBody List<TimePeriodAdmin> timePeriodAdmins, HttpServletRequest request) {
        try {
            Integer mapId = timePeriodAdmins.get(0).getMapId();
            String mapName = timePeriodAdmins.get(0).getMapName();
            List<TimePeriodAdmin> existingRecords = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId", mapId));
            if(existingRecords.size() + timePeriodAdmins.size() > 7){
                return new CommonResult<>(200, LocalUtil.get(String.format("无法为 %s 添加超过 7 个时间段",mapName)));
            }
            if(timePeriodAdminService.saveOrUpdateBatch(timePeriodAdmins)){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.t_p)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
//    -289
//            58
    @GetMapping("/getTimePeriodAdminInfoById/{mapId}")
    public CommonResult<Object> getTimePeriodAdminInfoById(@PathVariable("mapId") Integer mapId) {
        try {
            List<TimePeriodAdmin> timePeriods = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId",mapId));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),timePeriods);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


     @PostMapping(value = "/addFloorLock")
     @ApiOperation(value = "添加信标位置", notes = "111")
     public CommonResult<Object> addFloorLock(@RequestBody Map<Object,Object> params, HttpServletRequest request) {
         Date date = new Date();
         SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
         LocalDateTime now = LocalDateTime.now();
         List<DeviceInfoVO> beaconList = JSONArray.parseArray(JSONObject.toJSONString(params.get("params")), DeviceInfoVO.class);
         CommonResult<Object> res = null;
         CommonResult<Object> res2 = null;
         List<Object> arrayList = new ArrayList<>();
         try {
             if(NullUtils.isEmpty(beaconList)){
                 return new CommonResult<>(400,LocalUtil.get(KafukaTopics.PLEASE_ENTER_THE_DEVICE_INFORMATION_YOU_WANT_TO_BIND));
             }
             for (DeviceInfoVO beacon : beaconList) {
                 Map<String, Object> dataMap = new HashMap<>();
                 List<Object> duplicateDataList = new ArrayList<>();
                 res = new CommonResult<>(200, "");
                 res2 = new CommonResult<>();

                 List<ParkingPlace> places = parkingService.findPlaceByMapAndName(beacon.getMap(), beacon.getPlaceName(), null);
                 if (places == null || places.size() == 0 || places.size()>1 &&!beacon.getPlaceName().equals(places.get(0).getName())) {
                     res2.setMessage("未查询到该车位信息或车位重复请确认");
                     res2.setCode(400);
                     dataMap.put("placeName", beacon.getPlaceName());
                     dataMap.put("mac", beacon.getMac());
                     dataMap.put("map", beacon.getMap());
                     res2.setData(dataMap);
                     arrayList.add(res2);
                     res.setData(arrayList);
                     continue;
                 }

                 Map_2d map2d = map2dService.findByfmapID(String.valueOf(beacon.getMap()));
                 Integer mapid = null;
                 if (map2d != null) {
                     mapid = map2d.getId();
                 }

                 List<FloorLock> subCurrentMapName = floorLockService.getConditionData(beacon.getMac(), null, null, Long.valueOf(beacon.getMap()));
                 if(!NullUtils.isEmpty(subCurrentMapName)) {
                     for (FloorLock floorLock : subCurrentMapName) {
                         DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
                         deviceInfoVO.setId(Math.toIntExact(floorLock.getId()));
                         deviceInfoVO.setPlaceId(floorLock.getPlace());
                         deviceInfoVO.setMap(Math.toIntExact(floorLock.getMap()));
                         deviceInfoVO.setFid(floorLock.getFid());
                         deviceInfoVO.setPlaceName(floorLock.getParkingName());
                         deviceInfoVO.setX(Double.valueOf(places.get(0).getX()));
                         deviceInfoVO.setY(Double.valueOf(places.get(0).getY()));
                         deviceInfoVO.setFloor(Short.valueOf(places.get(0).getFloor()));
                         deviceInfoVO.setMac(floorLock.getDeviceNum());
                         duplicateDataList.add(deviceInfoVO);
                     }
                 }

                 dataMap.put("placeName", places);
                 dataMap.put("decimalNum", beacon.getMac());
                 List<FloorLock> floorLock1 = floorLockService.getConditionData(beacon.getMac(), places.get(0).getId(), null, null);
                 if(!NullUtils.isEmpty(floorLock1)){
                     if (floorLock1.get(0).getDeviceNum().equals(beacon.getMac())) {
                         res2.setMessage(beacon.getMac() + "该地锁已经被" + floorLock1.get(0).getMapName() + "绑定!");
                         res2.setCode(502);
                         dataMap.put("offlineData", beacon);
                         dataMap.put("duplicateData", duplicateDataList);
                         res2.setData(dataMap);
                         arrayList.add(res2);
                         res.setData(arrayList);
                         continue;
                     }
                     if (floorLock1.get(0).getParkingName().equals(places.get(0).getName())) {
                         res2.setMessage(beacon.getPlaceName() + "该车位已经被" +  floorLock1.get(0).getMapName() +"编号为"+floorLock1.get(0).getDeviceNum()+ "地锁绑定！");
                         res2.setCode(502);
                         dataMap.put("offlineData", beacon);
                         dataMap.put("duplicateData", duplicateDataList);
                         res2.setData(dataMap);
                         arrayList.add(res2);
                         res.setData(arrayList);
                         continue;
                     }
                 }

                 if (subCurrentMapName.size()==0&&NullUtils.isEmpty(subCurrentMapName)) {
                     FloorLock floorLock = new FloorLock();
                     floorLock.setDeviceNum(beacon.getMac());
                     floorLock.setPlace(places.get(0).getId());
                     floorLock.setFloor(places.get(0).getFloor());
                     floorLock.setX(places.get(0).getX());
                     floorLock.setY(places.get(0).getY());
                     floorLock.setFid(places.get(0).getFid());
                     floorLock.setParkingName(beacon.getPlaceName().toUpperCase());
                     floorLock.setMap(Long.valueOf(String.valueOf(mapid)));
                     if (floorLockService.addFloorLockInfo(floorLock)) {
                         res2.setCode(200);
                         res2.setMessage(LocalUtil.get(KafukaTopics.ADD_SUCCESS));
                         dataMap.put("floorLock",floorLock);
                         res2.setData(dataMap);
                         arrayList.add(res2);
                         res.setData(arrayList);

                         String ip = IpUtil.getIpAddr(request);
                         String address = ip2regionSearcher.getAddressAndIsp(ip);
                         operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("小程序添加地锁"+mapid+":"+ floorLock.getId()), now);
                         continue;
                     }
                 } else {
                     FloorLock floorLock = subCurrentMapName.get(0);
                     if (!NullUtils.isEmpty(floorLock) && !NullUtils.isEmpty(floorLock.getPlace())) {
                         if (!beacon.getChangeDevice()) {
                             List<ParkingPlace> oldPlcaes = parkingService.findByAllPlace(floorLock.getPlace(), null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                             ParkingPlace place = (oldPlcaes.size() == 0 ? null : oldPlcaes.get(0));
                             if (place != null) {
                                 place.setState((short) 1);
                                 place.setAddTime(date);
                                 parkMapper.updatePlace(place);
                             }
                         } else if (!NullUtils.isEmpty(subCurrentMapName)) {
                             if (beacon.getChangeDevice()) {
                                 List<ParkingPlace> oldPlcaes = parkMapper.getPlaceById(floorLock.getPlace());
                                 ParkingPlace place = (oldPlcaes.size() == 0 ? null : oldPlcaes.get(0));
                                 if (place != null) {
                                     place.setState((short) 1);
                                     place.setAddTime(date);
                                     parkMapper.updatePlace(place);
                                 }
                             }
                         } else if (!NullUtils.isEmpty(subCurrentMapName)) {
                             res2.setMessage(beacon.getMac() + "该地锁已经被" + floorLock.getMapName() + "车位名为" + (floorLock.getParkingName()!=null ? floorLock.getParkingName():null) + "的车位绑定！");
                             res2.setCode(502);
                             dataMap.put("offlineData", beacon);
                             dataMap.put("duplicateData", duplicateDataList);
                             res2.setData(dataMap);
                             arrayList.add(res2);
                             res.setData(arrayList);
                             continue;
                         }
                     }
                     floorLock.setId(subCurrentMapName.get(0).getId());
                     floorLock.setDeviceNum(beacon.getMac());
                     floorLock.setPlace(places.get(0).getId());
                     floorLock.setParkingName(beacon.getPlaceName().toUpperCase());
                     floorLock.setMap(Long.valueOf(String.valueOf(mapid)));
                     floorLockService.updateById(floorLock);
                     dataMap.put("floorLock",floorLock);
                     res2.setCode(200);
                     res2.setMessage(LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                     dataMap.put("code", 200);
                     dataMap.put("message", res2.getMessage());
                     res2.setData(dataMap);
                     arrayList.add(res2);
                     res.setData(arrayList);

                     String ip = IpUtil.getIpAddr(request);
                     String address = ip2regionSearcher.getAddressAndIsp(ip);
                     operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat("小程序更新地锁"+mapid+":"+ floorLock.getId()), now);
                     continue;
                 }

             }
         } catch (Exception e) {
             e.printStackTrace();
             return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
         }
         return res;
     }

    @GetMapping("/getFloorLockById/{mapId}")
    public CommonResult<Object> getFloorLockById(@PathVariable("mapId") Integer mapId) {
        try {
            List<FloorLock> list = floorLockService.getFloorLockInfo(Long.valueOf(mapId),null,null,null,null,null,null,null, null, null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),list);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delFloorLockById/{ids}")
    public CommonResult<Object> delFloorLockById(@PathVariable("ids") String ids) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS),floorLockService.removeBatchByIds(StrUtils.convertStringToList(ids)));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
