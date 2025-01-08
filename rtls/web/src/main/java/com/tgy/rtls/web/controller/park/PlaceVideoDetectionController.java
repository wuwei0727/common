package com.tgy.rtls.web.controller.park;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dtflys.forest.http.ForestResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraPlace;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.Camera.CarInfoResponse;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.eventserver.VehicleData;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVideoDetection;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.Camera.impl.CameraVehicleCaptureService;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.DockingService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.data.service.park.impl.CameraPlaceService;
import com.tgy.rtls.data.service.park.impl.ParkingServiceImpl;
import com.tgy.rtls.data.service.remote.RemoteRequestServiceImpl;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: wuwei
 * @CreateTime: 2023/5/31 16:45
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/placeVideoDetection")
@Slf4j
@Api("车位视频检测")
@Configurable
public class PlaceVideoDetectionController {
    @Resource
    private PlaceVideoDetectionService placeVideoDetectionService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private DockingService dockingService;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private CameraConfigService cameraConfigService;
    @Autowired
    private CameraPlaceService cameraPlaceService;
    @Autowired
    private CameraVehicleCaptureService cameraVehicleCaptureService;
    @Autowired(required = false)
    private RemoteRequestServiceImpl remoteRequestService;
    @Autowired(required = false)
     private ParkingServiceImpl park;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private  FastFileStorageClient fastFileStorageClient;
    @Value("${file.uploadFolder}")
    private  String uploadFolder;
    @Value("${file.url}")
    private String url;

