package com.tgy.rtls.web.controller.equip;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.FloorLockConfig;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.impl.FloorLockConfigService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.equip
 * @Author: wuwei
 * @CreateTime: 2024-06-06 14:28
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/flc")
@CrossOrigin
public class FloorLockConfigController {
    @Resource
    private FloorLockConfigService floorLockConfigService;
    @Resource
    private OperationlogService operationlogService;
    @Resource
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getFloorLockInfo")
    @ApiOperation(value = "获取地锁信息", notes = "111")
    public CommonResult<Object> getFloorLockConfigInfo(String map,
                                                 @RequestParam(value = "pageIndex", defaultValue = "1")Integer pageIndex,
                                                 @RequestParam(value = "pageSize", defaultValue = "12")Integer pageSize,
                                                 @RequestParam(value = "desc", defaultValue = "create_time desc") String desc,String maps) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }

            List<FloorLockConfig> data = floorLockConfigService.getFloorLockConfigInfo(map, null, desc,mapids);
            PageInfo<FloorLockConfig> pageInfo = new PageInfo<>(data);
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

    @RequiresPermissions({"flc:add"})
    @RequestMapping(value = "/addFloorLockConfigInfo")
    @ApiOperation(value = "添加地锁信息", notes = "111")
    public CommonResult<Object> addFloorLockConfigInfo(@RequestBody FloorLockConfig config, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(config.getCallCode())) {
                return new CommonResult<>(400, LocalUtil.get("调用码不能为空！！！"));
            }

            List<FloorLockConfig> floorLockConfigInfo = floorLockConfigService.getFloorLockConfigInfo(String.valueOf(config.getMap()), null, null, null);
            if(!NullUtils.isEmpty(floorLockConfigInfo)){
                return new CommonResult<>(400, LocalUtil.get("当前地图已存在！！！"));
            }
            if (NullUtils.isEmpty(config.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(config.getDataInterface())||!(config.getDataInterface().startsWith("http://")||config.getDataInterface().startsWith("https://"))) {
                return new CommonResult<>(400, LocalUtil.get("请正确填写数据传输接口！！！"));
            }
            if (NullUtils.isEmpty(config.getValidStartTime())||NullUtils.isEmpty(config.getValidEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("有效期时间不能为空！！！"));
            }
            floorLockConfigService.addFloorLockConfigInfo(config);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK_CONFIG)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequiresPermissions({"flc:del"})
    @RequestMapping(value = "/delFloorLockConfigInfo/{ids}")
    @ApiOperation(value = "删除地锁信息", notes = "111")
    public CommonResult<Object> delFloorLockConfigInfo(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            floorLockConfigService.delFloorLockConfigInfo(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK_CONFIG)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"flc:edit"})
    @RequestMapping(value = "/editFloorLockConfigInfo")
    @ApiOperation(value = "添加地锁信息", notes = "111")
    public CommonResult<Object> editFloorLockConfigInfo(@RequestBody FloorLockConfig config, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(config.getCallCode())) {
                return new CommonResult<>(400, LocalUtil.get("调用码不能为空！！！"));
            }
            List<FloorLockConfig> floorLockConfigInfo = floorLockConfigService.getFloorLockConfigInfo(String.valueOf(config.getMap()), config.getId(),null, null);
            if(!NullUtils.isEmpty(floorLockConfigInfo)){
                return new CommonResult<>(400, LocalUtil.get("当前地图已存在！！！"));
            }

            if (NullUtils.isEmpty(config.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(config.getDataInterface())||!(config.getDataInterface().startsWith("http://")||config.getDataInterface().startsWith("https://"))) {
                return new CommonResult<>(400, LocalUtil.get("请正确填写数据传输接口！！！"));
            }
            if (NullUtils.isEmpty(config.getValidStartTime())||NullUtils.isEmpty(config.getValidEndTime())) {
                return new CommonResult<>(400, LocalUtil.get("有效期时间不能为空！！！"));
            }
            floorLockConfigService.updateByPrimaryKeySelective(config);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.FLOOR_LOCK_CONFIG)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"flc:see", "flc:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getFloorLockInfoInfoById/{id}")
    @ApiOperation(value = "查看地锁信息", notes = "111")
    public CommonResult<Object> getFloorLockInfoInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            FloorLockConfig floorLock = floorLockConfigService.getById(id);
            res.setData(floorLock);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/getFloorLockInfoInfoByMapId/{mapId}")
    @ApiOperation(value = "查看地锁信息", notes = "111")
    public CommonResult<Object> getFloorLockInfoInfoByMapId(@PathVariable("mapId") Integer mapId) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            FloorLockConfig floorLock = floorLockConfigService.getOne(new QueryWrapper<FloorLockConfig>().eq("map",mapId));
            res.setData(floorLock);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


}
