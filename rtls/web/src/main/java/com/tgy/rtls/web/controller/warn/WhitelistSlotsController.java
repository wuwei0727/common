package com.tgy.rtls.web.controller.warn;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.warn.WhitelistSlots;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.warn.WhitelistSlotsService;
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
@RequestMapping("/whitelist/")
@RequiredArgsConstructor
public class WhitelistSlotsController {

    private final WhitelistSlotsService service;
    private final OperationlogService operationlogService;
    private final Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @GetMapping("getAllOrFilteredWhitelist")
    public CommonResult<Object> getAllOrFilteredWhitelist(String configName, String status,String mapId, String map, Integer pageIndex, Integer pageSize,
                                                                   @RequestParam(value = "desc", defaultValue = "ws.create_time desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<WhitelistSlots> whitelist = service.getAllOrFilteredWhitelist(configName, status, map,mapId, desc, mapids);
            PageInfo<WhitelistSlots> pageInfo = new PageInfo<>(whitelist);
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


    @PostMapping("addWhitelist")
    @RequiresPermissions("wli:add")
    public CommonResult<Object> addWhitelist(@RequestBody WhitelistSlots whitelistSlots, HttpServletRequest request) {
        try {
            QueryWrapper<WhitelistSlots> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("config_id", whitelistSlots.getConfigId());

            List<WhitelistSlots> list = service.list(queryWrapper);
            if (!NullUtils.isEmpty(list)) {
                return new CommonResult<>(500, LocalUtil.get("报警配置已存在"));
            }
            LocalDateTime now = LocalDateTime.now();
            boolean result = service.save(whitelistSlots);
            if(result){
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.WHITELIST)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping("deleteWhitelistById/{ids}")
    @RequiresPermissions("wli:del")
    public CommonResult<Object> deleteWhitelistById(@PathVariable String ids, HttpServletRequest request) {
        boolean removed = service.removeBatchByIds(StrUtils.convertStringToList(ids));
        LocalDateTime now = LocalDateTime.now();
        if (removed) {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.WHITELIST)), now);
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS), removed);

    }

    @GetMapping("getWhitelistById/{id}")
    @RequiresPermissions(value = {"wli:see","wli:edit"},logical = Logical.OR)
    public CommonResult<Object> getWhitelistById(@PathVariable int id) {
        WhitelistSlots whitelist = service.getWhitelistById(id);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS), whitelist);
    }

    @PostMapping("updateWhitelist")
    @RequiresPermissions("wli:edit")
    public CommonResult<Object> updateWhitelist(@RequestBody WhitelistSlots whitelistSlots, HttpServletRequest request) {
        try {
            QueryWrapper<WhitelistSlots> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("config_id", whitelistSlots.getConfigId());
            queryWrapper.ne("id", whitelistSlots.getId());

            List<WhitelistSlots> list = service.list(queryWrapper);
            if (!NullUtils.isEmpty(list)) {
                return new CommonResult<>(500, LocalUtil.get("报警配置已存在"));
            }
            LocalDateTime now = LocalDateTime.now();
            boolean result = service.updateById(whitelistSlots);
            if(result){
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.WHITELIST)), now);
            }
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

