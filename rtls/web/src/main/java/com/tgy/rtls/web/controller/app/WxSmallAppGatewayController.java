package com.tgy.rtls.web.controller.app;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.equip.Gateway_lora;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.map.BsConfigMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.equip.GatewayService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.user.SysUserService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import com.tgy.rtls.web.util.CommonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-08-15 17:40
 * @Description: TODO
 * @Version: 1.0
 */
@RequestMapping(value = "/wxGateway")
@CrossOrigin
@RestController
@Api(value = "微信网关接口")
@Slf4j
public class WxSmallAppGatewayController {
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private LocalUtil localUtil;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired
    private Map2dService map2dService;
    @Autowired
    private SubService subService;
    @Autowired(required = false)
    private BsConfigMapper bsConfigMapper;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private GatewayMapper gatewayMapper;
    @Autowired
    private AppletsWebSocket appletsWebSocket;
    @Autowired
    private ViewMapper viewMapper;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;


    @RequestMapping(value = "/getGatewaySel")
    @ApiOperation(value = "网关查询接口", notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "map", value = "地图id", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "networkstate", value = "连接状态", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "num", value = "网关名", required = false, dataType = "string"),
            @ApiImplicitParam(paramType = "query", name = "relevance", value = "是否关联地图", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getGatewaySel(Integer map, Integer networkstate, String num, Integer relevance, String name,
                                              @RequestParam(value = "desc", defaultValue = "addTime desc") String desc,
                                              @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                              @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            String uid = "12";
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(sysUser)) {
                uid = String.valueOf(sysUser.getPassword());
            }
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize < 0) {
                List<Gateway_lora> gatewayUwbs = gatewayService.findByAllGatewayLora(null, map, networkstate, num, relevance, localUtil.getLocale(), desc, name);
                Gateway_lora gatewayLora = new Gateway_lora();
                //查询当前地图
                List<Map_2d> map2ds = sysUserService.getUserByIdMap1(map);
                gatewayLora.setMapList(map2ds);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), gatewayUwbs);
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<Gateway_lora> gatewayUwbs = gatewayService.findByAllGatewayLora(null, map, networkstate, num, relevance, localUtil.getLocale(), desc, name);
            PageInfo<Gateway_lora> pageInfo = new PageInfo<>(gatewayUwbs);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            //生成操作日志-->查询分站数据
            operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.QUERY_GATEWAY));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /**
     *num-x-y-floor-map-changePlace
     */
    @RequestMapping(value = "/addGateway")
    @ApiOperation(value = "网关新增接口", notes = "uwb网关")
    @ApiImplicitParam(paramType = "path", name = "gatewayLora", value = "uwb网关", required = true)
    public CommonResult addGateway(@RequestBody Map<Object,Object> params, HttpServletRequest request) {
        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        LocalDateTime now = LocalDateTime.now();
        List<DeviceInfoVO> gatewayLoraList = JSONArray.parseArray(JSONObject.toJSONString(params.get("params")), DeviceInfoVO.class);
        CommonResult<Object> res = null;
        CommonResult<Object> res2 = null;
        List<Object> arrayList = new ArrayList<>();
        try {
            if(NullUtils.isEmpty(gatewayLoraList)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.PLEASE_ENTER_THE_DEVICE_INFORMATION_YOU_WANT_TO_BIND));
            }
            for (DeviceInfoVO gatewayLora : gatewayLoraList) {
                res  = new CommonResult<>(200,"");
                res2 = new CommonResult<>();
                //根据当前网关编号查询网关
                List<Gateway_lora> gateway = gatewayService.findAllGateway(null,null, gatewayLora.getNum());
                List<Gateway_lora> gatewayLora1 =null;
                if(!NullUtils.isEmpty(gateway)){
                    gatewayLora1 = gatewayService.findGatewayByNum(gatewayLora.getNum(), String.valueOf(gateway.get(0).getMap()));
                }
                if (gatewayLora1 != null && gatewayLora1.size() > 0) {
                    if(gatewayLora.getChangePlace()){
                        res2.setCode(402);
                        res2.setMessage(LocalUtil.get(gatewayLora.getNum()+"该网关已经被" + gatewayLora1.get(0).getMapName()+"绑定！"));
                        res2.setData(gatewayLora);
                        arrayList.add(res2);
                        res.setData(CommonUtils.speechless(arrayList));
                        continue;
                    }
                    Gateway_lora gatewayLora2 = gatewayLora1.get(0);
                    gatewayLora2.setX(gatewayLora.getX());
                    gatewayLora2.setY(gatewayLora.getY());
                    gatewayLora2.setFloor(gatewayLora.getFloor());
                    gatewayLora2.setMap(gatewayLora.getMap());
                    gatewayLora2.setUpdateTime(new Date());
                    gatewayService.updateWxGateway(gatewayLora2);
                    res2.setCode(200);
                    res2.setMessage(LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                    res2.setData(gatewayLora);
                    arrayList.add(res2);
                    res.setData(CommonUtils.speechless(arrayList));
                    operationlogService.addOperationloguser(sysUser.getUserId(), "更新网关：" + gatewayLora2.getId());
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("小程序更新网关"+gatewayLora.getMap()+":"+gatewayLora.getId()), now);
                    continue;
                } else {
                    Gateway_lora gatewayLora2 = new Gateway_lora();
                    gatewayLora2.setId(gatewayLora2.getId());
                    gatewayLora2.setNum(gatewayLora.getNum());
                    gatewayLora2.setX(gatewayLora.getX());
                    gatewayLora2.setY(gatewayLora.getY());
                    gatewayLora2.setFloor(gatewayLora.getFloor());
                    gatewayLora2.setMap(gatewayLora.getMap());
                    gatewayService.addWxGateway(gatewayLora2);
                    gatewayLora.setId(gatewayLora2.getId());
                    res2.setCode(200);
                    res2.setMessage(LocalUtil.get(KafukaTopics.ADD_SUCCESS));
                    res2.setData(gatewayLora);
                    arrayList.add(res2);
                    res.setData(CommonUtils.speechless(arrayList));
                    JSONObject jsonArea = new JSONObject();
                    List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
                    jsonArea.put("uid", "-1");
                    jsonArea.put("type", 22);
                    jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
                    appletsWebSocket.sendAll(jsonArea.toString());

                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("小程序添加网关"+gatewayLora.getMap()+":"+gatewayLora.getId()), now);
                    continue;
                }
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/delGateway/{ids}")
    @ApiOperation(value = "uwb网关删除接口",notes = "uwb网关id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "uwb网关id集",required = true,dataType = "String")
    public CommonResult delGateway(@PathVariable("ids")String ids,HttpServletRequest request){
        try {
            String uid="12";
            LocalDateTime now = LocalDateTime.now();
            SysUser sysUser=(SysUser) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(sysUser)){
                uid= String.valueOf(sysUser.getUserId());
            }
            Integer map = gatewayMapper.findAllGateway(Integer.valueOf(ids), null, null).get(0).getMap();
            if (gatewayMapper.delGateway_lora(ids.split(","))>0){
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat("小程序网关"+map), now);
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/beaconPos")
    @ApiOperation(value = "添加信标位置", notes = "111")
    public CommonResult<Object> addBeacon(@RequestBody Map<Object,Object> params,HttpServletRequest request) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            String jsonString = objectMapper.writeValueAsString(params);
//            log.error("addFloorLock → params={}", jsonString);
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }
//        SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
        LocalDateTime now = LocalDateTime.now();
        List<DeviceInfoVO> beaconList = JSONArray.parseArray(JSONObject.toJSONString(params.get("params")), DeviceInfoVO.class);
        CommonResult<Object> res = null;
        CommonResult<Object> res2 = null;
        List<Object> arrayList = new ArrayList<>();
        try {
            if(NullUtils.isEmpty(beaconList)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.PLEASE_ENTER_THE_DEVICE_INFORMATION_YOU_WANT_TO_BIND));
            }
            for (DeviceInfoVO beacon : beaconList) {
                res  = new CommonResult<>(200,"");
                res2 = new CommonResult<>();
                if (beacon.getFMapId() == null) {
                    res.setMessage("fmap is null");
                    res.setCode(500);
                    return res;
                }
                Map_2d map2d = map2dService.findByfmapID(String.valueOf(beacon.getMap()));
                Integer mapid = null;
                if (map2d != null) {
                    mapid = map2d.getId();
                }
                BsConfig data = bsConfigMapper.findByNum(beacon.getNum(), localUtil.getLocale());
                Substation subCurrentMapName=null;
                if(!NullUtils.isEmpty(data)){
                    subCurrentMapName = subService.getCurrentSubMapName(beacon.getNum(),data.getMap());
                }
                if (data == null) {
                    Substation sub = new Substation();
                    sub.setType(beacon.getType());
                    sub.setDeviceType(7);
                    sub.setNum(beacon.getNum());
                    sub.setMap(String.valueOf(mapid));
                    if (subService.addSub(sub, null)) {
                        BsConfig bsConfig = new BsConfig();
                        bsConfig.setMap(mapid);
                        bsConfig.setBsid(sub.getId());
                        bsConfig.setX(beacon.getX());
                        bsConfig.setY(beacon.getY());
                        bsConfig.setZ(bsConfig.getZ());
                        bsConfig.setFloor(beacon.getFloor());
                        bsConfigService.addDisparkBsConfig(bsConfig);
                        beacon.setId(bsConfig.getId());
                        res2.setCode(200);
                        res2.setMessage(LocalUtil.get(KafukaTopics.ADD_SUCCESS));
                        res2.setData(beacon);
                        arrayList.add(res2);
                        res.setData(CommonUtils.speechless(arrayList));
                        JSONObject jsonArea = new JSONObject();
                        List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
                        jsonArea.put("uid", "-1");
                        jsonArea.put("type", 22);
                        jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
                        appletsWebSocket.sendAll(jsonArea.toString());
                        String ip = IpUtil.getIpAddr(request);
                        String address = ip2regionSearcher.getAddressAndIsp(ip);
//                        operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat("小程序添加信标"+mapid+":"+ sub.getId()), now);
                        continue;
                    }
                } else {
                    if(beacon.getChangePlace()){
                        res2.setCode(402);
                        res2.setMessage(LocalUtil.get(beacon.getNum()+"该信标/道钉已经被" + subCurrentMapName.getMapName()+"绑定！"));
                        res2.setData(beacon);
                        arrayList.add(res2);
                        res.setData(CommonUtils.speechless(arrayList));
                        continue;
                    }
                    Substation sub = subService.findByNum(beacon.getNum());
                    sub.setType(beacon.getType());
                    sub.setDeviceType(7);
                    data.setFloor(beacon.getFloor());
                    data.setX(beacon.getX());
                    data.setY(beacon.getY());
                    data.setZ(Double.valueOf(beacon.getZ()));
                    data.setMap(mapid);
                    data.setLastTimeMap(sub.getMap());
                    sub.setMap(String.valueOf(mapid));
                    sub.setNetworkstate(0);
                    sub.setUpdateTime(new Date());
                    bsConfigService.updateBsConfig(data);
                    subMapper.updateSub(sub);
                    res2.setCode(200);
                    res2.setMessage(LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                    res2.setData(sub);
                    arrayList.add(res2);
                    res.setData(CommonUtils.speechless(arrayList));

                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
//                    operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat("小程序更新信标"+mapid+":"+ sub.getId()), now);
                    continue;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        //try {
        //    redisTemplate.opsForList().leftPushAll("bsConfigs:"+mapid, bsConfigService.findByAll(mapid).get());
        //} catch (Exception e) {
        //    throw new RuntimeException(e);
        //}
        return res;
    }

    @RequestMapping(value = "/delSub/{ids}")
    @ApiOperation(value = "删除信标接口",notes = "信标部署小程序")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "分站参数id集",required = true,dataType = "String")
    public CommonResult<Object> delSub(@PathVariable("ids")String ids,HttpServletRequest request){
        try {
            LocalDateTime now = LocalDateTime.now();
            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
            Integer map = bsConfigMapper.findById2(ids).getMap();
            if (bsConfigService.delBsConfig1(ids,map)){
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(sysUser.getUserId(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat("小程序信标"+map), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
