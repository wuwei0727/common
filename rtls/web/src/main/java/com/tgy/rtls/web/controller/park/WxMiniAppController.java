package com.tgy.rtls.web.controller.park;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.shortlink.GenerateShortLinkRequest;
import cn.binarywang.wx.miniapp.bean.urllink.GenerateUrlLinkRequest;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.TypeReference;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceAlarmsVo;
import com.tgy.rtls.data.entity.map.Feedback;
import com.tgy.rtls.data.entity.park.ParkingElevatorBinding;
import com.tgy.rtls.data.entity.park.ShangJia;
import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
import com.tgy.rtls.data.mapper.Camera.CameraConfigMapper;
import com.tgy.rtls.data.mapper.map.MapBuildCommonMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.park.ParkingExitMapper;
import com.tgy.rtls.data.service.map.impl.FeedbackService;
import com.tgy.rtls.data.service.map.impl.QrCodeLocationService;
import com.tgy.rtls.data.service.park.ParkingElevatorBindingService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.service.vip.ParkingInfoStatisticsService;
import com.tgy.rtls.data.service.vip.VipAreaService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * @BelongsProject: huizhan
 * @BelongsPackage: com.tgy.rtls.web.A.huizhan.controller.miniapp
 * @Author: wuwei
 * @CreateTime: 2024-05-16 19:12
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/WxMiniApp")
public class WxMiniAppController {
    private final WxMaService wxMaService;
    private final MapBuildCommonMapper mapBuildCommonMapper;
    private final ParkingElevatorBindingService parkingElevatorBindingService;
    private final ParkMapper parkMapper;
    private final ParkingExitMapper parkingExitMapper;
    private final VipParkingService vipParkingService;
    private final VipAreaService vipAreaService;
    private final FastFileStorageClient fastFileStorageClient;
    private final CameraConfigMapper cameraConfigMapper;
    private final DeviceAlarmsService deviceAlarmsService;
    private final ParkingInfoStatisticsService parkingInfoStatisticsService;
    private final FeedbackService feedbackService;
    private final QrCodeLocationService qrCodeLocationService;
    private final ParkingService parkingService;
    @Value("${file.uploadFolder}")
    private  String uploadFolder;




