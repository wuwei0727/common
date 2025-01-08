package com.tgy.rtls.web.controller.equip;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2020/10/16
 * 标签管理类
 */

@RestController
@RequestMapping(value = "/infrared")
@CrossOrigin
/**
 *车位检测器
 */
public class InfraredController {
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    LocalUtil localUtil;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MyPermission
    @RequestMapping(value = "/getInfraredSel")
    @ApiOperation(value = "车位检测器查询接口", notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "num", value = "卡号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "networkstate", value = "网络状态", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "relevance", value = "是否绑定地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "map", value = "地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getInfraredSel(String num, Integer networkstate,Integer power, Integer map, String name, Integer relevance,
                                          @RequestParam(value = "desc", defaultValue = "addTime desc") String desc,
                                          @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,String floorName,
                                          @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize, String placeName, Integer status,String infraredName, String maps) {
        try {
            String uid = "";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            //按条件查询
            //pageSize<0时查询所有
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize < 0) {
                List<Infrared> infrareds = tagMapper.findByAllInfrared2(num, networkstate,power, relevance, map, desc, localUtil.getLocale(), name, placeName, status,infraredName,floorName,mapids);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), infrareds);
            }

            PageHelper.startPage(pageIndex, pageSize);
            List<Infrared> infrareds = tagMapper.findByAllInfrared2(num, networkstate,power, relevance, map, desc, localUtil.getLocale(), name, placeName, status,infraredName,floorName,mapids);
            PageInfo<Infrared> pageInfo = new PageInfo<>(infrareds);
            Map<String, Object> resMap = new HashMap<>();
            resMap.put("list", pageInfo.getList());
            resMap.put("pageIndex", pageIndex);
            resMap.put("total", pageInfo.getTotal());
            resMap.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), resMap);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"ir:see","ir:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getInfraredId/{id}")
    @ApiOperation(value = "车位检测器详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "车位检测器id", required = true, dataType = "int")
    public CommonResult<Infrared> getTagId(@PathVariable("id") Integer id) {
        try {
            List<Infrared> res = tagMapper.findIredByIdAndName(id, null, null);
            Infrared data = null;
            if (res != null && res.size() > 0) {
                data = res.get(0);
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), data);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"ir:del"})
    @RequestMapping(value = "/delInfrared/{ids}")
    @ApiOperation(value = "uwb网关删除接口", notes = "uwb网关id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "uwb网关id集", required = true, dataType = "String")
    public CommonResult delSub(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            LocalDateTime now = LocalDateTime.now();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }

            String[]  placeId= tagMapper.getInfraredPlace(ids.split(","));

            if (tagMapper.delInfrared(ids.split(",")) > 0) {
                if (!NullUtils.isEmpty(placeId)) {
                        tagMapper.updatePlace(placeId,1);
                } else {
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.DETECTOR_INFO)), now);
                    return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
                }
                eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, 3));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.DETECTOR_INFO)), now);
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @PostMapping("/updateLifetime")
    @RequiresPermissions(value = "ir:udl")
    @ApiOperation(value = "更新地图下所有检测器的使用年限", notes = "根据地图ID更新所有检测器的使用年限（月）")
    public CommonResult<Object> updateDetectorLifetime(
                            @ApiParam(value = "地图ID", required = true) @RequestParam Integer map,
                            @ApiParam(value = "使用年限（月）", required = true) @RequestParam Integer lifetimeMonths) {
        try {
            // 参数验证
            if (map == null || lifetimeMonths == null) {
                return new CommonResult<>(400, "参数不能为空");
            }

            if (lifetimeMonths <= 0) {
                return new CommonResult<>(400, "使用年限必须大于0");
            }

            return new CommonResult<>(200, "更新成功",tagMapper.updateLifetimeByMap(map, lifetimeMonths));
        } catch (Exception e) {
            return new CommonResult<>(500, "更新失败：" + e.getMessage());
        }
    }
}
