package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingExit;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingExitService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
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
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2022-11-24 18:01
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/park")
public class PlaceExitController {
    @Autowired
    private ParkingExitService parkingExitService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    @MyPermission
    @RequestMapping(value = "/getPlaceExit")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlaceExit(String name, Integer pageIndex, Integer pageSize,String floorName,@RequestParam(value = "desc",defaultValue = "id desc") String desc, Integer map,Integer type,String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<ParkingExit> data = parkingExitService.getPlaceExit(name,map,type,floorName,desc,mapids);
            PageInfo<ParkingExit> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            if (pageSize != null && pageSize != -1) {
                res.setData(result);
            } else {
                res.setData(data);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/getPlaceExit2")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlaceExit2(String name, Integer map,String floorName,Integer type) {
        try {
            List<ParkingExit> data = parkingExitService.getPlaceExit(name,map,type,floorName, null, null);
            PageInfo<ParkingExit> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"pet:add"})
    @RequestMapping(value = "/addPlaceExit")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> addPlaceExit(@RequestBody ParkingExit parkingExit, HttpServletRequest request) {
        try {
            parkingExitService.addPlaceExit(parkingExit);
             LocalDateTime now = LocalDateTime.now();
             Member member = (Member) SecurityUtils.getSubject().getPrincipal();
             String ip = IpUtil.getIpAddr(request);
             String address = ip2regionSearcher.getAddressAndIsp(ip);
             operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.GARAGE_ENTRANCE)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("pet:edit")
    @RequestMapping(value = "/editPlaceExitById")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> editPlaceExitById(@RequestBody ParkingExit parkingExit, HttpServletRequest request) {
        try {
             parkingExitService.editPlaceExitById(parkingExit);
             LocalDateTime now = LocalDateTime.now();
             Member member = (Member) SecurityUtils.getSubject().getPrincipal();
             String ip = IpUtil.getIpAddr(request);
             String address = ip2regionSearcher.getAddressAndIsp(ip);
             operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.GARAGE_ENTRANCE)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"pet:see","pet:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getPlaceExitById/{id}")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getPlaceExitById(@PathVariable("id") Integer id) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            ParkingExit data = parkingExitService.getPlaceExitById(id);
            res.setData(data);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"pet:del"})
    @RequestMapping(value = "/delPlaceExit/{ids}")
    @ApiOperation(value = "删除车位出入口信息", notes = "111")
    public CommonResult<Object> delPlaceExit(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            parkingExitService.delPlaceExit(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.GARAGE_ENTRANCE)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
}
