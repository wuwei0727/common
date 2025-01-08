package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.GuideScreenDevice;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ShowScreenConfigMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.GuideScreenDeviceService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*@Author: wuwei
*@CreateTime: 2023/5/24 23:09
*/
@RestController
@RequestMapping("/guideScreenDevice")
@Api("导引屏设备")
public class GuideScreenDeviceController {
    @Autowired
    private GuideScreenDeviceService guideScreenDeviceService;
    @Autowired
    private ShowScreenConfigMapper showScreenConfigMapper;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getAllGuideScreenDeviceOrConditionQuery")
    @ApiOperation(value = "获取导引屏设备信息", notes = "111")
    public CommonResult<Object> getAllGuideScreenDeviceOrConditionQuery(String map, String deviceId,String floorName, String locationName, String networkStatus , String ip, Integer pageIndex, Integer pageSize, @RequestParam(value = "desc", defaultValue = "addTime desc") String desc, String maps) {
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

            List<GuideScreenDevice> data = guideScreenDeviceService.getAllGuideScreenDeviceOrConditionQuery(map, deviceId,ip, locationName,networkStatus,desc,floorName, mapids);
            PageInfo<GuideScreenDevice> pageInfo = new PageInfo<>(data);
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

    @RequiresPermissions({"gsd:add"})
    @RequestMapping(value = "/addGuideScreenDevice")
    @ApiOperation(value = "添加导引屏设备信息", notes = "111")
    public CommonResult<Object> addGuideScreenDevice(@RequestBody GuideScreenDevice guideScreenDevice,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(guideScreenDevice.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(guideScreenDevice.getDeviceId())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(guideScreenDevice.getLocationName())) {
                return new CommonResult<>(400, LocalUtil.get("位置不能为空！！！"));
            }
            List<GuideScreenDevice> edit = guideScreenDeviceService.getGuideScreenDeviceById (null, String.valueOf (guideScreenDevice.getDeviceId ()), "add");
            if(!NullUtils.isEmpty (edit)){
                return new CommonResult<>(400, LocalUtil.get("设备ID重复"));
            }
            guideScreenDevice.setAddTime(LocalDateTime.now());
            guideScreenDeviceService.addGuideScreenDevice(guideScreenDevice);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_DEVICE)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"gsd:del"})
    @RequestMapping(value = "/delGuideScreenDevice/{ids}")
    @ApiOperation(value = "删除导引屏设备信息", notes = "111")
    public CommonResult<Object> delGuideScreenDevice(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            guideScreenDeviceService.delGuideScreenDevice(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_DEVICE)), now);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
    @RequiresPermissions(value = {"gsd:edit"})
    @RequestMapping(value = "/updateGuideScreenDevice")
    @ApiOperation(value = "更新导引屏设备信息", notes = "111")
    public CommonResult<Object> updateGuideScreenDevice(@RequestBody GuideScreenDevice guideScreenDevice, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(guideScreenDevice.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(guideScreenDevice.getDeviceId())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(guideScreenDevice.getLocationName())) {
                return new CommonResult<>(400, LocalUtil.get("位置不能为空！！！"));
            }
            List<GuideScreenDevice> edit = guideScreenDeviceService.getGuideScreenDeviceById (Math.toIntExact (guideScreenDevice.getId ()), String.valueOf (guideScreenDevice.getDeviceId ()), "edit");
            if(!NullUtils.isEmpty (edit)){
                return new CommonResult<>(400, LocalUtil.get("设备ID重复"));
            }
            guideScreenDevice.setUpdateTime(LocalDateTime.now());
            guideScreenDeviceService.updateGuideScreenDevice(guideScreenDevice);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_DEVICE)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"gsd:see", "gsd:edit", "gsd:add", "gsd:del"}, logical = Logical.OR)
    @RequestMapping(value = "/getGuideScreenDeviceById/{id}")
    @ApiOperation(value = "查看导引屏设备信息", notes = "111")
    public CommonResult<Object> getGuideScreenDeviceById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<GuideScreenDevice> screenConfigList = guideScreenDeviceService.getGuideScreenDeviceById(id,null,null);
            res.setData(screenConfigList);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
