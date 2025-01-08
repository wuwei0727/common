package com.tgy.rtls.web.controller.app;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.equip.TagScanService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-08-15 16:27
 * @Description: TODO
 * @Version: 1.0
 */
@RequestMapping(value = "/wxInfrared")
@CrossOrigin
@RestController
@Api(value = "微信车位检测器接口")
@Slf4j
public class WxSmallAppInfraredController {
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Autowired
    private LocalUtil localUtil;
    @Autowired
    private TagScanService tagScanService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private AppletsWebSocket appletsWebSocket;
    @Autowired
    private ViewMapper viewMapper;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;


    @RequestMapping(value = "/getInfraredSel")
    @ApiOperation(value = "车位检测器查询接口", notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "num", value = "卡号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "networkstate", value = "网络状态", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "relevance", value = "是否绑定地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "map", value = "地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getInfraredSel(String num, Integer networkstate, Integer map, String name, Integer relevance,
                                               @RequestParam(value = "desc", defaultValue = "addTime desc") String desc,
                                               @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                               @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            String uid = "";
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(sysUser)) {
                uid = String.valueOf(sysUser.getUserId());
            }
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize < 0) {
                List<Infrared> infrareds = tagMapper.findByAllInfrared(num, networkstate, relevance, map, desc, localUtil.getLocale(), name);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), infrareds);
            }

            PageHelper.startPage(pageIndex, pageSize);
            List<Infrared> infrareds = tagMapper.findByAllInfrared(num, networkstate, relevance, map, desc, localUtil.getLocale(), name);
            PageInfo<Infrared> pageInfo = new PageInfo<>(infrareds);
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("list", pageInfo.getList());
            resMap.put("pageIndex", pageIndex);
            resMap.put("total", pageInfo.getTotal());
            resMap.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), resMap);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateInfrared")
    @ApiOperation(value = "更新车位检测器", notes = "111")
    public CommonResult<Object> updateInfrared(DeviceInfoVO infrared, HttpServletRequest request) {
        CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        try {
            LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(sysUser)) {
                uid = String.valueOf(sysUser.getUserId());
            }

            String rawProductId = infrared.getMac().substring(infrared.getMac().length() - 6);
            Integer decimalNum = Integer.parseInt(rawProductId, 16);
            List<Infrared> infrared1 = tagMapper.findInfraredByNum(String.valueOf(decimalNum));
            infrared.setNum(String.valueOf(decimalNum));
            infrared.setRawProductId(infrared.getMac());
            infrared.setUpdateTime(new Date());
            if (tagScanService.updateInfrared(infrared)) {
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), "换绑".concat("小程序检测器"+infrared.getMap()), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS), infrared);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return res;
    }
