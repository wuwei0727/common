package com.tgy.rtls.web.controller.warn;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.warn.ConfigTimePeriods;
import com.tgy.rtls.data.entity.warn.ParkingAlertConfig;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.warn.ParkingAlertConfigService;
import com.tgy.rtls.data.service.warn.impl.ConfigTimePeriodsService;
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
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pac/")
@RequiredArgsConstructor
public class ParkingAlertConfigController {

    private final ParkingAlertConfigService service;
    private final ConfigTimePeriodsService configTimePeriodsService;
    private final OperationlogService operationlogService;
    private final Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @GetMapping("getAllOrFilteredParkingAlertConfig")
    public CommonResult<Object> getAllOrFilteredParkingAlertConfig(String configName, String status, String map, Integer pageIndex, Integer pageSize,
                                                                   @RequestParam(value = "desc", defaultValue = "create_time desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<ParkingAlertConfig> cameras = service.getAllOrFilteredParkingAlertConfig(configName, status, map, desc, mapids);
            PageInfo<ParkingAlertConfig> pageInfo = new PageInfo<>(cameras);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            log.info("getAllOrFilteredParkingAlertConfig → configName={},status={},map={},desc={},maps={}", configName, status, map, desc, maps);
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("addParkingAlertConfig")
    @RequiresPermissions("pac:add")
    public CommonResult<Object> addParkingAlertConfig(@RequestBody ParkingAlertConfig config, HttpServletRequest request) {
        try {
            List<ParkingAlertConfig> list = service.list(new QueryWrapper<ParkingAlertConfig>().eq("map", config.getMap()));
            if (!NullUtils.isEmpty(list)) {
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.TIME_EXIST));
            }

            LocalDateTime now = LocalDateTime.now();
            boolean result = service.save(config);
            if(result){
                Long configId = config.getId();

                List<String> t2TimePeriods = config.getT2TimePeriods();
                List<String> t3TimePeriods = config.getT3TimePeriods();

                // 将 T2 时间段保存到 config_time_periods 表
                if (t2TimePeriods != null) {
                    List<ConfigTimePeriods> existingT2Periods = configTimePeriodsService.list(new QueryWrapper<ConfigTimePeriods>().eq("config_id", configId).eq("period_type", "t2"));
                    for (String period : t2TimePeriods) {
                        String[] times = period.split("~");

                        LocalTime newStart = LocalTime.parse(times[0]);
                        LocalTime newEnd = LocalTime.parse(times[1]);

                        if (isTimePeriodOverlap(existingT2Periods, newStart, newEnd)) {
                            return new CommonResult<>(500, "时间段 " + period + " 重叠，请检查输入。");
                        }

                        ConfigTimePeriods timePeriod = new ConfigTimePeriods();
                        timePeriod.setConfigId(configId);
                        timePeriod.setStartTime(LocalTime.parse(times[0]));
                        timePeriod.setEndTime(LocalTime.parse(times[1]));
                        timePeriod.setPeriodType("t2");
                        configTimePeriodsService.save(timePeriod);
                    }
                }

                // 将 T3 时间段保存到 config_time_periods 表
                if (t3TimePeriods != null) {
                    List<ConfigTimePeriods> existingT2Periods = configTimePeriodsService.list(new QueryWrapper<ConfigTimePeriods>().eq("config_id", configId).eq("period_type", "t3"));

                    for (String period : t3TimePeriods) {
                        String[] times = period.split("~");

                        LocalTime newStart = LocalTime.parse(times[0]);
                        LocalTime newEnd = LocalTime.parse(times[1]);

                        if (isTimePeriodOverlap(existingT2Periods, newStart, newEnd)) {
                            return new CommonResult<>(500, "时间段 " + period + " 重叠，请检查输入。");
                        }

                        ConfigTimePeriods timePeriod = new ConfigTimePeriods();
                        timePeriod.setConfigId(configId);
                        timePeriod.setStartTime(LocalTime.parse(times[0]));
                        timePeriod.setEndTime(LocalTime.parse(times[1]));
                        timePeriod.setPeriodType("t3");
                        configTimePeriodsService.save(timePeriod);
                    }
                }

                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PARKINGALERTCONFIG)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @DeleteMapping("deleteParkingAlertConfigById/{ids}")
    @RequiresPermissions("pac:del")
    public CommonResult<Object> deleteParkingAlertConfigById(@PathVariable String ids, HttpServletRequest request) {
        boolean removed = service.removeBatchByIds(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
        if (removed) {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PARKINGALERTCONFIG)), now);
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);

    }


