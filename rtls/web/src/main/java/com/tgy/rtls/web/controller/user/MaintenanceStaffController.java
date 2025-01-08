package com.tgy.rtls.web.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.MaintenanceStaff;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.user.impl.MaintenanceStaffService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/maintenanceStaff/")
@RequiredArgsConstructor
public class MaintenanceStaffController {

    private final MaintenanceStaffService service;
    private final OperationlogService operationlogService;
    private final Ip2regionSearcher ip2regionSearcher;


    @MyPermission
    @GetMapping("getAllOrFilteredMaintenanceStaff")
    public CommonResult<Object> getAllOrFilteredMaintenanceStaff(String name, String status, String map, String phone, Integer pageIndex, Integer pageSize,
                                                                 @RequestParam(value = "desc", defaultValue = "create_time desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<MaintenanceStaff> maintenanceStaffs = service.getAllOrFilteredMaintenanceStaff(map, name, phone, status, map, desc, mapids);
            PageInfo<MaintenanceStaff> pageInfo = new PageInfo<>(maintenanceStaffs);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("addMaintenanceStaff")
    @RequiresPermissions("mc:add")
    public CommonResult<Object> addMaintenanceStaff(@RequestBody MaintenanceStaff main, HttpServletRequest request) {
        try {
            MaintenanceStaff one = service.getOne(new QueryWrapper<MaintenanceStaff>().eq("phone", main.getPhone()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            LocalDateTime now = LocalDateTime.now();
            boolean result = service.save(main);
            if(result){
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MS)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @DeleteMapping("deleteMaintenanceStaffById/{ids}")
    @RequiresPermissions("mc:del")
    public CommonResult<Object> deleteParkingAlertConfigById(@PathVariable String ids, HttpServletRequest request) {
        boolean removed = service.removeBatchByIds(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        if (removed) {
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MS)), now);
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);
    }

    @GetMapping("getMaintenanceStaffById/{id}")
    @RequiresPermissions(value = {"mc:see","mc:edit"},logical = Logical.OR)
    public CommonResult<Object> getParkingAlertConfigById(@PathVariable int id) {
        MaintenanceStaff main = service.getMaintenanceStaffById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), main);
    }

    @PostMapping("updateMaintenanceStaff")
    @RequiresPermissions("mc:edit")
    public CommonResult<Object> updateMaintenanceStaff(@RequestBody MaintenanceStaff main, HttpServletRequest request) {
        try {
            MaintenanceStaff one = service.getOne(new QueryWrapper<MaintenanceStaff>().eq("phone", main.getPhone()).ne("id", main.getId()));
            if(!NullUtils.isEmpty(one)){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            LocalDateTime now = LocalDateTime.now();
            boolean result = service.updateById(main);
            if(result){
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MS)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
