package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.MapPathLabel;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.map.MapPathLabelService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.map
 * @Author: wuwei
 * @CreateTime: 2023-01-02 15:15
 * @Description: TODO
 * @Version: 1.0
 */
@Api(value="地图路径标注")
@RestController
@RequestMapping(value = "/mapPathLabel")
@CrossOrigin
public class MapPathLabelController {
    @Autowired
    private MapPathLabelService mapPathLabelService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getMapPathLabel")
    @ApiOperation(value = "地图路径标注查询接口", notes = "地图路径标注查询")
    public CommonResult<Object> getMapPathLabel(String name, Integer pageIndex, Integer pageSize,String floorName, Integer mapId,String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<MapPathLabel> mapPathLabels = mapPathLabelService.getMapPathLabels(name,mapId, floorName, mapids);
            PageInfo<MapPathLabel> pageInfo = new PageInfo<>(mapPathLabels);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            if (pageSize != null && pageSize != -1) {
                res.setData(map);
            } else {
                res.setData(mapPathLabels);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMapPathLabel2")
    @ApiOperation(value = "小程序地图路径标注查询接口", notes = "地图路径标注查询")
    public CommonResult<Object> getMapPathLabel2(String map,String floorName) {
        try {
            List<MapPathLabel> mapPathLabels = mapPathLabelService.getMapPathLabels(null, Integer.valueOf(map),floorName,null);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),mapPathLabels);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"mpl:add"})
    @RequestMapping(value = "/addMapPathLabel")
    @ApiOperation(value = "地图路径标注添加接口", notes = "111")
    public CommonResult<Object> addMapPathLabel(MapPathLabel mapPathLabel,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if(NullUtils.isEmpty(mapPathLabel.getName())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ROAD_NAME));
            }
            if(NullUtils.isEmpty(mapPathLabel.getInteriorName())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.INTERIOR_NAME));
            }
            if(NullUtils.isEmpty(mapPathLabel.getMap())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_ID));
            }
            if(NullUtils.isEmpty(mapPathLabel.getX())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_X));
            }
            if(NullUtils.isEmpty(mapPathLabel.getY())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_Y));
            }
            mapPathLabel.setAddTime(new Date());
            mapPathLabelService.addMapPathLabel(mapPathLabel);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MAP_PATH_INFO)), now);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
        }
    }

    @RequiresPermissions({"mpl:del"})
    @RequestMapping(value = "/delMapPathLabel/{ids}")
    @ApiOperation(value = "删除车位出入口信息", notes = "111")
    public CommonResult<Object> delMapPathLabel(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            mapPathLabelService.delMapPathLabel(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MAP_PATH_INFO)), now);

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"mpl:see","mpl:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getMapPathLabelById/{id}")
    @ApiOperation(value = "获取车位出入口信息", notes = "111")
    public CommonResult<Object> getMapPathLabelById(@PathVariable("id") Integer id) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            MapPathLabel data = mapPathLabelService.getMapPathLabelById(id);
            res.setData(data);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"mpl:edit"})
    @RequestMapping(value = "/editMapPathLabel")
    @ApiOperation(value = "地图路径标注修改接口", notes = "111")
    public CommonResult<Object> editMapPathLabel(MapPathLabel mapPathLabel,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if(NullUtils.isEmpty(mapPathLabel.getName())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ROAD_NAME));
            }
            if(NullUtils.isEmpty(mapPathLabel.getInteriorName())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.INTERIOR_NAME));
            }
            if(NullUtils.isEmpty(mapPathLabel.getMap())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_ID));
            }
            if(NullUtils.isEmpty(mapPathLabel.getX())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_X));
            }
            if(NullUtils.isEmpty(mapPathLabel.getY())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.MAP_Y));
            }
            mapPathLabel.setUpdateTime(new Date());
            mapPathLabelService.editById(mapPathLabel);
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MAP_PATH_INFO)), now);

            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
        }
    }
}
