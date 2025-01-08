package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.RecommConfig;
import com.tgy.rtls.data.entity.park.RecommConfigArea;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.RecommConfigMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.RecommConfigService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
 * @CreateTime: 2023-05-12 10:08
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/recommConfig")
@Slf4j
@Api("推荐配置管理")
public class RecommConfigController {
    @Autowired
    private RecommConfigService recommConfigService;
    @Autowired
    private RecommConfigMapper recommConfigMapper;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getAllRecommConfigOrConditionQuery")
    @ApiOperation(value = "获取推荐配置信息", notes = "111")
    public CommonResult<Object> getAllRecommConfigOrConditionQuery(String map, String areaname, String recommelevel, Integer pageIndex, Integer pageSize,
                                                                   @RequestParam(value = "desc", defaultValue = "starttime desc") String desc, String maps) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<RecommConfig> data = recommConfigService.getAllRecommConfigOrConditionQuery(map, areaname, recommelevel, desc, mapids);
            PageInfo<RecommConfig> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            res.setData(result);
            operationlogService.addOperationlog(member.getUid(), LocalUtil.get(KafukaTopics.QUERY_PERSONPERMISSION));
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"rcg:add"})
    @RequestMapping(value = "/addRecommConfig")
    @ApiOperation(value = "添加推荐配置信息", notes = "111")
    public CommonResult<Object> addRecommConfig(@RequestBody RecommConfig recommConfig, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            RecommConfigArea area = new RecommConfigArea();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(recommConfig.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }

            if (NullUtils.isEmpty(recommConfig.getAreaname())) {
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            if (NullUtils.isEmpty(recommConfig.getRecommelevel())) {
                return new CommonResult<>(400, LocalUtil.get("推荐级别不能为空！！！"));
            }
            recommConfig.setStarttime(LocalDateTime.now());

            if (NullUtils.isEmpty(recommConfig.getVertexInfo())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.COORDINATES_IS_EMPTY));
            }
            if (NullUtils.isEmpty(recommConfig.getPlaceList())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PARKING_SPACE_IS_EMPTY));
            }
            if (!NullUtils.isEmpty(recommConfig.getPlaceList())) {
                for (String placeName : recommConfig.getPlaceList().split(",")) {
                    ParkingPlace placeByPlaceNames = parkingService.getPlaceByPlaceNames(Integer.valueOf(recommConfig.getMap()), placeName.replaceAll("\\(.*?\\)", ""),null,null);
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        List<RecommConfig> recommConfig1 = recommConfigService.getRecommConfigById(null, String.valueOf(placeByPlaceNames.getId()),null);
                        if(!recommConfig1.isEmpty()){
                            return new CommonResult<>(400, LocalUtil.get("请检查列表的车位是否重复配置！！！"));
                        }
                    }
                }
                recommConfigService.addRecommConfig(recommConfig);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            }
//            String ip = IpUtil.getIpAddr(request);
//            String address = ip2regionSearcher.getAddressAndIsp(ip);
//            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.RECOMM_CONFIG)), now);
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"rcg:del"})
    @RequestMapping(value = "/delRecommConfig/{ids}")
    @ApiOperation(value = "删除推荐配置", notes = "111")
    public CommonResult<Object> delRecommConfig(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            recommConfigService.delRecommConfig(ids.split(","));
            LocalDateTime now = LocalDateTime.now();
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.RECOMM_CONFIG)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
    @RequiresPermissions({"rcg:edit"})
    @RequestMapping(value = "/updateRecommConfig")
    @ApiOperation(value = "修改推荐配置信息", notes = "111")
    public CommonResult<Object> updateRecommConfig(@RequestBody RecommConfig recommConfig,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(recommConfig.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }

            if (NullUtils.isEmpty(recommConfig.getAreaname())) {
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            if (NullUtils.isEmpty(recommConfig.getRecommelevel())) {
                return new CommonResult<>(400, LocalUtil.get("推荐级别不能为空！！！"));
            }
            if (NullUtils.isEmpty(recommConfig.getVertexInfo())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.COORDINATES_IS_EMPTY));
            }
            if (NullUtils.isEmpty(recommConfig.getPlaceList())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PARKING_SPACE_IS_EMPTY));
            }
            if (!NullUtils.isEmpty(recommConfig.getPlaceList())) {
                for (String placeName : recommConfig.getPlaceList().split(",")) {
                    ParkingPlace placeByPlaceNames = parkingService.getPlaceByPlaceNames(Integer.valueOf(recommConfig.getMap()), placeName.replaceAll("\\(.*?\\)", ""),null,null);
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        List<RecommConfig> recommConfig1 = recommConfigService.getRecommConfigById(String.valueOf(recommConfig.getId()), String.valueOf(placeByPlaceNames.getId()),"update");
                        if(!recommConfig1.isEmpty()){
                            return new CommonResult<>(400, LocalUtil.get("请检查列表的车位是否重复配置！！！"));
                        }
                    }
                }
                recommConfigService.updateRecommConfig(recommConfig);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"rcg:see","rcg:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getRecommConfigInfoById/{id}")
    @ApiOperation(value = "查看推荐配置信息", notes = "111")
    public CommonResult<Object> getRecommConfigInfoInfoById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<RecommConfig> recommConfig = recommConfigService.getRecommConfigById(String.valueOf(id),null,"see");
            res.setData(recommConfig);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
