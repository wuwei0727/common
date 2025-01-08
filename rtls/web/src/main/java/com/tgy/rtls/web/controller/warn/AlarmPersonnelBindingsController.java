package com.tgy.rtls.web.controller.warn;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.MaintenanceStaff;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.warn.AlarmPersonnelBindings;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.user.impl.MaintenanceStaffService;
import com.tgy.rtls.data.service.warn.AlarmPersonnelBindingsService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/apb/")
@RequiredArgsConstructor
public class AlarmPersonnelBindingsController {

    private final AlarmPersonnelBindingsService service;
    private final MaintenanceStaffService mainSevice;
    private final OperationlogService operationlogService;
    private final Ip2regionSearcher ip2regionSearcher;
    
    
    @MyPermission
    @GetMapping("getAllOrFilteredAlarmPersonnelBindings")
    public CommonResult<Object> getAllOrFilteredAlarmPersonnelBindings(String map, Integer pageIndex, Integer pageSize,@RequestParam(value = "desc", defaultValue = "create_time desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<AlarmPersonnelBindings> cameras = service.getAllOrFilteredAlarmPersonnelBindings(map, desc, mapids);
            PageInfo<AlarmPersonnelBindings> pageInfo = new PageInfo<>(cameras);
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

    @PostMapping("addAlarmPersonnelBindings")
    @RequiresPermissions("pac:add")
    public CommonResult<Object> addAlarmPersonnelBindings(@RequestBody AlarmPersonnelBindings apb, HttpServletRequest request) throws Exception {
        try {
            // 1. 解析所有ID字符串
            List<String> staffIds = splitAndTrim(apb.getMaintenanceStaffId());
            List<String> alarmTypeIds = splitAndTrim(apb.getAlarmTypeId());
            List<String> levelIds = splitAndTrim(apb.getLevel());



            CommonResult<Object> checkResult = checkMaintenanceStaffExists(apb.getMap(), staffIds,null);
            if (checkResult != null) {
                return checkResult;
            }
            for (String staffId : staffIds) {
                // 2. 保存主表数据
                AlarmPersonnelBindings binding = createBinding(apb,staffId);
                service.saveBindingWithMappings(binding,alarmTypeIds,levelIds);
            }
            recordOperationLog(request);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY),e);
        }
    }


    @DeleteMapping("deleteAlarmPersonnelBindingsById/{ids}")
    @RequiresPermissions("apb:del")
    public CommonResult<Object> deleteAlarmPersonnelBindingsById(@PathVariable String ids, HttpServletRequest request) {
        boolean removed = service.removeBindingsWithMappings(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
        if (removed) {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.APB)), now);
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);

    }


    @GetMapping("getParkingAlertConfigById/{id}")
    @RequiresPermissions(value = {"apb:see","apb:edit"},logical = Logical.OR)
    public CommonResult<Object> getAlarmPersonnelBindingsById(@PathVariable int id) {
        AlarmPersonnelBindings parkingAlertConfig = service.getAlarmPersonnelBindingsById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), parkingAlertConfig);
    }

    @PostMapping("updateParkingAlertConfig")
    @RequiresPermissions("apb:edit")
    public CommonResult<Object> updateParkingAlertConfig(@RequestBody AlarmPersonnelBindings apb, HttpServletRequest request) {
        try {
            List<String> staffIds = splitAndTrim(apb.getMaintenanceStaffId());
            List<String> alarmTypeIds = splitAndTrim(apb.getAlarmTypeId());
            List<String> levelIds = splitAndTrim(apb.getLevel());

            CommonResult<Object> checkResult = checkMaintenanceStaffExists(apb.getMap(), staffIds, apb.getId());
            if (checkResult != null) {
                return checkResult;
            }
            for (String staffId : staffIds) {
                // 3. 更新数据
                AlarmPersonnelBindings binding = createBinding(apb,staffId);
                service.updateBindingWithMappings(binding, alarmTypeIds, levelIds);
            }

            recordOperationLog(request);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY),e);
        }
    }


    private List<String> splitAndTrim(String ids) {
        if (StringUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        return Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private CommonResult<Object> checkMaintenanceStaffExists(String map, List<String> staffIds, Long excludeId) {
        for (String staffId : staffIds) {
            boolean exists = excludeId == null ?
                    service.checkStaffExists(map, staffId) :
                    service.checkStaffExistsExclude(map, staffId, excludeId);
            if (exists) {
                MaintenanceStaff staff = mainSevice.getById(staffId);
                return new CommonResult<>(400, "维护人员: " + staff.getName() + " 已存在");
            }
        }
        return null;
    }

    // 创建主表记录
    private AlarmPersonnelBindings createBinding(AlarmPersonnelBindings dto, String staffId) {
        AlarmPersonnelBindings binding = new AlarmPersonnelBindings();
        binding.setId(dto.getId());
        binding.setMap(dto.getMap());
        binding.setMaintenanceStaffId(staffId);
        return binding;
    }


    // 记录操作日志
    private void recordOperationLog(HttpServletRequest request) {
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        String ip = IpUtil.getIpAddr(request);
        String address = ip2regionSearcher.getAddressAndIsp(ip);
        operationlogService.addUserOperationlog(
                member.getUid(),
                ip.concat(address == null ? "" : address),
                KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.APB)),
                LocalDateTime.now()
        );
    }
}


