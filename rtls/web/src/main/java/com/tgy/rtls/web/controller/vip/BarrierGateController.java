package com.tgy.rtls.web.controller.vip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.vip.BarrierGate;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.vip.BarrierGateService;
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
 * @CreateTime: 2023-04-06 09:06
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/vip")
@Slf4j
@Api("道闸管理")
public class BarrierGateController {
    @Autowired
    private BarrierGateService barrierGateService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getBarrierGateInfo")
    @ApiOperation(value = "获取道闸信息", notes = "111")
    public CommonResult<Object> getBarrierGateInfo(String barrierGateNum, Long map,String barrierGateId,Integer state,String floorName,Integer pageIndex, Integer pageSize,
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

            List<BarrierGate> data = barrierGateService.getBarrierGateInfo(map,barrierGateId,barrierGateNum,state,desc,floorName,mapids);
            PageInfo<BarrierGate> pageInfo = new PageInfo<>(data);
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

    @RequiresPermissions({"ba:add"})
    @RequestMapping(value = "/addBarrierGateInfo")
    @ApiOperation(value = "添加道闸信息", notes = "111")
    public CommonResult<Object> addBarrierGateInfo(@RequestBody BarrierGate barrierGate, HttpServletRequest request) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            List<BarrierGate> conditionData1 = barrierGateService.getConditionData(barrierGate.getDeviceNum(), null, null);
            if(!NullUtils.isEmpty(conditionData1)){
                return new CommonResult<>(400, LocalUtil.get("该设备编号已绑定道闸"));
            }

            List<BarrierGate> conditionData = barrierGateService.getConditionData(null, barrierGate.getBindingArea(), null);
            if(!NullUtils.isEmpty(conditionData)){
                return new CommonResult<>(400, LocalUtil.get("该区域已绑定道闸"));
            }
            if(NullUtils.isEmpty(barrierGate.getMap())){
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if(NullUtils.isEmpty(barrierGate.getDeviceNum())){
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if(NullUtils.isEmpty(barrierGate.getBindingArea())){
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            barrierGateService.addBarrierGateInfo(barrierGate);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.BARRIER_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),barrierGate);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"ba:del"})
    @RequestMapping(value = "/delBarrierGateInfo/{ids}")
    @ApiOperation(value = "删除道闸信息", notes = "111")
    public CommonResult<Object> delBarrierGateInfo(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            barrierGateService.delBarrierGateInfo(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.BARRIER_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"ba:edit"})
    @RequestMapping(value = "/editBarrierGateInfo")
    @ApiOperation(value = "修改道闸信息", notes = "111")
    public CommonResult<Object> editBarrierGateInfo(@RequestBody BarrierGate barrierGate,HttpServletRequest request) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            List<BarrierGate> conditionData = barrierGateService.getConditionData(barrierGate.getDeviceNum(), barrierGate.getBindingArea(), String.valueOf(barrierGate.getId()));
            if(!NullUtils.isEmpty(conditionData)){
                return new CommonResult<>(400, LocalUtil.get("当前设备编号或绑定区域" + "已经在" + conditionData.get(0).getMapName() + "存在，" + "请勿重复添加！！！"));
            }
            if(NullUtils.isEmpty(barrierGate.getMap())){
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if(NullUtils.isEmpty(barrierGate.getDeviceNum())){
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if(NullUtils.isEmpty(barrierGate.getBindingArea())){
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            barrierGateService.editBarrierGateInfo(barrierGate);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.BARRIER_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS),barrierGate);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"ba:see","ba:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getBarrierGateInfoInfoById/{id}")
    @ApiOperation(value = "查看道闸信息", notes = "111")
    public CommonResult<Object> getBarrierGateInfoInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            BarrierGate barrierGate = barrierGateService.getBarrierGateInfoInfoById(id);
            res.setData(barrierGate);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
}
