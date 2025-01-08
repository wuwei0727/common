package com.tgy.rtls.web.controller.vip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.service.common.OperationlogService;
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
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.vip
 * @Author: wuwei
 * @CreateTime: 2023-04-04 09:31
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/vip")
@Slf4j
@Api("VIP车位管理")
public class VipParkingPlaceManageController {
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private RedissonClient redissonClient;
    @MyPermission
    @RequestMapping(value = "/getVipParkingSpaceInfo")
    @ApiOperation(value = "获取VIP车位信息", notes = "111")
    public CommonResult<Object> getVipParkingSpaceInfo(String name, String license, Long map, String phone, Short state,String floorName, Short type, Integer pageIndex, Integer pageSize,
                                                       @RequestParam(value = "desc", defaultValue = "v.id desc") String desc,Integer status, String maps) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            String[] mapids = null;
            if(pageSize<0){
                List<VipParking> data = vipParkingService.getVipParkingSpaceInfo(name, license, map, phone, state, type, desc,floorName,status, mapids);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),data);
            }
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            if (license != null) {
                license = license.toUpperCase();
            }
            List<VipParking> data = vipParkingService.getVipParkingSpaceInfo(name, license, map, phone, state, type, desc,floorName, status, mapids);
            PageInfo<VipParking> pageInfo = new PageInfo<>(data);
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

    @RequiresPermissions({"vps:add"})
    @RequestMapping(value = "/addVipParingSpaceInfo")
    @ApiOperation(value = "添加VIP车位信息", notes = "111")
    public CommonResult<Object> addVipParingSpaceInfo(@RequestBody VipParking vipParking, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            vipParking.setCreateTime(now);
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String startTime = vipParking.getStartTime().format(formatter);
            String endTime = vipParking.getEndTime().format(formatter);
            VipParking vipParking1 = vipParkingService.getVipParingSpaceInfoBySomeTimePart(null,vipParking.getName(), null, vipParking.getMap(), startTime, endTime);
            List<ParkingPlace> vipParkingList = null;
            if (!NullUtils.isEmpty(vipParking.getName().trim())) {
                vipParking.setName(vipParking.getName().toUpperCase().trim());
                vipParkingList = vipParkingService.getInfoByMapAndName(vipParking.getMap(), vipParking.getName().trim(), 0, 1);
                if (NullUtils.isEmpty(vipParkingList)) {
                    return new CommonResult<>(400, LocalUtil.get("请确认是否为空闲VIP车位！！！"));
                }
                vipParking.setFloor(Integer.valueOf(vipParkingList.get(0).getFloor()));
                vipParking.setPlace(vipParkingList.get(0).getId());
            }
            if (!NullUtils.isEmpty(vipParking.getLicense())) {
                vipParking.setLicense(vipParking.getLicense().toUpperCase().trim());
            } else {
                return new CommonResult<>(400, LocalUtil.get("车牌号不能为空！！！"));
            }

            if (NullUtils.isEmpty(vipParking.getReservationPerson())) {
                return new CommonResult<>(400, LocalUtil.get("预约人不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipParking.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get("手机号码不能为空！！！"));
            }
            if (!NullUtils.isEmpty(vipParking1)) {
                return new CommonResult<>(400, LocalUtil.get("该时间段已被占用，预约失败"));
            }
            if (vipParking.getStartTime().isAfter(vipParking.getEndTime()) || vipParking.getStartTime().isEqual(vipParking.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("开始时间早于或等于结束时间"));
            }
            if(vipParking.getStartTime().isBefore(now)|| vipParking.getEndTime().isBefore(now)){
                return new CommonResult<>(400, LocalUtil.get("开始时间或结束时间不能早于等于当前时间"));
            }
            if (NullUtils.isEmpty(vipParking.getStartTime()) && NullUtils.isEmpty(vipParking.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("预约时间不能为空！！！"));
            }
            if(vipParking.getStartTime().isAfter(now)){
                vipParking.setStatus(2);
            }else {
                vipParking.setStatus(1);
            }
            vipParkingService.addVipParingSpaceInfo(vipParking);
            if(vipParking.getStatus()==1){
                setExpirationHandler(vipParking.getId(), vipParking.getEndTime());
            }else if(vipParking.getStatus()==2&&vipParking.getStartTime().isAfter(now)){
                String activeKey = "vip:parking:active," + vipParking.getId();
                RBucket<String> activeBucket = redissonClient.getBucket(activeKey);
                long activeTime = Duration.between(now, vipParking.getStartTime()).toMillis();
                activeBucket.set(vipParking.getId().toString(), activeTime, TimeUnit.MILLISECONDS);

                setExpirationHandler(vipParking.getId(), vipParking.getEndTime());
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.VIP_PLACE)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), vipParking);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    private void setExpirationHandler(Long id,LocalDateTime endTime) {
        String redisKey = "vip:parking:expire," + id;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        // 计算过期时间
        long expireTime = Duration.between(LocalDateTime.now(), endTime).toMillis();
        if (expireTime > 0) {
            bucket.set(id.toString(), expireTime, TimeUnit.MILLISECONDS);
        }
    }

    @RequiresPermissions({"vps:del"})
    @RequestMapping(value = "/delVipParingSpaceInfo/{ids}")
    @ApiOperation(value = "删除VIP车位信息", notes = "111")
    public CommonResult<Object> delVipParingSpaceInfo(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            vipParkingService.delVipParingSpaceInfo(ids.split(","));
            LocalDateTime now = LocalDateTime.now();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.VIP_PLACE)), now);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"vps:edit"})
    @RequestMapping(value = "/editVipParingSpaceInfo")
    @ApiOperation(value = "修改VIP车位信息", notes = "111")
    public CommonResult<Object> editVipParingSpaceInfo(@RequestBody VipParking vipParking,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            vipParking.setUpdateTime(now);
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String startTime = vipParking.getStartTime().format(formatter);
            String endTime = vipParking.getEndTime().format(formatter);
            VipParking vipParking1 = vipParkingService.getVipParingSpaceInfoBySomeTimePart(vipParking.getId(),vipParking.getName(), null, vipParking.getMap(), startTime, endTime);
            List<ParkingPlace> vipParkingList = null;
            if (!NullUtils.isEmpty(vipParking.getName())) {
                vipParking.setName(vipParking.getName().toUpperCase().trim());
                vipParkingList = vipParkingService.getInfoByMapAndName(vipParking.getMap(), vipParking.getName().trim(), 0, 1);
                if (NullUtils.isEmpty(vipParkingList)) {
                    return new CommonResult<>(400, LocalUtil.get("没有该车位信息，请确认车位是否存在或车位被占用！！！"));
                }
                vipParking.setPlace(vipParkingList.get(0).getId());
                vipParking.setFloor(vipParking.getFloor());
            }
            if (!NullUtils.isEmpty(vipParking.getLicense())) {
                vipParking.setLicense(vipParking.getLicense().toUpperCase().trim());
            } else {
                return new CommonResult<>(400, LocalUtil.get("车牌号不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipParking.getReservationPerson())) {
                return new CommonResult<>(400, LocalUtil.get("预约人不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipParking.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get("手机号码不能为空！！！"));
            }
            if (!NullUtils.isEmpty(vipParking1)) {
                return new CommonResult<>(400, LocalUtil.get("该时间段已被占用，预约失败"));
            }

            if (vipParking.getStartTime().isAfter(vipParking.getEndTime()) || vipParking.getStartTime().isEqual(vipParking.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("开始时间早于或等于结束时间"));
            }
            if (NullUtils.isEmpty(vipParking.getStartTime()) && NullUtils.isEmpty(vipParking.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("预约时间不能为空！！！"));
            }
            if(vipParking.getStartTime().isAfter(now)){
                vipParking.setStatus(2);

                String activeKey = "vip:parking:active," + vipParking.getId();
                RBucket<String> activeBucket = redissonClient.getBucket(activeKey);
                long activeTime = Duration.between(now, vipParking.getStartTime()).toMillis();
                activeBucket.set(vipParking.getId().toString(), activeTime, TimeUnit.MILLISECONDS);
            }else {
                vipParking.setStatus(1);
            }
            vipParkingService.editVipParingSpaceInfo(vipParking);
            if(vipParking.getStatus()==1){
                updateExpirationTime(vipParking.getId(), vipParking.getEndTime());
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.VIP_PLACE)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS), vipParking);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    private void updateExpirationTime(Long id,LocalDateTime endTime) {
        String redisKey = "vip:parking:expire," + id;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        // 删除旧的过期key
        bucket.delete();

        // 设置新的过期时间
        long expireTime = Duration.between(LocalDateTime.now(), endTime).toMillis();
        if (expireTime > 0) {
            bucket.set(id.toString(), expireTime, TimeUnit.MILLISECONDS);
        }
    }

    @RequiresPermissions(value = {"vps:see", "vps:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getVipParingSpaceInfoById/{id}")
    @ApiOperation(value = "查看VIP车位信息", notes = "根据Id查看信息")
    public CommonResult<Object> getVipParingSpaceInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            VipParking vipParking = vipParkingService.getVipParingSpaceInfoById(id);
            res.setData(vipParking);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
