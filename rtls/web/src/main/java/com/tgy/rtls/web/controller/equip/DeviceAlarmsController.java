package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceAlarmsVo;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
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
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.equip
 * @Author: wuwei
 * @CreateTime: 2023-12-26 14:36
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/deviceAlarms")
@CrossOrigin
public class DeviceAlarmsController {
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;


    @MyPermission
    @RequestMapping(value = "/getDeviceAlarmsData")
    public CommonResult<Object> getDeviceAlarmsData(Integer id,String num,String placeName,Integer state, Integer map, Integer priority,Integer deviceType, Integer alarmType,
                                                    @RequestParam(value = "desc", defaultValue = "d.start_time desc") String desc, Integer pageIndex, Integer pageSize,String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if(-1==pageSize){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), deviceAlarmsService.getDeviceAlarmsData(id,num,placeName,state, map,priority,deviceType,alarmType,desc,null,null,mapids));

            }
//            if (pageSize != -1) {
//                PageHelper.startPage(pageIndex, pageSize);
//            }

            long count = deviceAlarmsService.countWithConditions(id,num, placeName, state, map, priority,deviceType, alarmType);
            int pages = (int) ((count + pageSize - 1) / pageSize);
            List<DeviceAlarmsVo> deviceAlarms = deviceAlarmsService.getDeviceAlarmsData(id,num,placeName,state, map,priority,deviceType,alarmType,desc,pageIndex,pageSize,mapids);
            PageInfo<DeviceAlarmsVo> pageInfo = new PageInfo<>(deviceAlarms);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", count);
            result.put("pages",pages);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateDeviceAlarms/{ids}")
    @RequiresPermissions("das:se")
    public CommonResult<Object> updateDeviceAlarms(@PathVariable("ids") String ids) {
        try {
            if(!NullUtils.isEmpty(ids)){
                if(deviceAlarmsService.updateByIds(ids.split(","))){
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
    public CommonResult<Object> getDeviceAlarmsTypeConfig(Integer id,Integer deviceName,Integer alarmType,Integer pageIndex,Integer pageSize,@RequestParam(defaultValue = "id desc") String desc) {
        try {
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<DeviceAlarmsVo> deviceAlarms = deviceAlarmsService.getDeviceAlarmsTypeConfig(id,deviceName,alarmType,desc);
            PageInfo<DeviceAlarmsVo> pageInfo = new PageInfo<>(deviceAlarms);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }



    @RequestMapping(value = "/getDeviceAlarmsTypeConfigById")
    @RequiresPermissions(value = {"atc:see","atc:edit"},logical = Logical.OR)
    public CommonResult<Object> getDeviceAlarmsTypeConfigById(Integer id) {
        try {
            List<DeviceAlarmsVo> deviceAlarms = deviceAlarmsService.getDeviceAlarmsTypeConfig(id,null,null,null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), deviceAlarms);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/addDeviceAlarmsTypeConfig")
    @RequiresPermissions({"atc:add"})
    public CommonResult<Object> addDeviceAlarmsTypeConfig(@RequestBody DeviceAlarmsVo deviceAlarms, HttpServletRequest request) {
        try {
            if (!NullUtils.isEmpty(deviceAlarms)) {
                deviceAlarmsService.addDeviceAlarmsTypeConfig(deviceAlarms);
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);

            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/editDeviceAlarmsTypeConfig")
    @RequiresPermissions({"atc:edit"})
    public CommonResult<Object> editDeviceAlarmsTypeConfig(@RequestBody DeviceAlarmsVo deviceAlarms,HttpServletRequest request) {
        try {

            if (!NullUtils.isEmpty(deviceAlarms)) {
                List<DeviceAlarmsVo> deviceAlarms1 = deviceAlarmsService.getDeviceAlarmsTypeConfig(deviceAlarms.getId(),null,null,null);
                if(!NullUtils.isEmpty(deviceAlarms1)){
                    deviceAlarms.setDeviceTypeId(deviceAlarms1.get(0).getDeviceTypeId());
                    deviceAlarms.setAlarmsTypeId(deviceAlarms1.get(0).getAlarmsTypeId());
                    deviceAlarmsService.editDeviceAlarmsTypeConfig(deviceAlarms);
                    LocalDateTime now = LocalDateTime.now();
                    Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);
                }
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/delDeviceAlarmsConfig/{ids}")
    @RequiresPermissions({"atc:del"})
    public CommonResult<Object> delDeviceAlarmsConfig(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            if (!NullUtils.isEmpty(ids)) {
                deviceAlarmsService.delDeviceAlarmsConfig(ids.split(","));
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