    @MyPermission
    @RequestMapping(value = "/getAllPlaceVideoDetectionOrConditionQuery")
    @ApiOperation(value = "获取车位视频检测信息", notes = "111")
    public CommonResult<Object> getAllGuideScreenDeviceOrConditionQuery(String map, String ip, String placeInquireAddress, String licenseInquireAddress,String status, Integer pageIndex, Integer pageSize, @RequestParam(value = "desc", defaultValue = "addTime desc") String desc, String maps) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }

            List<PlaceVideoDetection> data = placeVideoDetectionService.getAllGuideScreenDeviceOrConditionQuery(map, ip, placeInquireAddress, licenseInquireAddress, desc,status,mapids);
            PageInfo<PlaceVideoDetection> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            res.setData(result);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"pvd:add"})
    @RequestMapping(value = "/addPlaceVideoDetection")
    @ApiOperation(value = "添加车位视频检测信息", notes = "111")
    public CommonResult<Object> addPlaceVideoDetection(@RequestBody PlaceVideoDetection placeVideoDetection, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(placeVideoDetection.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            List<PlaceVideoDetection> detectiont = placeVideoDetectionService.getPlaceVideoDetectiontByMap(String.valueOf(placeVideoDetection.getMap()), null, null);
            if (!NullUtils.isEmpty(detectiont)) {
                return new CommonResult<>(400, LocalUtil.get("当前地图下重复！！！"));
            }
//            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("mapId=" + placeVideoDetection.getMap(), "pages/licenseAdd/licenseAdd", uploadFolder+"floorLock/", false, "trial", 430, true, null, false);
            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("mapId=" + placeVideoDetection.getMap(), "pages/licenseAdd/licenseAdd", uploadFolder+"floorLock/");
            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            String fullPath = storePath.getFullPath();
            placeVideoDetection.setPhoto(fullPath);
            placeVideoDetection.setPhotolocal(url+fullPath);
            //if (NullUtils.isEmpty(placeVideoDetection.getIp())) {
            //    return new CommonResult<>(400, LocalUtil.get("IP地址不能为空！！！"));
            //}
            //if (NullUtils.isEmpty(placeVideoDetection.getPlaceInquireAddress())) {
            //    return new CommonResult<>(400, LocalUtil.get("车位查询地址不能为空！！！"));
            //}
            //if (NullUtils.isEmpty(placeVideoDetection.getLicenseInquireAddress())) {
            //    return new CommonResult<>(400, LocalUtil.get("车牌查询地址不能为空！！！"));
            //}
            placeVideoDetection.setAddTime(LocalDateTime.now());
            placeVideoDetectionService.addPlaceVideoDetection(placeVideoDetection);
            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(), ip.concat((address == null ? "" : address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PLACE_VIDEO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"pvd:del"})
    @RequestMapping(value = "/delPlaceVideoDetection/{ids}")
    @ApiOperation(value = "删除车位视频检测信息", notes = "111")
    public CommonResult<Object> delPlaceVideoDetection(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            placeVideoDetectionService.delPlaceVideoDetection(ids.split(","));
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(), ip.concat((address == null ? "" : address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PLACE_VIDEO)), now);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"pvd:edit"})
    @RequestMapping(value = "/updatePlaceVideoDetection")
    @ApiOperation(value = "更新车位视频检测信息", notes = "111")
    public CommonResult<Object> updatePlaceVideoDetection(@RequestBody PlaceVideoDetection placeVideoDetection, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(placeVideoDetection.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            List<PlaceVideoDetection> detectiont = placeVideoDetectionService.getPlaceVideoDetectiontByMap(String.valueOf(placeVideoDetection.getMap()), null, String.valueOf(placeVideoDetection.getId()));
            if (!NullUtils.isEmpty(detectiont)) {
                return new CommonResult<>(400, LocalUtil.get("当前地图下重复！！！"));
            }
//            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("mapId=" + placeVideoDetection.getMap(), "pages/licenseAdd/licenseAdd", uploadFolder+"floorLock/", false, "develop", 430, true, null, false);
            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit("mapId=" + placeVideoDetection.getMap(), "pages/licenseAdd/licenseAdd", uploadFolder+"floorLock/");
            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            String fullPath = storePath.getFullPath();
            placeVideoDetection.setPhoto(fullPath);
            placeVideoDetection.setPhotolocal(url+fullPath);
            //if (NullUtils.isEmpty(placeVideoDetection.getIp())) {
            //    return new CommonResult<>(400, LocalUtil.get("IP地址不能为空！！！"));
            //}
            //if (NullUtils.isEmpty(placeVideoDetection.getPlaceInquireAddress())) {
            //    return new CommonResult<>(400, LocalUtil.get("车位查询地址不能为空！！！"));
            //}
            //if (NullUtils.isEmpty(placeVideoDetection.getLicenseInquireAddress())) {
            //    return new CommonResult<>(400, LocalUtil.get("车牌查询地址不能为空！！！"));
            //}
            placeVideoDetection.setUpdateTime(LocalDateTime.now());
            placeVideoDetectionService.updatePlaceVideoDetection(placeVideoDetection);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(), ip.concat((address == null ? "" : address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.PLACE_VIDEO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"pvd:see", "pvd:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getPlaceVideoDetectionById/{id}")
    @ApiOperation(value = "查看车位视频检测信息", notes = "111")
    public CommonResult<Object> getPlaceVideoDetectionById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<PlaceVideoDetection> screenConfigList = placeVideoDetectionService.getPlaceVideoDetectionById(id);
            res.setData(screenConfigList);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPlaceByLicense")
    @ApiOperation(value = "车牌找车", notes = "111")
    public CommonResult<Object> getPlaceVideoDetectionById(String map, String license,@RequestParam(defaultValue = "false") boolean isManualSelect) {
        if (NullUtils.isEmpty(license)) {
            return new CommonResult<>(400, LocalUtil.get("请输入车牌号！！！"));
        }

        if ("178".equals(map)) {
            // 1. 先调用第三方API查询车牌信息
            String jsonBody = "{\"PlateNo\":\"" + license + "\"}";
            Instant start = Instant.now();

            ForestResponse<String> response = remoteRequestService.findCarInfo(jsonBody);

            Instant end = Instant.now();
            // 计算API调用的响应时间
            Duration duration = Duration.between(start, end);
            System.out.println("Third-party API response time: " + duration.toMillis() + " ms");
            if (response.isSuccess()) {
                String responseBody = response.getResult();

                JSONObject root = JSON.parseObject(responseBody);
                int code = root.getIntValue("Code");

                if (code == 0) {
                    // 获取 Describe 字段中的 JSON 字符串
                    String describeJson = root.getString("Describe");
                    List<CarInfoResponse.CarInfo> describeList = JSON.parseArray(describeJson, CarInfoResponse.CarInfo.class);

                    // 获取第一个元素
                    if (!NullUtils.isEmpty(describeList)) {
                        // 查询本地数据库是否有这个车位信息
                        List<VehicleData> placeList = cameraVehicleCaptureService.getPlaceByLicense(null, Integer.valueOf(map), describeList);
                        if (!NullUtils.isEmpty(placeList)) {
                            // 遍历 placeList 并与 describeList 进行匹配
                            for (VehicleData place : placeList) {
                                // 获取 placeList 中的 placeName (ParkingNo)
                                String placeName = place.getPlaceName();

                                // 在 describeList 中寻找匹配的 ParkingNo
                                describeList.stream()
                                        .filter(carInfo -> placeName.equals(carInfo.getParkingNo()))  // 匹配 ParkingNo
                                        .findFirst()
                                        .ifPresent(carInfo -> place.setLicense(carInfo.getCarPlateNo()));  // 将 CarPlateNo 赋值给 place 的 license
                            }

                            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), placeList);
                        }
                    }
                }
            }
        }

        // 2. 如果第三方API未能成功查到信息，则继续使用摄像头逻辑查询
        List<VehicleData> place = cameraVehicleCaptureService.getPlaceByLicense(license, Integer.valueOf(map), null);
        if (!NullUtils.isEmpty(place)) {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), place);
        }

        // 查询视频车位数据是否存在时间最近的记录
        List<CameraVehicleCapture> captures = cameraVehicleCaptureService.getPlaceById2(license, null, null, Integer.valueOf(map));
        if (NullUtils.isEmpty(captures)) {
            return new CommonResult<>(200, LocalUtil.get("未查询到该车辆信息！！！"));
        }
        if (!isManualSelect) {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), captures);
        }
        // 检查视频记录表中唯一标志位>1且<4的情况
        List<CameraVehicleCapture> flagBetweenOneAndFour = captures.stream().filter(c -> c.getUniqueFlag() != null && c.getUniqueFlag() > 1 && c.getUniqueFlag() < 4).filter(c -> c.getLicense() != null && c.getLicense().equals(license)).collect(Collectors.toList());

        if (!flagBetweenOneAndFour.isEmpty()) {
            // 从camera_place查询与视频记录关联的车位信息
            List<CameraVehicleCapture> result = new ArrayList<>();
            for (CameraVehicleCapture capture : flagBetweenOneAndFour) {
                // 查询关联的camera_place记录
                List<CameraPlace> cameraPlaces = cameraPlaceService.list(new LambdaQueryWrapper<CameraPlace>().eq(CameraPlace::getCameraVehicleCaptureId, capture.getId()));

                if (!cameraPlaces.isEmpty()) {
                    // 查询关联的停车位信息
                    List<ParkingPlace> parkingPlaces = parkingService.findByIds(
                            cameraPlaces.stream()
                                    .map(CameraPlace::getPlaceId)
                                    .collect(Collectors.toList())
                    );

                    // 设置车位信息
                    for (ParkingPlace parkingPlace : parkingPlaces) {
                        CameraVehicleCapture newCapture = new CameraVehicleCapture();
                        BeanUtils.copyProperties(capture, newCapture);

                        newCapture.setMap(parkingPlace.getMap());
                        newCapture.setPlace(parkingPlace.getId().toString());
                        newCapture.setPlaceName(parkingPlace.getName());
                        newCapture.setFloor(parkingPlace.getFloor());
                        newCapture.setX(parkingPlace.getX());
                        newCapture.setY(parkingPlace.getY());
                        newCapture.setFid(parkingPlace.getFid());
                        result.add(newCapture);
                    }
                }
            }
            result = result.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(CameraVehicleCapture::getPlace))), ArrayList::new));
            // 返回多个车位信息，小程序将多个车位突出显示
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        }

        // 检查视频记录表中唯一标志位=1的情况
        List<CameraVehicleCapture> flagEqualsOne = captures.stream().filter(c -> c.getUniqueFlag() != null && c.getUniqueFlag() == 1).filter(c -> c.getLicense() != null && c.getLicense().equals(license)).collect(Collectors.toList());
        if (!flagEqualsOne.isEmpty()) {
            // 从视频记录关联的车位信息返回
            List<CameraVehicleCapture> result = new ArrayList<>();
            for (CameraVehicleCapture capture : flagEqualsOne) {
                Integer placeId = null;
                if (capture.getCapturePlace() != null) {
                    placeId = capture.getCapturePlace();
                } else if (capture.getPlace() != null) {
                    placeId = Integer.valueOf(capture.getPlace());
                }
                // 查询关联的停车位信息
                if (placeId != null) {
                    ParkingPlace parkingPlace = parkingService.getPlaceByPlaceId(placeId,null,null,null);
                    if (parkingPlace != null) {
                        capture.setMap(parkingPlace.getMap());
                        capture.setPlace(parkingPlace.getId().toString());
                        capture.setPlaceName(parkingPlace.getName());
                        capture.setFloor(parkingPlace.getFloor());
                        capture.setX(parkingPlace.getX());
                        capture.setY(parkingPlace.getY());
                        capture.setFid(parkingPlace.getFid());
                        // 设置其他必要的车位信息
                        result.add(capture);
                    }
                }
            }
            result = result.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(CameraVehicleCapture::getPlace))), ArrayList::new));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        }


        // 如果上述条件都不满足，则从视频记录关联摄像头位置信息并返回给小程序，小程序显示区域级反向寻车
        List<CameraVehicleCapture> matchedCaptures = captures.stream()
                .filter(c -> c.getLicense() != null && c.getLicense().equals(license))
                .collect(Collectors.toList());

        List<CameraVehicleCapture> result = new ArrayList<>();
        List<String> serialNumbers = matchedCaptures.stream()
                .map(CameraVehicleCapture::getSerialNumber)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!serialNumbers.isEmpty()) {
            List<CameraConfig> cameraConfigs = cameraConfigService.list(
                    new QueryWrapper<CameraConfig>().in("serial_number", serialNumbers));

            if (!NullUtils.isEmpty(cameraConfigs)) {
                Map<String, CameraConfig> configMap = cameraConfigs.stream()
                        .collect(Collectors.toMap(CameraConfig::getSerialNumber, config -> config));

                matchedCaptures.forEach(capture -> {
                    CameraConfig config = configMap.get(capture.getSerialNumber());
                    if (config != null) {
                        result.add(createCaptureFromConfig(capture, config));
                    }
                });
            }
        }

        return !result.isEmpty()
                ? new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result)
                : new CommonResult<>(200, LocalUtil.get("未查询到该车辆信息！！！"));
    }

        // 辅助方法：根据配置创建捕获记录
        private CameraVehicleCapture createCaptureFromConfig(CameraVehicleCapture capture, CameraConfig config) {
            CameraVehicleCapture result = new CameraVehicleCapture();
            result.setId(config.getId());
            result.setSerialNumber(config.getSerialNumber());
            result.setAreaName(capture.getAreaName());
            result.setCameraVertexInfo(config.getCameraVertexInfo());
            result.setLicense(capture.getLicense());
            result.setName(config.getName());
            result.setX(config.getX());
            result.setY(config.getY());
            result.setMap(Integer.valueOf(config.getMap()));
            result.setFloor(String.valueOf(config.getFloor()));
            result.setFid(config.getFid());
            return result;
        }
}