//这是一个main方法，程序的入口
public static void main(String[] args){
    System.out.println("Integer.parseInt(\"0x17D8\", 16) = " + Integer.parseInt("0x17D8", 16));
}
//    /**mac-map-placeName-fid-infraredOfflineData*/
//    /**
//     * 重复数据返回id-map-x-y-z-floor-fid-placeName-mac
//     **/
    /**
     * @param id
     * @param num
     * @param map
     * @param x
     * @param y
     * @param floor
     * @param fid
     * @param fMapId
     * @param placeName
     * @param mac
     * @return
     */
    @RequestMapping(value = "/bindInfraredtoPlace")
    @ApiOperation(value = "绑定车位检测器到指定车位", notes = "111")
    public CommonResult<Object> bindPlaces(@RequestBody Map<Object, Object> params,HttpServletRequest request) {
        try {
            Date date = new Date();
            LocalDateTime now = LocalDateTime.now();
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            List<Object> arrayList = new ArrayList<>();

            List<DeviceInfoVO> infrareds = JSONArray.parseArray(JSONObject.toJSONString(params.get("params")), DeviceInfoVO.class);
            CommonResult<Object> res = null;
            CommonResult<Object> res2;

            if (NullUtils.isEmpty(infrareds)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PLEASE_ENTER_THE_DEVICE_INFORMATION_YOU_WANT_TO_BIND));
            }
            for (DeviceInfoVO infrared : infrareds) {
                Map<String, Object> dataMap = new HashMap<>();
                List<Object> duplicateDataList = new ArrayList<>();
                res = new CommonResult<>(200, "");
                res2 = new CommonResult<>();
                List<ParkingPlace> places = parkingService.findPlaceByMapAndName(infrared.getMap(), infrared.getPlaceName(), null);
                if (places == null || places.size() == 0 || !infrared.getPlaceName().equals(places.get(0).getName())) {
                    res2.setMessage("未查询到该车位信息");
                    res2.setCode(400);
                    dataMap.put("placeName", infrared.getPlaceName());
                    dataMap.put("mac", infrared.getMac());
                    dataMap.put("map", infrared.getMap());
                    res2.setData(dataMap);
                    arrayList.add(res2);
                    res.setData(arrayList);
                    continue;
                    //return res;
                }
                if (places.size() > 1) {
                    res2.setMessage("该地图存在同名车位,请输入有效fid");
                    res2.setCode(400);
                    dataMap.put("placeName", infrared.getPlaceName());
                    dataMap.put("mac", infrared.getMac());
                    dataMap.put("map", infrared.getMap());
                    log.info(dataMap.toString());
                    res2.setData(dataMap);
                    res.setData(arrayList);
                    continue;
                    //return res;
                }
                if (infrared.getMac().length() != 14 && infrared.getMac().startsWith("EI022083")) {
                    res2.setMessage("请输入正确的检测器编号！");
                    res2.setCode(400);
                    dataMap.put("placeName", infrared.getPlaceName());
                    dataMap.put("mac", infrared.getMac());
                    dataMap.put("map", infrared.getMap());
                    res2.setData(dataMap);
                    arrayList.add(res2);
                    res.setData(arrayList);
                    continue;
                    //return res;
                }
                String rawProductId = infrared.getMac().substring(infrared.getMac().length() - 6);
                Integer decimalNum = Integer.parseInt(rawProductId, 16);
                //车位检测器地图
                Map_2d currentInfraredMapName = parkMapper.getCurrentInfrared(decimalNum, null);
                //车位地图
                Map_2d infraredMapName = parkMapper.getCurrentInfraredMapName(places.get(0).getId(), null);
                //当前检测器绑定的车位
                List<Infrared> infreds = tagMapper.findIredByIdAndName(null, null, String.valueOf(decimalNum));
                ////查询该车位检测器是否存在，且是否绑定车位
                List<Infrared> infreds2 = tagMapper.findInfraredId(places.get(0).getId(), null, null);
                // 查询该车位是否绑定了其他车位检测器
                List<Infrared> placeNames = parkingService.getPlaceByName(infrared.getMap(), infrared.getPlaceName());
                if(!NullUtils.isEmpty(placeNames)) {
                    for (Infrared placeName : placeNames) {
                        DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
                        deviceInfoVO.setInfraredId(placeName.getInfraredId());
                        deviceInfoVO.setPlaceId(placeName.getPlaceId());
                        deviceInfoVO.setMap(placeName.getMap());
                        deviceInfoVO.setNum(placeName.getNum());
                        deviceInfoVO.setFid(placeName.getFid());
                        deviceInfoVO.setPlaceName(placeName.getPlaceName());
                        deviceInfoVO.setX(Double.valueOf(places.get(0).getX()));
                        deviceInfoVO.setY(Double.valueOf(places.get(0).getY()));
                        deviceInfoVO.setFloor(Short.valueOf(places.get(0).getFloor()));
                        deviceInfoVO.setMac(placeName.getRawProductId());
                        duplicateDataList.add(deviceInfoVO);
                    }
                }
                if(!NullUtils.isEmpty(infreds)&&!NullUtils.isEmpty(infreds.get(0).getPlace())){
//                    Map_2d infraredByPlace = parkMapper.getCurrentInfraredMapName(infreds.get(0).getPlace(), infreds.get(0).getMap());
//                    if(!NullUtils.isEmpty(infraredByPlace)){
//                        for (Infrared infred : infreds) {
//                            DeviceInfoVO deviceInfoVO = new DeviceInfoVO();
//                            deviceInfoVO.setInfraredId(infred.getInfraredId());
//                            deviceInfoVO.setPlaceId(infred.getPlaceId());
//                            deviceInfoVO.setMap(infred.getMap());
//                            deviceInfoVO.setNum(infred.getNum());
//                            deviceInfoVO.setFid(infred.getFid());
//                            deviceInfoVO.setX(Double.valueOf(places.get(0).getX()));
//                            deviceInfoVO.setY(Double.valueOf(places.get(0).getY()));
//                            deviceInfoVO.setFloor(Short.valueOf(places.get(0).getFloor()));
//                            deviceInfoVO.setPlaceName(infred.getPlaceName());
//                            deviceInfoVO.setMac(infred.getRawProductId());
//                            duplicateDataList.add(deviceInfoVO);
//                        }
//                    }
                }

                dataMap.put("placeName", places);
                dataMap.put("decimalNum", decimalNum);
                if (!NullUtils.isEmpty(placeNames)) {
                    if (infrared.getChangePlace()) {
                        if (NullUtils.isEmpty(currentInfraredMapName)) {
                            dataMap.put("offlineData", infrared);
                            dataMap.put("duplicateData", duplicateDataList);
                            res2.setCode(402);
                            res2.setMessage(LocalUtil.get(infrared.getPlaceName() + "该车位已经被" + infraredMapName.getName() + "编号为" + placeNames.get(0).getRawProductId() + "车位检测器绑定！"));
                            res2.setData(dataMap);
                            arrayList.add(res2);
                            res.setData(arrayList);
                            continue;
                        }
                        dataMap.put("offlineData", infrared);
                        dataMap.put("duplicateData", duplicateDataList);
                        res2.setCode(402);
                        res2.setMessage(LocalUtil.get(infrared.getPlaceName() + "该车位已经被" + infraredMapName.getName() + "编号为" + placeNames.get(0).getRawProductId() + "车位检测器绑定！"));
//                        res2.setMessage(infrared.getMac() + "该车位检测器已经被" + currentInfraredMapName.getName() + "车位名为" + (infrared.getPlaceName()!=null ? infrared.getPlaceName():null) + "的车位绑定！");

                        res2.setData(dataMap);
                        arrayList.add(res2);
                        res.setData(arrayList);
                        continue;
                    } else {
                        if (!NullUtils.isEmpty(infreds)) {
                            tagMapper.delInfraredApp(infreds.get(0).getId());
                        }
                        Infrared infred = new Infrared();
                        infred.setPlace(places.get(0).getId());
                        infred.setMap(infrared.getMap());
                        infred.setNum(String.valueOf(decimalNum));
                        infred.setRawProductId(infrared.getMac());
                        infred.setNetworkstate((short) 0);
                        infred.setStatus((short) 1);
                        infred.setPower((short) 0);
                        infred.setAddTime(date);
                        tagMapper.addInfrared(infred);
                        res2.setCode(200);
                        res2.setMessage("车位检测器-添加成功");
                        dataMap.put("infred", infred);
                        res2.setData(dataMap);
                        arrayList.add(res2);
                        res.setData(arrayList);
                        JSONObject jsonArea = new JSONObject();
                        List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
                        jsonArea.put("uid", "-1");
                        jsonArea.put("type", 22);
                        jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
                        appletsWebSocket.sendAll(jsonArea.toString());

                        String ip = IpUtil.getIpAddr(request);
                        String address = ip2regionSearcher.getAddressAndIsp(ip);
                        operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("小程序检测器"+infrared.getMap()), now);
                        continue;
                    }
                }

                if (infreds != null && infreds.size() > 0) {
                    Infrared infred = infreds.get(0);
                    if (!NullUtils.isEmpty(infred) && !NullUtils.isEmpty(infred.getPlace())) {
                        if (!infrared.getChangeDevice()) {
                            List<ParkingPlace> oldPlcaes = parkMapper.findByAllPlace(infred.getPlace(), null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                            ParkingPlace place = (oldPlcaes.size() == 0 ? null : oldPlcaes.get(0));
                            if (place != null) {
                                place.setState((short) 1);
                                place.setAddTime(date);
                                parkMapper.updatePlace(place);
                            }
                        } else if (!NullUtils.isEmpty(placeNames)) {
                            if (infrared.getChangeDevice()) {
                                List<ParkingPlace> oldPlcaes = parkMapper.getPlaceById(infred.getPlace());
                                ParkingPlace place = (oldPlcaes.size() == 0 ? null : oldPlcaes.get(0));
                                if (place != null) {
                                    place.setState((short) 1);
                                    place.setAddTime(date);
                                    parkMapper.updatePlace(place);
                                }
                            }
                        } else if (!NullUtils.isEmpty(currentInfraredMapName)) {
                            res2.setMessage(infrared.getMac() + "该车位检测器已经被" + currentInfraredMapName.getName() + "车位名为" + (infred.getPlaceName()!=null ? infred.getPlaceName():null) + "的车位绑定！");
                            res2.setCode(502);
                            dataMap.put("offlineData", infrared);
                            dataMap.put("duplicateData", duplicateDataList);
                            res2.setData(dataMap);
                            arrayList.add(res2);
                            res.setData(arrayList);
                            continue;
                        }
                    }
                    infred.setNum(String.valueOf(decimalNum));
                    infred.setPlace(places.get(0).getId());
                    infred.setMap(infrared.getMap());
                    infred.setRawProductId(infrared.getMac());
                    infred.setStatus((short) 1);
                    infred.setNetworkstate((short) 0);
                    infred.setUpdateTime(new Date());
                    tagMapper.updateInfrared(infred);
                    parkMapper.updatePlaceById(places.get(0).getId(), (short) 1,null,null,null,null,null);
                    dataMap.put("infred", infred);
                    res2.setCode(200);
                    res2.setMessage("车位检测器-更新成功");
                    dataMap.put("code", 200);
                    dataMap.put("message", res2.getMessage());
                    res2.setData(dataMap);
                    arrayList.add(res2);
                    res.setData(arrayList);
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat("小程序检测器"+infrared.getMap()), now);
                    continue;
                } else if (NullUtils.isEmpty(placeNames) && placeNames.size() < 1) {
                    Infrared infred = new Infrared();
                    infred.setPlace(places.get(0).getId());
                    infred.setMap(infrared.getMap());
                    infred.setNum(String.valueOf(decimalNum));
                    infred.setRawProductId(infrared.getMac());
                    infred.setNetworkstate((short) 0);
                    infred.setStatus((short) 1);
                    infred.setPower((short) 0);
                    infred.setAddTime(date);
                    tagMapper.addInfrared(infred);
                    res2.setCode(200);
                    res2.setMessage("车位检测器-添加成功");
                    dataMap.put("infred", infred);
                    res2.setData(dataMap);
                    arrayList.add(res2);
                    res.setData(arrayList);
                    JSONObject jsonArea = new JSONObject();
                    List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
                    jsonArea.put("uid", "-1");
                    jsonArea.put("type", 22);
                    jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
                    appletsWebSocket.sendAll(jsonArea.toString());

                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("2小程序检测器"+infrared.getMap()), now);
                    continue;
                }
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    public static Object speechless(List<Object> list) {
        return list = list.stream().distinct().collect(Collectors.toList());
    }

    @RequestMapping(value = "/delInfrared/{ids}")
    @ApiOperation(value = "uwb网关删除接口", notes = "uwb网关id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "uwb网关id集", required = true, dataType = "String")
    public CommonResult delInfrared(@PathVariable("ids") String ids) {
        try {
            String uid = "12";
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(sysUser)) {
                uid = String.valueOf(sysUser.getUserId());
            }

            String[] placeId = tagMapper.getInfraredPlace(ids.split(","));

            if (tagMapper.delInfrared(ids.split(",")) > 0) {
                if (!NullUtils.isEmpty(placeId)) {
                    tagMapper.updatePlace(placeId,1);
                } else {
                    return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
                }
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}

