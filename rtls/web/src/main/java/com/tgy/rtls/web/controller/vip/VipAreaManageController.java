package com.tgy.rtls.web.controller.vip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.vip.VipArea;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.vip.VipAreaService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@Api("VIP区域管理")
public class VipAreaManageController {
    @Autowired
    private VipAreaService vipAreaService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getVipAreaInfo")
    @ApiOperation(value = "获取VIP区域信息", notes = "111")
    public CommonResult<Object> getVipAreaInfo(String license, Long map,String floorName, String barrierGateId, String phone, Integer pageIndex, Integer pageSize, @RequestParam(value = "desc", defaultValue = "start_time desc") String desc, String maps) {
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
            if (license != null) {
                license = license.toUpperCase();
            }
            List<VipArea> data = vipAreaService.getVipAreaInfo(license, map, phone, barrierGateId, desc,floorName, mapids);
            String barrierGateAreaStr = vipAreaService.getBarrierGateInfo();
            PageInfo<VipArea> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("barrierGateAreaStr", barrierGateAreaStr);
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

    @RequiresPermissions({"vaa:add"})
    @RequestMapping(value = "/addVipAreaInfo")
    @ApiOperation(value = "添加区域信息", notes = "111")
    public CommonResult<Object> addVipAreaInfo(@RequestBody VipArea vipArea, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(vipArea.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (!NullUtils.isEmpty(vipArea.getLicense())) {
                vipArea.setLicense(vipArea.getLicense().toUpperCase().trim());
            } else {
                return new CommonResult<>(400, LocalUtil.get("车牌号不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipArea.getVipCustomers())) {
                return new CommonResult<>(400, LocalUtil.get("VIP客户不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipArea.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get("手机号码不能为空！！！"));
            }
            if (vipArea.getStartTime().isAfter(vipArea.getEndTime()) || vipArea.getStartTime().isEqual(vipArea.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("开始时间早于或等于结束时间"));
            }
            if (NullUtils.isEmpty(vipArea.getStartTime()) && NullUtils.isEmpty(vipArea.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("预约时间不能为空！！！"));
            }
            vipAreaService.addVipAreaInfo(vipArea);
            LocalDateTime now = LocalDateTime.now();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.VIP_AREA)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), vipArea);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"vaa:del"})
    @RequestMapping(value = "/delVipAreaInfo/{ids}")
    @ApiOperation(value = "删除区域信息", notes = "111")
    public CommonResult<Object> delVipAreaInfo(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            vipAreaService.delVipAreaInfo(ids.split(","));
            LocalDateTime now = LocalDateTime.now();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.VIP_AREA)), now);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"vaa:edit"})
    @RequestMapping(value = "/editVipAreaInfo")
    @ApiOperation(value = "修改区域信息", notes = "111")
    public CommonResult<Object> editVipAreaInfo(@RequestBody VipArea vipArea,HttpServletRequest request) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(vipArea.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (!NullUtils.isEmpty(vipArea.getLicense())) {
                vipArea.setLicense(vipArea.getLicense().toUpperCase().trim());
            } else {
                return new CommonResult<>(400, LocalUtil.get("车牌号不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipArea.getVipCustomers())) {
                return new CommonResult<>(400, LocalUtil.get("VIP客户不能为空！！！"));
            }
            if (NullUtils.isEmpty(vipArea.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get("手机号码不能为空！！！"));
            }
            if (vipArea.getStartTime().isAfter(vipArea.getEndTime()) || vipArea.getStartTime().isEqual(vipArea.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("开始时间早于或等于结束时间"));
            }
            if (NullUtils.isEmpty(vipArea.getStartTime()) && NullUtils.isEmpty(vipArea.getEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("预约时间不能为空！！！"));
            }
            vipAreaService.editVipAreaInfo(vipArea);
            LocalDateTime now = LocalDateTime.now();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.VIP_AREA)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS), vipArea);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"vaa:see", "vaa:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getVipAreaInfoInfoById/{id}")
    @ApiOperation(value = "查看区域信息", notes = "111")
    public CommonResult<Object> getVipAreaInfoInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            VipArea vipArea = vipAreaService.getVipAreaInfoInfoById(id);
            res.setData(vipArea);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
