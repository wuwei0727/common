package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.MapBuildCommon;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.map.MapBuildCommonService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:36
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/mapBuild")
public class MapBuildController {
    @Autowired
    private MapBuildCommonService mapBuildCommonService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;


    @MyPermission
    @RequestMapping(value = "/getMapBuild")
    public CommonResult<Object> getMapBuild(String name, Integer map,String objectType, Integer floor, Integer pageIndex, Integer pageSize,String floorName,
                                                          @RequestParam(value = "desc",defaultValue = "id") String desc,String type, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<MapBuildCommon> list = null;
            if("wc".equals(type)){
                list = mapBuildCommonService.getByConditions(name,map,floor,desc,floorName,objectType,mapids,null);
            }else if("build".equals(type)){
                list = mapBuildCommonService.getByConditions2(name,map,floor,desc,floorName,objectType,mapids,null);
            }
            PageInfo<MapBuildCommon> pageInfo = new PageInfo<>(list);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/getMapBuild2")
    public CommonResult<Object> getMapBuild2(String name, Integer map, String floorName,Integer pageSize) {
        try {
            if (pageSize < 0) {
                List<MapBuildCommon> mapwc = mapBuildCommonService.getByConditions(name, map, null, null, floorName, null, null,null);
                List<MapBuildCommon> mapBuild = mapBuildCommonService.getByConditions2(name, map, null, null, floorName, null, null,null);
                mapwc.addAll(mapBuild);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), mapwc);
            }
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"mp:add"})
    @RequestMapping(value = "/addMapBuild")
    public CommonResult<Object> addMapBuild(@RequestBody MapBuildCommon mapBuildCommon, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if("wc".equals(mapBuildCommon.getType())){
                if(mapBuildCommonService.addMapBuild(mapBuildCommon)){
                    LocalDateTime now = LocalDateTime.now();
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MWC_INFO)), now);
                }
            }else if("build".equals(mapBuildCommon.getType())){
                if(mapBuildCommonService.addMapBuild2(mapBuildCommon)){
                    LocalDateTime now = LocalDateTime.now();
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MB_INFO)), now);
                }
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"mp:see","mp:edit"}, logical = Logical.OR)
    @RequestMapping(value = "/getMapBuild2ById/{id}/{type}")
    public CommonResult<Object> getMapBuild2ById(@PathVariable("id") Integer id,@PathVariable("type") String type) {
        try {
            MapBuildCommon list = null;
            if("wc".equals(type)){
                list = mapBuildCommonService.getMapBuild2ById(id);
            }else if("build".equals(type)){
                list = mapBuildCommonService.getMapBuild2ById2(id);
            }
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),list);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequiresPermissions({"mp:edit"})
    @RequestMapping(value = "/updateMapBuild")
    public CommonResult<Object> updateMapBuild(@RequestBody MapBuildCommon mapBuildCommon, HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if("wc".equals(mapBuildCommon.getType())){
                if(mapBuildCommonService.updateMapBuild(mapBuildCommon)){
                    LocalDateTime now = LocalDateTime.now();
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MWC_INFO)), now);
                }
            }else if("build".equals(mapBuildCommon.getType())){
                if(mapBuildCommonService.updateMapBuild2(mapBuildCommon)){
                    LocalDateTime now = LocalDateTime.now();
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MB_INFO)), now);
                }
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.EDIT_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequiresPermissions({"mp:del"})
    @RequestMapping(value = "/delMapBuild/{ids}/{type}")
    @ApiOperation(value = "删除车位视频检测信息", notes = "111")
    public CommonResult<Object> delMapBuild(@PathVariable("ids") String ids,@PathVariable("type") String type,HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            LocalDateTime now = LocalDateTime.now();
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            if("wc".equals(type)){
                if (mapBuildCommonService.delMapBuild(ids.split(","))) {
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)),KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MWC_INFO)), now);
                }
            }else if("build".equals(type)){
                if (mapBuildCommonService.delMapBuild2(ids.split(","))) {
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)),KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MB_INFO)), now);
                }
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
//这是一个main方法，程序的入口
public static void main(String[] args) {
//    // 示例数据
//    String timeString = "2024-07-02 16:32:45"; // 从realTimeInAndOutData.getTime()获取的时间字符串
//
//    // 定义日期格式
//    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//    try {
//        // 解析时间字符串为Date对象
//        Date date = dateFormat.parse(timeString);
//        // 将Date对象转换为时间戳（以毫秒为单位）
//        long timestamp = date.getTime();
//
//        // 打印时间戳
//        System.out.println("时间戳: " + timestamp);
//
//        // 示例：计算两个时间戳之间的差值
//        long otherTimestamp = System.currentTimeMillis(); // 当前时间戳
//        long durationMillis = Math.abs(otherTimestamp - timestamp);
//        long durationMinutes = durationMillis / (1000 * 60);
//
//        // 判断是否大于12分钟
//        if (durationMinutes > 12) {
//            System.out.println("超过12分钟");
//        } else {
//            System.out.println("12分钟以内");
//        }
//    } catch (ParseException e) {
//        System.err.println("时间格式错误：" + e.getMessage());
//    }
        long realTimeTimestamp = 1719919502000L;
        long timestamp = 1719918812000L;
        Instant instant1 = Instant.ofEpochMilli(timestamp);
        Instant instant2 = Instant.ofEpochMilli(realTimeTimestamp);
        Duration duration = Duration.between(instant1, instant2);
        if (Math.abs(duration.toMinutes()) >= 12) {
            System.out.println("时间相差超过12分钟");
        } else {
            System.out.println("时间相差在12分钟以内");
        }
    }
}
