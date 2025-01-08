package com.tgy.rtls.web.controller.vip;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.remote.RemoteRequestServiceImpl;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.vip
 * @author: wuwei
 * @CreateTime: 2023-04-06 09:07
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/vip")
@Slf4j
@Api("地锁管理")
public class FloorLockController {
    @Resource
    private FloorLockService floorLockService;
    @Resource
    private RemoteRequestServiceImpl requestService;
    @Resource
    private VipParkingService vipParkingService;
    @Resource
    private OperationlogService operationlogService;
    @Resource
    private Ip2regionSearcher ip2regionSearcher;
    @Resource
    private  WxMaService wxMaService;
    @Resource
    private FastFileStorageClient fastFileStorageClient;
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Resource
    private ParkMapper parkMapapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;


    @MyPermission
    @RequestMapping(value = "/getFloorLockInfo")
    @ApiOperation(value = "获取地锁信息", notes = "111")
    public CommonResult<Object> getFloorLockInfo(String parkingName, Long map, String deviceNum, Integer placeState,Integer floorState,Integer state,Integer networkstate,String floorName, Integer pageIndex, Integer pageSize,
                                                 @RequestParam(value = "desc", defaultValue = "start_time desc") String desc, String maps) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }

            List<FloorLock> data = floorLockService.getFloorLockInfo(map, deviceNum, parkingName, placeState, desc,floorName,networkstate,floorState,state, mapids);
            PageInfo<FloorLock> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            res.setData(result);
            operationlogService.addOperationlog(member.getUid(), LocalUtil.get(KafukaTopics.QUERY_PERSONPERMISSION));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"fk:add"})
    @RequestMapping(value = "/addFloorLockInfo")
    @ApiOperation(value = "添加地锁信息", notes = "111")
    public CommonResult<Object> addFloorLockInfo(@RequestBody FloorLock floorLock,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            floorLock.setStartTime(now);
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }

            List<ParkingPlace> vipParkingList;
            if (!NullUtils.isEmpty(floorLock.getDeviceNum())) {
                List<FloorLock> floorLock1 = floorLockService.getConditionDataById(floorLock.getDeviceNum(), null,null, null);
                if (!NullUtils.isEmpty(floorLock1)) {
                    return new CommonResult<>(400, LocalUtil.get("该设备编号已绑定地锁"));
                }
            }

            if (!NullUtils.isEmpty(floorLock.getParkingName())) {
                floorLock.setParkingName(floorLock.getParkingName().toUpperCase());
                vipParkingList = vipParkingService.getInfoByMapAndName(floorLock.getMap(), floorLock.getParkingName(), null, null);
                floorLock.setPlace(vipParkingList.get(0).getId());
                List<FloorLock> floorLock1 = floorLockService.getConditionData(null, floorLock.getPlace(), null, null);
                if (!NullUtils.isEmpty(floorLock1)) {
                    return new CommonResult<>(400, LocalUtil.get("该车位号已绑定地锁"));
                }
            }

            if (NullUtils.isEmpty(floorLock.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(floorLock.getDeviceNum())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }

            floorLockService.addFloorLockInfo(floorLock);
            ParkingPlace place =null;
            String companyId = "";
            if(!NullUtils.isEmpty(floorLock.getPlace())){
                place = parkMapapper.findPlace(floorLock.getPlace(), null, null, null);
                companyId = NullUtils.isEmpty(place.getCompany()) ? "" : "c=" + place.getCompany();
            }

            String num = "n=" + floorLock.getDeviceNum();
            String mapId = "m=" + floorLock.getMap();
            String placeId = NullUtils.isEmpty(floorLock.getPlace()) ? "" : "p=" + floorLock.getPlace();
            String deviceId = "d=" + floorLock.getId();
            List<String> params = new ArrayList<>();
            params.add(deviceId);
            params.add(num);
            params.add(mapId);
            if (!companyId.isEmpty()) {
                params.add(companyId);
            }
            if (!placeId.isEmpty()) {
                params.add(placeId);
            }

            String scene = String.join("&", params);
            System.out.println("join = " + scene);
            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/lockList/lockList", uploadFolder+"floorLock/");
//           File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/lockList/lockList", "C:\\Users\\Administrator\\Pictures\\新建文件夹", false, "develop", 430, true, null, false);
//             File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene,"pages/lockList/lockList", uploadFolder+"floorLock/", false, "develop", 430, true, null, false);

            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            floorLock.setQrcode(storePath.getFullPath());
            floorLock.setQrcodelocal("/rtls/floorLock/" + file.getName());

            floorLockService.updateById(floorLock);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), floorLock);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY),e.getMessage());
        }
    }

    @RequiresPermissions({"fk:del"})
    @RequestMapping(value = "/delFloorLockInfo/{ids}")
    @ApiOperation(value = "删除地锁信息", notes = "111")
    public CommonResult<Object> delFloorLockInfo(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            floorLockService.delFloorLockInfo(ids.split(","));
            eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, 4));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"fk:edit"})
    @RequestMapping(value = "/editFloorLockInfo")
    @ApiOperation(value = "修改地锁信息", notes = "111")
    public CommonResult<Object> editFloorLockInfo(@RequestBody FloorLock floorLock,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            floorLock.setEndTime(now);
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }

            List<ParkingPlace> vipParkingList;
            if (!NullUtils.isEmpty(floorLock.getDeviceNum())) {
                List<FloorLock> floorLock1 = floorLockService.getConditionDataById(floorLock.getDeviceNum(), null, Math.toIntExact(floorLock.getId()), null);
                if (!NullUtils.isEmpty(floorLock1)) {
                    return new CommonResult<>(400, LocalUtil.get("该设备编号已绑定地锁"));
                }
            }

            if (!NullUtils.isEmpty(floorLock.getParkingName())) {
                floorLock.setParkingName(floorLock.getParkingName().toUpperCase());
                vipParkingList = vipParkingService.getInfoByMapAndName(floorLock.getMap(), floorLock.getParkingName(), null, null);
                floorLock.setPlace(vipParkingList.get(0).getId());
                List<FloorLock> floorLock1 = floorLockService.getConditionDataById(null, floorLock.getPlace(), Math.toIntExact(floorLock.getId()), null);
                if (!NullUtils.isEmpty(floorLock1)) {
                    return new CommonResult<>(400, LocalUtil.get("该车位号已绑定地锁"));
                }
            }


            if (NullUtils.isEmpty(floorLock.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(floorLock.getDeviceNum())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            ParkingPlace place =null;
            String companyId = "";
            if(!NullUtils.isEmpty(floorLock.getPlace())){
                place = parkMapapper.findPlace(floorLock.getPlace(), null, null, null);
                companyId = NullUtils.isEmpty(place.getCompany()) ? "" : "c=" + place.getCompany();
            }
            String num = "n=" + floorLock.getDeviceNum();
            String mapId = "m=" + floorLock.getMap();
            String placeId = NullUtils.isEmpty(floorLock.getPlace()) ? "" : "p=" + floorLock.getPlace();
            List<String> params = new ArrayList<>();
            params.add(num);
            params.add(mapId);
            if (!companyId.isEmpty()) {
                params.add(companyId);
            }
            if (!placeId.isEmpty()) {
                params.add(placeId);
            }

            String scene = String.join("&", params);
            System.out.println("join = " + scene);
            File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/lockList/lockList", uploadFolder+"floorLock/");
//           File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene, "pages/lockList/lockList", "C:\\Users\\Administrator\\Pictures\\新建文件夹", false, "develop", 430, true, null, false);
//             File file = wxMaService.getQrcodeService().createWxaCodeUnlimit(scene,"pages/lockList/lockList", uploadFolder+"floorLock/", false, "develop", 430, true, null, false);

            StorePath storePath = fastFileStorageClient.uploadFile(Files.newInputStream(file.toPath()), file.length(), "png", null);
            floorLock.setQrcode(storePath.getFullPath());
            floorLock.setQrcodelocal("/rtls/floorLock/" + file.getName());
//            MultipartFile multipartFile = FileUtils.getMultipartFile(file);
//            CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder+"floorLock/", multipartFile,storePath.getFullPath());
//            if (commonResult.getCode()==200) {
//            floorLock.setQrcodelocal("/rtls/floorLock/" + commonResult.getData());
//            }

            floorLockService.editFloorLockInfo(floorLock);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS), floorLock);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"fk:see", "fk:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getFloorLockInfoInfoById/{id}")
    @ApiOperation(value = "查看地锁信息", notes = "111")
    public CommonResult<Object> getFloorLockInfoInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            FloorLock floorLock = floorLockService.getFloorLockInfoInfoById(id);
            res.setData(floorLock);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

//    @PostMapping(value = "/checkAndCall",produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("fk:call")
    @PostMapping(value = "/checkAndCall")
    public CommonResult<Object> checkAndCall(String endTime, String nedid, String mode, String code) {
        String url = String.format("/kk/setNedMode?nedid=%s&mode=%s", nedid, mode);
        try {
            return requestService.checkAndCall(url);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(e.getCause().getMessage()));
        }

    }


    //这是一个main方法，程序的入口
    public static void main(String[] args){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime endDateTime = LocalDateTime.parse("2024-06-13 15:40:30", formatter);
        LocalDateTime currentDateTime = LocalDateTime.now();
        if (currentDateTime.isAfter(endDateTime)) {
            System.out.println("调用码不在有限期内");
        }
    }

}
