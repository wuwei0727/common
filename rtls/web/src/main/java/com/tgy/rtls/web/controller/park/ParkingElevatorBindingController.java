package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingElevatorBinding;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingElevatorBindingService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
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
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-09-12 09:49
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/peb")
@CrossOrigin
public class ParkingElevatorBindingController {
    @Autowired
    private ParkingElevatorBindingService parkingElevatorBindingService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getParkingElevatorBinding")
    public CommonResult<Object> getParkingElevatorBinding(String name,Integer map,String objectType,String building,Integer floor,String placeName, String floorName,Integer pageIndex,Integer pageSize,
                                         @RequestParam(value ="desc",defaultValue="peb.id")String desc, String maps) {
        try {
            if(pageSize<0){
                List<ParkingElevatorBinding> list = parkingElevatorBindingService.getByConditions(name,map,null,null,null,null,floorName,objectType,null,null);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), list);
            }
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<ParkingElevatorBinding> list = parkingElevatorBindingService.getByConditions(name,map,building,floor,placeName,desc,floorName,objectType,mapids,null);
            PageInfo<ParkingElevatorBinding> pageInfo = new PageInfo<>(list);
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

    @RequiresPermissions("peb:add")
    @RequestMapping(value = "/addParkingElevatorBinding")
    public CommonResult<Object> addParkingElevatorBinding(@RequestBody ParkingElevatorBinding peb, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if(parkingElevatorBindingService.addParkingElevatorBinding(peb)){
                LocalDateTime now = LocalDateTime.now();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PEB_INFO)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("peb:edit")
    @RequestMapping(value = "/updateParkingElevatorBinding")
    public CommonResult<Object> updateParkingElevatorBinding(@RequestBody ParkingElevatorBinding peb, HttpServletRequest request) {
        try {

            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if(parkingElevatorBindingService.updateParkingElevatorBinding(peb)){
                LocalDateTime now = LocalDateTime.now();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.PEB_INFO)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"peb:see","peb:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getParkingElevatorBindingById/{id}")
    public CommonResult<Object> getParkingElevatorBindingById(@PathVariable("id") String id) {
        try {
            List<ParkingElevatorBinding> list = parkingElevatorBindingService.getParkingElevatorBindingById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),list);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"peb:del"})
    @RequestMapping(value = "/delParkingElevatorBinding")
    public CommonResult<Object> delParkingElevatorBinding(@RequestBody ParkingElevatorBinding parkingElevatorBinding,HttpServletRequest request) {
        try {
            String[] placeId = null;
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if(parkingElevatorBinding.getPlaceId()!=null){
                placeId = parkingElevatorBinding.getPlaceId().split(",");
            }
            if(parkingElevatorBindingService.delParkingElevatorBinding(parkingElevatorBinding.getIds().split(","),request, placeId)){
                LocalDateTime now = LocalDateTime.now();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PEB_INFO)), now);

            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}