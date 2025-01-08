package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.CrossLevelCorridor;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.impl.CrossLevelCorridorService;
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
 * @CreateTime: 2024-01-04 16:03
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/crossLevelCorridor")
public class CrossLevelCorridorController {
    @Autowired
    private CrossLevelCorridorService crossLevelCorridorService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    @GetMapping("getAllOrConditionalQuery")
    @MyPermission
    public CommonResult<Object> getAllOrConditionalQuery(String name, Integer pageIndex, Integer pageSize,String floorName, Integer map,Integer type,@RequestParam(value = "desc",defaultValue = "id desc") String desc,String maps) {
        try {
            String[] mapIds = null;
            if (!NullUtils.isEmpty(maps)) {
                mapIds = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<CrossLevelCorridor> data = crossLevelCorridorService.getAllOrConditionalQuery(name,map,type,floorName,desc,mapIds);
            PageInfo<CrossLevelCorridor> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @GetMapping("/getConditionalQuery2")
    public CommonResult<Object> getConditionalQuery2(String name, Integer map,String floorName,Integer type) {
        try {

            List<CrossLevelCorridor> data = crossLevelCorridorService.getAllOrConditionalQuery(name,map,type, floorName, null, null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),data);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PostMapping("addCrossLevelCorridor")
    @RequiresPermissions("clc:add")
    public CommonResult<Object> addCrossLevelCorridor(@RequestBody CrossLevelCorridor crossLevelCorridor, HttpServletRequest request) {
        try {

            if(crossLevelCorridorService.insertSelective(crossLevelCorridor)>1){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.C_L_C)), now);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @GetMapping("/getConditionalById/{id}")
    @RequiresPermissions(value = {"clc:see","clc:edit"},logical = Logical.OR)
    public CommonResult<Object> getConditionalById(@PathVariable("id") Integer id) {
        try {
            CrossLevelCorridor crossLevelCorridor = crossLevelCorridorService.getConditionalById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),crossLevelCorridor);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @PutMapping("/editCrossLevelCorridor")
    @RequiresPermissions("clc:edit")
    public CommonResult<Object> editCrossLevelCorridor(@RequestBody CrossLevelCorridor crossLevelCorridor, HttpServletRequest request) {
        try {
            if(crossLevelCorridorService.updateByPrimaryKeySelective(crossLevelCorridor)>1){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.C_L_C)), now);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delConditionalById/{ids}")
     @RequiresPermissions("clc:del")
    public CommonResult<Object> delConditionalById(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            if(crossLevelCorridorService.delConditionalById(ids.split(","))>1){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.C_L_C)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