    @RequestMapping(value = "/getVipParkingSpaceInfo")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getVipParkingSpaceInfo(String name,Long map) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),vipParkingService.getVipParkingSpaceInfo(name, null, map, null, null, null, null,null, null, null));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPlace")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getPlace(Integer map,Short state, Short type,String reserve,String isReservable) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),parkingService.findByAllPlace2(null, null, null, map, null,reserve, state, null, null, null, type, null, null,null, null, null,null,null,isReservable,null));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "getQrCodeLocationById/{id}")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getQrCodeLocationById(@PathVariable("id") Integer id) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),qrCodeLocationService.getQrCodeLocationById(id));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addFeedback")
    public CommonResult<Object> addFeedback(@RequestBody Feedback floorLock, HttpServletRequest request) {
        try {
            feedbackService.save(floorLock);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), floorLock);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY),e.getMessage());
        }
    }

    @PostMapping("/findcar")
    @ApiOperation("增加反向寻车次数")
    public CommonResult<Object> addFindCarCount(@RequestBody ParkingInfoStatistics infoStatistics) {
        infoStatistics.setStartTime(LocalDateTime.now());
        infoStatistics.setEndTime(LocalDateTime.now());
        parkingInfoStatisticsService.addParkingInfoStatisticsfindCar(infoStatistics);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS));
    }

    @RequestMapping(value = "/updateDeviceAlarms")
    public CommonResult<Object> updateDeviceAlarms(String ids,String desc) {
        try {
            if(!NullUtils.isEmpty(ids)){
                if(deviceAlarmsService.updateByIds1(ids.split(","), desc)){
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                }
            }
            return new CommonResult<>(400, LocalUtil.get("ids为空"));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getDeviceAlarmsTypeConfig")
    public CommonResult<Object> getDeviceAlarmsTypeConfig() {
        try {
            List<DeviceAlarmsVo> deviceAlarms = deviceAlarmsService.getDeviceAlarmsTypeConfig(null,null,null,null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), deviceAlarms);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getDataByType/{mapId}/{id}/{type}")
    public CommonResult<Object> getDataByType(@PathVariable String mapId, @PathVariable Integer id, @PathVariable Integer type) {
        LocationType locationType = LocationType.fromValue(type);
        Object data = getDataForLocationType(locationType, mapId, id);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), data);
    }

    private Object getDataForLocationType(LocationType type, String mapId, Integer id) {
        switch (type) {
            case PLACE:
                return parkMapper.getPlaceDataById(mapId, id);
            case EXIT:
                return parkingExitMapper.getPlaceExitById(id);
            case ELEVATOR:
                return parkingElevatorBindingService.getParkingElevatorBindingById(String.valueOf(id));
            case BUILD:
                return mapBuildCommonMapper.getMapBuild2ById2(id);
            case BUSINESS:
                return parkMapper.getShangJiaById(String.valueOf(id));
            case COMPANY:
                return parkMapper.getComById(String.valueOf(id));
            case CAMERA:
                return cameraConfigMapper.getCameraById(id);
            case BUILD_2:
                return mapBuildCommonMapper.getMapBuild2ById(id);
            default:
                throw new IllegalArgumentException("Invalid location type: " + type);
        }
    }

    enum LocationType {
        PLACE(1, 2),
        EXIT(3),
        ELEVATOR(4),
        BUILD(5),
        BUSINESS(6),
        COMPANY(7),
        CAMERA(8),
        BUILD_2(9);

        private final List<Integer> values;

        LocationType(Integer... values) {
            this.values = Arrays.asList(values);
        }

        public static LocationType fromValue(int value) {
            return Arrays.stream(values())
                    .filter(type -> type.values.contains(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid location type: " + value));
        }
    }
    @GetMapping("/generateQrCode")
    public CommonResult<Object> generateQrCode(String scene, String env) {
        try {
//            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/map/map", "C:\\Users\\Administrator\\Pictures\\新建文件夹", false, env, 430, true, null, false);
            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/map/map", uploadFolder+"floorLock/", false, env, 430, true, null, false);
            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
//            Map<String, Object> map = new HashMap<>();
//            map.put("url", "/rtls/floorLock/"+file.getName());
//            map.put("path", storePath.getFullPath());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), "/UWB/rtls/floorLock/"+file.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/getShortLink")
    public CommonResult<Object> getShortLink(@RequestBody Map<String, Object> params) throws WxErrorException {
        List<Map<String, Object>> paramsList = Convert.convert(new TypeReference<List<Map<String, Object>>>() {
        }, params.get("params"));
        StringBuilder queryBuilder = new StringBuilder();
        ShangJia shangJia = new ShangJia();
        Object idObj = paramsList.get(0).getOrDefault("id", null);
        if (idObj instanceof Integer) {
            shangJia.setId((Integer) idObj);
        }
        for (Map<String, Object> param : paramsList) {
            for (Map.Entry<String, Object> entry : param.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 检查值是否为空或null，跳过它们
                if (value != null && !value.toString().isEmpty()) {
                    queryBuilder.append(key).append("=").append(value).append("&");
                }
            }
        }
        // 移除最后一个多余的"&"
        if (queryBuilder.charAt(queryBuilder.length() - 1) == '&') {
            queryBuilder.deleteCharAt(queryBuilder.length() - 1);
        }

        String urlLinkPath = "/pages/map/map";
        GenerateUrlLinkRequest request = GenerateUrlLinkRequest.builder()
                .query(queryBuilder.toString())
                .path(urlLinkPath)
                .build();
        String shortLink = wxMaService.getLinkService().generateUrlLink(request);
        if (!NullUtils.isEmpty(shortLink)) {
            shangJia.setUrl(shortLink);

        }
        parkMapper.updateShangJiaById(shangJia);
        log.error("shortLink -> {}", shortLink);
        return new CommonResult<>(200, "成功", shortLink);
    }

    @PostMapping("updateShangJiaById")
    public CommonResult<Object> updateShangJiaById(@RequestBody ShangJia shangJia) {
        parkMapper.updateShangJiaById(shangJia);
        return new CommonResult<>(200, "成功");

    }

    @PostMapping("getById")
    public CommonResult<Object> getById(Integer placeId, Integer areaId) {
        if (!NullUtils.isEmpty(placeId)) {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), vipParkingService.getByIds(placeId));
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), vipAreaService.getByIds(areaId));
    }

    @RequestMapping(value = "/getParkingElevatorBindingById/{id}")
    public CommonResult<Object> getParkingElevatorBindingById(@PathVariable("id") String id) {
        try {
            List<ParkingElevatorBinding> list = parkingElevatorBindingService.getParkingElevatorBindingById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), list);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getShortLink1")
    public CommonResult<Object> getShortLink1(@RequestBody Map<String, Object> params) throws WxErrorException {
        List<Map<String, Object>> paramsList = Convert.convert(new TypeReference<List<Map<String, Object>>>() {
        }, params.get("params"));
        StringJoiner stringJoiner = new StringJoiner("&");
        paramsList.stream()
                .flatMap(map -> map.entrySet().stream())
                .filter(entry -> entry.getValue() != null && !entry.getValue().toString().isEmpty())
                .forEach(entry -> stringJoiner.add(entry.getKey() + "=" + entry.getValue()));
        String urlLinkPath = "/pages/map/map";
        GenerateShortLinkRequest request = GenerateShortLinkRequest.builder()
                .pageUrl(urlLinkPath)
                .pageTitle(stringJoiner.toString())
                .build();
        String shortLink = wxMaService.getLinkService().generateShortLink(request);
        log.error("shortLink -> {}", shortLink);
        return new CommonResult<>(200, "成功", shortLink);
    }

    @PostMapping("getById111")
    public CommonResult<Object> getById111(Integer placeId, Integer num, Integer mapId) throws WxErrorException {
        String join = String.join("&", "mapId=" + mapId, "placeId=" + placeId, "num=" + num);
        System.out.println("join = " + join);
        wxMaService.getQrcodeService().createWxaCodeUnlimit(join, "pages/lockList/lockList", "C:\\Users\\Administrator\\Pictures\\新建文件夹", false, "develop", 430, true, null, false);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
    }
}
