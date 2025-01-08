package com.tgy.rtls.web.controller.camera;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.Camera.impl.CameraVehicleCaptureService;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/cameraVehicleCapture")
@Slf4j
public class CameraVehicleCaptureController {

    @Resource
    private CameraVehicleCaptureService cameraVehicleCaptureService;
    @Resource
    private OperationlogService operationlogService;
    @Resource
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @GetMapping("getAllOrFilteredCameraVehicleCapture")
    public CommonResult<Object> getAllOrFilteredCameraVehicleCapture(String name,String placeName,String license,String serialNumber, String map,String start,String end,Integer pageIndex, Integer pageSize,
                                                        @RequestParam(value = "desc", defaultValue = "c.create_time desc") String desc,String floorName,  String maps){
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<CameraVehicleCapture> cameras = cameraVehicleCaptureService.getAllOrFilteredCameraVehicleCapture(license,serialNumber, name,placeName,map,desc,start,end,floorName,  mapids);
            PageInfo<CameraVehicleCapture> pageInfo = new PageInfo<>(cameras);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            log.error("getAllOrFilteredCameras → serialNumber={},license={},map={},start={},end={},desc={},pageIndex={},pageSize={},maps={}", serialNumber,license,map,start,end,desc,pageIndex,pageSize,maps);
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("delCameraVehicleCapture/{ids}")
    @RequiresPermissions("cvc:del")
    public CommonResult<Object> delCameraVehicleCapture(@PathVariable String ids, HttpServletRequest request) {
        boolean removed = cameraVehicleCaptureService.removeBatchByIds(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        String ip = IpUtil.getIpAddr(request);
        String address = ip2regionSearcher.getAddressAndIsp(ip);
        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.CAMERA_CONFIG)), now);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);
    }

}