    @GetMapping("getParkingAlertConfigById/{id}")
    @RequiresPermissions(value = {"pac:see","pac:edit"},logical = Logical.OR)
    public CommonResult<Object> getParkingAlertConfigById(@PathVariable int id) {
        ParkingAlertConfig parkingAlertConfig = service.getParkingAlertConfigById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), parkingAlertConfig);
    }

    @PostMapping("updateParkingAlertConfig")
    @RequiresPermissions("pac:edit")
    public CommonResult<Object> updateParkingAlertConfig(@RequestBody ParkingAlertConfig config, HttpServletRequest request) {
        try {
            List<ParkingAlertConfig> list = service.list(new QueryWrapper<ParkingAlertConfig>().eq("map", config.getMap()).ne("id", config.getId()));
            if(!NullUtils.isEmpty(list)){
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.TIME_EXIST));
            }
            LocalDateTime now = LocalDateTime.now();
            boolean result = service.updateById(config);
            if(result){
                Long configId = config.getId();

                // 删除该配置的旧时间段数据
                QueryWrapper<ConfigTimePeriods> timePeriodQuery = new QueryWrapper<>();
                timePeriodQuery.eq("config_id", configId);
                configTimePeriodsService.remove(timePeriodQuery);

                // 获取 T2 和 T3 时间段列表并保存到 config_time_periods 表
                List<String> t2TimePeriods = config.getT2TimePeriods(); // 假设已定义为 List<String>
                List<String> t3TimePeriods = config.getT3TimePeriods(); // 假设已定义为 List<String>

                // 将 T2 时间段保存到 config_time_periods 表
                if (t2TimePeriods != null) {
                    List<ConfigTimePeriods> existingT2Periods = configTimePeriodsService.list(new QueryWrapper<ConfigTimePeriods>().eq("config_id", configId).eq("period_type", "t2"));

                    for (String period : t2TimePeriods) {
                        String[] times = period.split("~");

                        LocalTime newStart = LocalTime.parse(times[0]);
                        LocalTime newEnd = LocalTime.parse(times[1]);

                        if (isTimePeriodOverlap(existingT2Periods, newStart, newEnd)) {
                            return new CommonResult<>(500, "时间段 " + period + " 重叠，请检查输入。");
                        }

                        ConfigTimePeriods timePeriod = new ConfigTimePeriods();
                        timePeriod.setConfigId(configId);
                        timePeriod.setStartTime(LocalTime.parse(times[0]));
                        timePeriod.setEndTime(LocalTime.parse(times[1]));
                        timePeriod.setPeriodType("t2");
                        configTimePeriodsService.save(timePeriod);
                    }
                }

                // 将 T3 时间段保存到 config_time_periods 表
                if (t3TimePeriods != null) {
                    List<ConfigTimePeriods> existingT2Periods = configTimePeriodsService.list(new QueryWrapper<ConfigTimePeriods>().eq("config_id", configId).eq("period_type", "t3"));

                    for (String period : t3TimePeriods) {
                        String[] times = period.split("~");

                        LocalTime newStart = LocalTime.parse(times[0]);
                        LocalTime newEnd = LocalTime.parse(times[1]);

                        if (isTimePeriodOverlap(existingT2Periods, newStart, newEnd)) {
                            return new CommonResult<>(500, "时间段 " + period + " 重叠，请检查输入。");
                        }

                        ConfigTimePeriods timePeriod = new ConfigTimePeriods();
                        timePeriod.setConfigId(configId);
                        timePeriod.setStartTime(LocalTime.parse(times[0]));
                        timePeriod.setEndTime(LocalTime.parse(times[1]));
                        timePeriod.setPeriodType("t3");
                        configTimePeriodsService.save(timePeriod);
                    }
                }
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.PARKINGALERTCONFIG)), now);
            }
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // 检查时间段是否重叠的方法
    private boolean isTimePeriodOverlap(List<ConfigTimePeriods> existingTimePeriods, LocalTime newStartTime, LocalTime newEndTime) {
        for (ConfigTimePeriods period : existingTimePeriods) {
            LocalTime existingStart = period.getStartTime();
            LocalTime existingEnd = period.getEndTime();

            // 检查新时间段是否与已有时间段重叠
            if ((newStartTime.isBefore(existingEnd) && newEndTime.isAfter(existingStart))) {
                return true;
            }
        }
        return false;
    }

}
