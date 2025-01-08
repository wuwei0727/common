package com.tgy.rtls.web.controller.pay;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.pay.SmsQuota;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.SmsQuotaService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
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
 * @BelongsPackage: com.tgy.rtls.web.controller.pay
 * @Author: wuwei
 * @CreateTime: 2023-11-13 14:48
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/smsQuota")
public class SmsQuotaController {

    @Autowired
    private SmsQuotaService smsQuotaService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    @MyPermission
    @GetMapping(value = "/getSmsQuotaOrCondition")
    public CommonResult<Object> getSmsQuotaOrCondition(String map, Integer pageIndex, Integer pageSize,@RequestParam(defaultValue = "sq.id desc") String desc, String maps) {
        String[] mapids = null;
        if (!NullUtils.isEmpty(maps)) {
            mapids = maps.split(",");
        }
        if (pageSize != null && pageSize != -1) {
            PageHelper.startPage(pageIndex, pageSize);
        }
        List<SmsQuota> list = smsQuotaService.getSmsQuotaOrCondition(map,desc,mapids);
        PageInfo<SmsQuota> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("pageIndex", pageIndex);
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
    }


    @RequiresPermissions(value = {"sqa:see","sqa:edit"},logical = Logical.OR)
    @GetMapping (value = "/getSmsQuotaById")
    public CommonResult<Object> getParkLotCostById(@RequestParam Integer id) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),smsQuotaService.getSmsQuotaById(id));
    }

    @RequiresPermissions("sqa:edit")
    @PutMapping(value = "/updateSmsQuota")
    public CommonResult<Object> updateSmsQuota(@RequestBody SmsQuota smsQuota,HttpServletRequest request) {
        SmsQuota quota = smsQuotaService.getSmsQuotaByMap(smsQuota.getMap(),smsQuota.getId());
        if(!NullUtils.isEmpty(quota)){
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.CURRENT_MAP_EXISTS));
        }
        if(smsQuotaService.updateByPrimaryKeySelective(smsQuota)>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.SMS_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }


     @RequiresPermissions({"sqa:del"})
    @DeleteMapping (value = "/delSmsQuota")
    public CommonResult<Object> delSmsQuota(@RequestBody String ids,HttpServletRequest request) {
        if(smsQuotaService.deleteByIdIn(ids.split(","))>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.SMS_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }


    @RequiresPermissions({"sqa:add"})
    @PostMapping (value = "/addSmsQuota")
    public CommonResult<Object> addSmsQuota(@RequestBody SmsQuota smsQuota, HttpServletRequest request) {
        if (NullUtils.isEmpty(smsQuota)) {
            return new CommonResult<>(400, "请正确填写，不能为空");
        }
        SmsQuota quota = smsQuotaService.getSmsQuotaByMap(smsQuota.getMap(),null);
        if(!NullUtils.isEmpty(quota)){
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.CURRENT_MAP_EXISTS));
        }
        if(smsQuotaService.insert(smsQuota)>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.SMS_INFO)), now);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }

}
