package com.tgy.rtls.web.controller.park.floorLock;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.StrUtils;
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
 * @BelongsPackage: com.tgy.rtls.web.controller.park.floorLock
 * @Author: wuwei
 * @CreateTime: 2024-07-16 11:41
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/timePeriod")
public class TimePeriodAdminController {
    @Autowired
    private TimePeriodAdminService timePeriodAdminService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ParkMapper parkMapper;
    @MyPermission
    @RequestMapping(value = "/getTimePeriodAdminInfo")
    @ApiOperation(value = "获取公司名", notes = "111")
    public CommonResult<Object> getTimePeriodAdminInfo(String dayOfWeek, Integer map, String companyId, Integer pageIndex, Integer pageSize,
                                                       @RequestParam(value = "desc",defaultValue = "id desc")String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }

            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<TimePeriodAdmin> data = timePeriodAdminService.getTimePeriodAdminInfo(map, companyId, dayOfWeek, desc,null,mapids);
            List<ParkingCompanyVo> mapComName = parkMapper.getAllMap(mapids);
            PageInfo<TimePeriodAdmin> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("mapComName",mapComName);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions("tp:add")
    @PostMapping("addTimePeriodAdminInfo")
    public CommonResult<Object> addTimePeriodAdminInfo(@RequestBody List<TimePeriodAdmin> timePeriodAdmins, HttpServletRequest request) {
        try {
            Integer mapId = timePeriodAdmins.get(0).getMapId();
            String mapName = timePeriodAdmins.get(0).getMapName();
            List<TimePeriodAdmin> com=null;
            for (TimePeriodAdmin timePeriodAdmin : timePeriodAdmins) {
                com = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId", mapId).eq("day_of_week", timePeriodAdmin.getDayOfWeek()).eq("company_id", timePeriodAdmin.getCompanyId()));
                if(!NullUtils.isEmpty(com)){
                    break;
                }
            }

//            List<TimePeriodAdmin> existingRecords = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId", mapId));
//            if(existingRecords.size() + timePeriodAdmins.size() > 7||!NullUtils.isEmpty(com)){
            if(!NullUtils.isEmpty(com)){
                return new CommonResult<>(400, LocalUtil.get(String.format("所选日期在 %S 已经配置,请检查！！！",mapName)));
            }

            if(timePeriodAdminService.saveBatch(timePeriodAdmins)){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.t_p)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getTimePeriodAdminInfoById/{id}")
    @RequiresPermissions(value = {"tp:see","tp:edit"},logical = Logical.OR)
    public CommonResult<Object> getTimePeriodAdminInfoById(@PathVariable("id") Integer id) {
        try {
            TimePeriodAdmin timePeriodAdmin = timePeriodAdminService.getTimePeriodAdminInfoById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),timePeriodAdmin);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PutMapping("/editTimePeriodAdminInfo")
    @RequiresPermissions("tp:edit")
    public CommonResult<Object> editTimePeriodAdminInfo(@RequestBody TimePeriodAdmin timePeriodAdmin, HttpServletRequest request) {
        try {
            List<TimePeriodAdmin> com = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId", timePeriodAdmin.getMapId())
                    .eq("day_of_week", timePeriodAdmin.getDayOfWeek()).eq("company_id", timePeriodAdmin.getCompanyId()).ne("id",timePeriodAdmin.getId()));

//            List<TimePeriodAdmin> existingRecords = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("mapId", timePeriodAdmin.getMapId()).ne("id",timePeriodAdmin.getId()));
//            if(existingRecords.size() + 1 > 7||!NullUtils.isEmpty(com)){
//                return new CommonResult<>(400, LocalUtil.get(String.format("无法为 %s 添加超过 7 个时间段或某天已经配置,请检查！！！",timePeriodAdmin.getMapName())));
//            }
            if(!NullUtils.isEmpty(com)){
                return new CommonResult<>(400, LocalUtil.get(String.format("所选日期在 %S 已经配置,请检查！！！",timePeriodAdmin.getMapName())));
            }
            if(timePeriodAdminService.updateById(timePeriodAdmin)){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.t_p)), now);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delTimePeriodAdminInfoById/{ids}")
    @RequiresPermissions("tp:del")
    public CommonResult<Object> delTimePeriodAdminInfoById(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            if(timePeriodAdminService.removeBatchByIds(StrUtils.convertStringToList(ids))){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.t_p)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
