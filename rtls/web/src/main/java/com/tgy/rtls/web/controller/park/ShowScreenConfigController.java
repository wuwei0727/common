package com.tgy.rtls.web.controller.park;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.park.ShowScreenConfigArea;
import com.tgy.rtls.data.entity.park.ShowScreenConfigAreaPlace;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ShowScreenConfigMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.ShowScreenConfigService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
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
import java.util.*;

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
@RequestMapping(value = "/showScreenConfig")
@Slf4j
@Api("显示屏配置管理")
public class ShowScreenConfigController {

    @Autowired
    private ShowScreenConfigService showScreenConfigService;
    @Autowired
    private ShowScreenConfigMapper showScreenConfigMapper;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getAllRecommConfigOrConditionQuery")
    @ApiOperation(value = "获取显示屏配置信息", notes = "111")
    public CommonResult<Object> getAllRecommConfigOrConditionQuery(String map, String deviceNum,String floorName, String screennum, String screenposition , String bindarea, Integer pageIndex, Integer pageSize, @RequestParam(value = "desc", defaultValue = "starttime desc") String desc, String maps) {
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

            List<ShowScreenConfig> data = showScreenConfigService.getAllShowScreenConfigOrConditionQuery(map, deviceNum, screennum,screenposition,bindarea,desc,floorName, mapids);
            PageInfo<ShowScreenConfig> pageInfo = new PageInfo<>(data);
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

    @RequiresPermissions({"sg:add"})
    @RequestMapping(value = "/addShowScreenConfig")
    @ApiOperation(value = "添加显示屏配置信息", notes = "111")
    public CommonResult<Object> addShowScreenConfig(@RequestBody ShowScreenConfig showScreenConfig, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(showScreenConfig.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getBindarea())) {
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getGuideScreenId())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getScreennum())) {
                return new CommonResult<>(400, LocalUtil.get("屏幕编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getScreenposition())) {
                return new CommonResult<>(400, LocalUtil.get("屏幕方位不能为空！！！"));
            }
            //ShowScreenConfig guideScreenId = showScreenConfigService.getGuideScreenIdByGuideScreenIsRepeated(String.valueOf(showScreenConfig.getGuideScreenId()),null);
            //if(!NullUtils.isEmpty(guideScreenId)){
            //    return new CommonResult<>(400, LocalUtil.get("当前引导屏已经被绑定！！！"));
            //}
            showScreenConfig.setGuideScreenId(showScreenConfig.getGuideScreenId());
            showScreenConfig.setStarttime(LocalDateTime.now());
            showScreenConfigService.addShowScreenConfig(showScreenConfig);
            List<ShowScreenConfigArea> areaList = new ArrayList<>();
            ShowScreenConfigArea area = null;
            if (NullUtils.isEmpty(showScreenConfig.getVertexInfo())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.COORDINATES_IS_EMPTY));
            } else {
                for (int i = 0; i < showScreenConfig.getVertexInfo().size(); i++) {
                    ShowScreenConfigArea screenConfigArea = showScreenConfig.getVertexInfo().get(i);
                    for (ShowScreenConfigArea point : screenConfigArea.getPoints()) {
                        area = new ShowScreenConfigArea();
                        area.setId(showScreenConfig.getId());
                        area.setX(point.getX());
                        area.setY(point.getY());
                        area.setFloor(screenConfigArea.getFloor());
                        area.setSid(showScreenConfig.getId());
                        area.setAreaQuFen(String.valueOf(i+1));
                        areaList.add(area);
                    }
                }
                showScreenConfigService.addShowScreenConfigArea(areaList);
            }

            if (!NullUtils.isEmpty(showScreenConfig.getPlaceList())) {
                List<String>  nameList= Arrays.asList(showScreenConfig.getPlaceList().split(","));
                List<ParkingPlace> placeByPlaceNames = parkingService.getPlaceByPlaceNameList(Integer.valueOf(showScreenConfig.getMap()), nameList);
                List<ShowScreenConfigAreaPlace> areaPlaceList = new ArrayList<>();
                for (ParkingPlace placeByPlaceName : placeByPlaceNames) {
                    ShowScreenConfigAreaPlace areaPlace = new ShowScreenConfigAreaPlace();
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        areaPlace.setAreaId(area.getSid());
                        areaPlace.setPlaceId(Long.valueOf(placeByPlaceName.getId()));
                        areaPlaceList.add(areaPlace);
                    }
                }
                showScreenConfigService.addShowScreenConfigAreaPlace(areaPlaceList);
            } else {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PARKING_SPACE_IS_EMPTY));
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_CONFIG)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"sg:del"})
    @RequestMapping(value = "/delShowScreenConfig/{ids}")
    @ApiOperation(value = "删除显示屏配置信息", notes = "111")
    public CommonResult<Object> delShowScreenConfig(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            showScreenConfigService.delShowScreenConfig(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_CONFIG)), now);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
    @RequiresPermissions(value = {"sg:edit"})
    @RequestMapping(value = "/updateShowScreenConfig")
    @ApiOperation(value = "更新显示屏配置信息", notes = "111")
    public CommonResult<Object> updateShowScreenConfig(@RequestBody ShowScreenConfig showScreenConfig,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String uid = "12";
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (NullUtils.isEmpty(showScreenConfig.getMap())) {
                return new CommonResult<>(400, LocalUtil.get("请选择关联地图！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getBindarea())) {
                return new CommonResult<>(400, LocalUtil.get("区域名称不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getGuideScreenId())) {
                return new CommonResult<>(400, LocalUtil.get("设备编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getScreennum())) {
                return new CommonResult<>(400, LocalUtil.get("屏幕编号不能为空！！！"));
            }
            if (NullUtils.isEmpty(showScreenConfig.getScreenposition())) {
                return new CommonResult<>(400, LocalUtil.get("屏幕方位不能为空！！！"));
            }
//            ShowScreenConfig guideScreenId = showScreenConfigService.getGuideScreenIdByGuideScreenIsRepeated(null,String.valueOf(showScreenConfig.getGuideScreenId()));
//            if(!NullUtils.isEmpty(guideScreenId)){
//                return new CommonResult<>(400, LocalUtil.get("当前引导屏已经被绑定！！！"));
//            }
            showScreenConfig.setGuideScreenId(showScreenConfig.getGuideScreenId());
            showScreenConfig.setEndtime(LocalDateTime.now());
            showScreenConfigService.updateShowScreenConfig(showScreenConfig);
            List<ShowScreenConfig> screenConfigList = showScreenConfigService.getShowScreenConfigById(String.valueOf(showScreenConfig.getId()));
            List<ShowScreenConfigArea> areaList = new ArrayList<>();
            if (NullUtils.isEmpty(showScreenConfig.getVertexInfo())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.COORDINATES_IS_EMPTY));
            } else {
                showScreenConfigMapper.delShowScreenConfigArea(String.valueOf(screenConfigList.get(0).getAreaId()));
                for (int i = 0; i < showScreenConfig.getVertexInfo().size(); i++) {
                    ShowScreenConfigArea screenConfigArea = showScreenConfig.getVertexInfo().get(i);
                    for (ShowScreenConfigArea screenConfigArea1 : screenConfigArea.getPoints()) {
                        ShowScreenConfigArea area = new ShowScreenConfigArea();
                        area.setX(screenConfigArea1.getX());
                        area.setY(screenConfigArea1.getY());
                        area.setFloor(screenConfigArea.getFloor());
                        area.setSid(showScreenConfig.getId());
                        area.setAreaQuFen(String.valueOf(i + 1));
                        areaList.add(area);
                    }
                }
                showScreenConfigService.addShowScreenConfigArea(areaList);
            }

            if (!NullUtils.isEmpty(showScreenConfig.getPlaceList())) {
                showScreenConfigService.delShowScreenConfigByAreaId(String.valueOf(screenConfigList.get(0).getAreaId()));
                List<String>  nameList= Arrays.asList(showScreenConfig.getPlaceList().split(","));
                List<ParkingPlace> placeByPlaceNames = parkingService.getPlaceByPlaceNameList(Integer.valueOf(showScreenConfig.getMap()), nameList);
                List<ShowScreenConfigAreaPlace> areaPlaceList = new ArrayList<>();
                for (ParkingPlace placeByPlaceName : placeByPlaceNames) {
                    ShowScreenConfigAreaPlace areaPlace = new ShowScreenConfigAreaPlace();
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        areaPlace.setAreaId(screenConfigList.get(0).getAreaId());
                        areaPlace.setPlaceId(Long.valueOf(placeByPlaceName.getId()));
                        areaPlaceList.add(areaPlace);
                    }
                }
                showScreenConfigService.addShowScreenConfigAreaPlace(areaPlaceList);
            } else {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PARKING_SPACE_IS_EMPTY));
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_CONFIG)), now);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"sg:see", "sg:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getShowScreenConfigById/{id}")
    @ApiOperation(value = "查看显示屏配置信息", notes = "111")
    public CommonResult<Object> getShowScreenConfigById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ShowScreenConfig> screenConfigList = showScreenConfigService.getShowScreenConfigById(id.toString());
            res.setData(screenConfigList);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
