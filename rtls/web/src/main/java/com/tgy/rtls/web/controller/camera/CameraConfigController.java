package com.tgy.rtls.web.controller.camera;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.impl.CameraAreaBindingService;
import com.tgy.rtls.data.service.park.impl.CameraAreaInfoService;
import com.tgy.rtls.data.service.park.impl.CameraCoordinatesService;
import com.tgy.rtls.data.service.park.impl.CameraParkingSpaceService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cameras/")
@RequiredArgsConstructor
@Slf4j
public class CameraConfigController {

    private final CameraConfigService service;
    private final CameraAreaInfoService cameraAreaInfoService;
    private final CameraAreaBindingService cameraAreaBindingService;
    private final CameraParkingSpaceService cameraParkingSpaceService;
    private final CameraCoordinatesService cameraCoordinatesService;
    private final OperationlogService operationlogService;
    private final Ip2regionSearcher ip2regionSearcher;
    private final ApplicationEventPublisher eventPublisher;

    @MyPermission
    @GetMapping("getAllOrFilteredCameras")
    public CommonResult<Object> getAllOrFilteredCameras(String serialNumber, String name, String map, String networkState, String floorName,Integer pageIndex, Integer pageSize,
                                                        @RequestParam(value = "desc", defaultValue = "c.create_time desc") String desc,String maps){
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<CameraConfigResponse> cameras = service.getAllOrFilteredCameras(serialNumber, name, map, networkState,floorName, desc, mapids);
            long count = service.count();
            PageInfo<CameraConfigResponse> pageInfo = new PageInfo<>(cameras);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("pages", pageInfo.getPages());
            result.put("total", count);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            log.error("getAllOrFilteredCameras → serialNumber={},name={},map={},networkState={},floorName={},desc={},pageIndex={},pageSize={},maps={}", serialNumber,name,map,networkState,floorName,desc,pageIndex,pageSize,maps);
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("addCamera")
    @RequiresPermissions("cac:add")
    public CommonResult<Object> addCamera(@RequestBody CameraConfig config, HttpServletRequest request) {
        try {
            // Step 1: Check for duplicates
            List<CameraConfig> one = service.list(new QueryWrapper<CameraConfig>()
                    .eq("map", config.getMap())
                    .and(wrapper -> wrapper.eq("serial_number", config.getSerialNumber())
                            .or().eq("name", config.getName())));
            if (!NullUtils.isEmpty(one)) {
                throw new RuntimeException(LocalUtil.get(KafukaTopics.CAMERA_EXIST)); // Prevent continuation
            }
            boolean success = service.addCamera(config); // Delegate to service layer
            if (success) {
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.CAMERA_CONFIG)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            } else {
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.ADD_FAIL));
            }
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.ADD_FAIL));
        }
    }

    @GetMapping("getCameraById/{id}")
    @RequiresPermissions(value = {"cac:see","cac:edit"},logical = Logical.OR)
    public CommonResult<Object> getCameraById(@PathVariable int id) {
        CameraConfigResponse camera = service.getCameraById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), camera);
    }

    @PostMapping("updateCamera")
 @RequiresPermissions("cac:edit")
    public CommonResult<Object> updateCamera(@RequestBody CameraConfig config, HttpServletRequest request) {
        try {
            // Step 1: Check for duplicate camera configurations (excluding current ID)
            List<CameraConfig> existingConfigs = service.list(new QueryWrapper<CameraConfig>()
                    .ne("id", config.getId())
                    .eq("map", config.getMap())
                    .and(wrapper -> wrapper.eq("serial_number", config.getSerialNumber()).or().eq("name", config.getName())));
            if (!NullUtils.isEmpty(existingConfigs)) {
                throw new RuntimeException(LocalUtil.get(KafukaTopics.CAMERA_EXIST)); // Prevent continuation
            }
            boolean success = service.updateCamera(config); // Delegate to service
            if (success) {
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.CAMERA_CONFIG)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            } else {
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
            }
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
        }
    }



    @DeleteMapping("deleteCameraById/{ids}")
    @RequiresPermissions("cac:del")
    public CommonResult<Object> deleteCameraById(@PathVariable String ids,HttpServletRequest request) {
        boolean removed = service.deleteCameraByIds(StrUtils.convertStringToList(ids));
        eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, 5));
        LocalDateTime now = LocalDateTime.now();
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        String ip = IpUtil.getIpAddr(request);
        String address = ip2regionSearcher.getAddressAndIsp(ip);
        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.CAMERA_CONFIG)), now);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);

    }

}
