package com.tgy.rtls.web.controller.park.floorLock;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/userCompanyMap")
public class UserCompanyMapController {

    @Resource
    private UserCompanyMapService userCompanyMapService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ParkMapper parkMapper;

    @MyPermission
    @RequestMapping(value = "/getUserCompanyMap")
    @ApiOperation(value = "获取公司名", notes = "111")
    public CommonResult<Object> getCarPlate(Integer map, Integer companyId, String userName,String phone,String licensePlate,Integer pageIndex, Integer pageSize,
                                            @RequestParam(value = "desc", defaultValue = "id desc") String desc, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }

            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<UserCompanyMap> data = userCompanyMapService.getUserCompanyMap(map, companyId, userName,phone,licensePlate,desc, mapids);
            List<ParkingCompanyVo> mapComName = parkMapper.getAllMap(mapids);
            PageInfo<UserCompanyMap> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("mapComName", mapComName);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @GetMapping("/getUserCompanyMapById/{id}")
    @RequiresPermissions(value = {"ucm:see","ucm:edit"},logical = Logical.OR)
    public CommonResult<Object> getUserCompanyMapById(@PathVariable("id") Integer id) {
        try {
            UserCompanyMap userCompanyMap = userCompanyMapService.getById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),userCompanyMap);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @DeleteMapping("/delUserCompanyMapById/{ids}")
    @RequiresPermissions("ucm:del")
    public CommonResult<Object> delCarPlateById(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            if( userCompanyMapService.removeBatchByIds(StrUtils.convertStringToList(ids))){
                LocalDateTime now = LocalDateTime.now();
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.cp)), now);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
