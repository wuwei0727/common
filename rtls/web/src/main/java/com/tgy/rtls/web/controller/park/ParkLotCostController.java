package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingLotCost;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingLotCostService;
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
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-11-06 14:14
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping (value = "/parkingLotCost")
public class ParkLotCostController {
    @Autowired
    private ParkingLotCostService parkingLotCostService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    @MyPermission
    @GetMapping (value = "/getParkLotCostOrCondition")
    public CommonResult<Object> getParkLotCostOrCondition(Integer map,Integer pageIndex,Integer pageSize,String maps) {
        String[] mapids = null;
        if (!NullUtils.isEmpty(maps)) {
            mapids = maps.split(",");
        }
        if (pageSize != null && pageSize != -1) {
            PageHelper.startPage(pageIndex, pageSize);
        }
        List<ParkingLotCost> list = parkingLotCostService.selectAllByMap(map,mapids);
        PageInfo<ParkingLotCost> pageInfo = new PageInfo<>(list);
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageInfo.getList());
        result.put("pageIndex", pageIndex);
        result.put("total", pageInfo.getTotal());
        result.put("pages", pageInfo.getPages());
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
    }

    @RequiresPermissions(value = {"plt:see","plt:edit"},logical = Logical.OR)
    @GetMapping (value = "/getParkLotCostById")
    public CommonResult<Object> getParkLotCostById(@RequestParam Integer id) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),parkingLotCostService.getParkLotCostById(id, null,null));
    }

     @RequiresPermissions({"plt:edit"})
    @PutMapping (value = "/updateParkLotCost")
    public CommonResult<Object> updateParkLotCost(@RequestBody ParkingLotCost parkingLotCost,HttpServletRequest request) {
         ParkingLotCost cost = parkingLotCostService.getParkLotCostById(parkingLotCost.getId(), parkingLotCost.getMap(),"edit");
         if(!NullUtils.isEmpty(cost)){
             return new CommonResult<>(400, LocalUtil.get(KafukaTopics.CURRENT_MAP_EXISTS), null);
         }
         if(parkingLotCostService.updateByPrimaryKeySelective(parkingLotCost)>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.C_L_C)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }


     @RequiresPermissions({"plt:del"})
    @DeleteMapping (value = "/delParkLotCost")
    public CommonResult<Object> delParkLotCost(@RequestBody String ids,HttpServletRequest request) {
        if(parkingLotCostService.deleteByIdIn(ids.split(","))>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.P_L_C_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }


     @RequiresPermissions({"plt:add"})
    @PostMapping (value = "/addParkLotCost")
    public CommonResult<Object> addParkLotCost(@RequestBody ParkingLotCost parkingLotCost, HttpServletRequest request) {
        if (NullUtils.isEmpty(parkingLotCost)) {
            return new CommonResult<>(400, "请正确填写，不能为空", null);
        }
         List<ParkingLotCost> list = parkingLotCostService.selectAllByMap(parkingLotCost.getMap(), null);
         if(!NullUtils.isEmpty(list)){
             return new CommonResult<>(400, LocalUtil.get(KafukaTopics.CURRENT_MAP_EXISTS), null);
         }
         if(parkingLotCostService.insert(parkingLotCost)>0){
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.P_L_C_INFO)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        }
        return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }
}
