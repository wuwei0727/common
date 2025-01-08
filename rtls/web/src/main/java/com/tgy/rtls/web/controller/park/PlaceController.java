package com.tgy.rtls.web.controller.park;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.excel.ExcelDataVo;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.*;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.userinfo.WechatUserPosition;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.CollectMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.TagService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.sms.impl.SmsService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.service.vip.ParkingInfoStatisticsService;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.config.SpringContextHolder;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import com.tgy.rtls.web.util.CommonUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 车位规划
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/park")
@Slf4j
public class PlaceController {
    @Autowired
    private ParkingService parkingService;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Autowired(required = false)
    private Map2dService map2dService;
    @Autowired
    private RedisService redisService;
    @Autowired
    TagService tagService;
    @Autowired
    SmsService smsService;
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired(required = false)
    private CollectMapper collectMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private ParkingInfoStatisticsService parkingInfoStatisticsService;
    @Autowired
    private AppletsWebSocket appletsWebSocket;
    @Autowired
    private ViewMapper viewMapper;
    @Autowired
    private FloorLockService floorLockService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @MyPermission
    @RequestMapping(value = "/getPlace")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlace(String name, String companyName, String license,String carbitType, Integer map, Integer company,
                                         String floor, Short state, Short type, Short charge, String fid,String configWay,
                                         Integer pageIndex,
                                         Integer pageSize,String floorName,
                                         @RequestParam(value = "desc", defaultValue = "parking_place.id desc") String desc,String reserve, String maps) {
        try {
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            Object member = SecurityUtils.getSubject().getPrincipal();
            if (member instanceof ParkingCompany) {
                ParkingCompany com = (ParkingCompany) member;
                company = com.getId();
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            if (license != null) {
                license = license.toUpperCase();
            }
            if("1".equals(reserve)){
                carbitType = "1";
                state = 0;
                List<ParkingPlace> data = parkingService.findByAllPlace2(null, null,null,map,null,carbitType,state ,company,null,null,null,reserve,null,null,null,null, null,floorName, null, null);
                res.setData(data);
                return res;
            }
            List<ParkingPlace> data = parkingService.findByAllPlace2(null, name, companyName, map, license,carbitType, state, company, floor, charge, type, reserve, fid,configWay, null, null,desc,floorName, null, mapids);
            List<ParkingCompanyVo> mapComName = parkMapper.getAllMap(mapids);
            Object sessionId = redisTemplate.opsForValue().get("sessionId");
            PageInfo<ParkingPlace> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("mapComName",mapComName);
            result.put("sessionId",sessionId);
            if (pageSize != null && pageSize != -1) {
                res.setData(result);
            } else {
                res.setData(data);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

     @RequiresPermissions(value = {"pak:see","pak:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getPlaceById/{id}")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlace(@PathVariable("id") Integer id, String name, String license, Integer map, String floor, Short type, Short charge, Integer company, Short state, String fid, Integer pageIndex, Integer pageSize) {
        try {
            if (license != null) {
                license = license.toUpperCase();
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ParkingPlace> data = parkingService.findByAllPlace(id, name, null, map, license, state, company, floor, charge, type, null, fid, pageIndex, pageSize, null);
            ParkingPlace place = data.size() == 0 ? null : data.get(0);
            if (place.getCompany() != null) {
                List<ParkingCompany> companys = parkMapper.findByAllCompany(place.getCompany(), null, null, null, null, null);
                if (companys != null && companys.size() > 0) {
                    place.setUser(companys.get(0).getUser());
                    place.setPhone(companys.get(0).getPhone());
                }
            }
            res.setData(place);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

     @RequiresPermissions({"pak:add"})
    @RequestMapping(value = "/addPlace")
    @ApiOperation(value = "添加车位信息", notes = "111")
    public CommonResult<Object> addPlace(ParkingPlace parkingPlace, HttpServletRequest request) {
        try {
            String uid = "12";
            if (parkingPlace.getLicense() != null) {
                parkingPlace.setLicense(parkingPlace.getLicense().toUpperCase());
            }
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            ParkingPlace place = parkingService.getPlaceByPlaceId(null,null,parkingPlace.getName(),parkingPlace.getMap());
            if(!NullUtils.isEmpty(place)){
                return new CommonResult<>(500, LocalUtil.get("车位名称重复"));
            }
            parkingService.addPlace(parkingPlace);
            LocalDateTime now = LocalDateTime.now();

            // String ip = IpUtil.getIpAddr(request);
            // String address = ip2regionSearcher.getAddressAndIsp(ip);
            // operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.PLACE_PLANNING)), now);

            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

     @RequiresPermissions(value = {"pak:edit"})
    @RequestMapping(value = "/updatePlace")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public CommonResult<Object> updatePlace(ParkingPlace parkingPlace,HttpServletRequest request) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if (parkingPlace.getLicense() != null) {
                parkingPlace.setLicense(parkingPlace.getLicense().toUpperCase());
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            //if (!NullUtils.isEmpty(parkingPlace.getCompany())) {
            if(NullUtils.isEmpty(parkingPlace.getElevatorId())||parkingPlace.getElevatorId()==0){
                parkingPlace.setElevatorId(null);
            }
            ParkingPlace place = parkingService.getPlaceByPlaceId(parkingPlace.getId(),"update",parkingPlace.getName(),parkingPlace.getMap());
            if(!NullUtils.isEmpty(place)){
                return new CommonResult<>(500, LocalUtil.get("车位名称重复"));
            }

            parkingService.updatePlaceCompany(parkingPlace);
                List<ParkingCompany> data = parkingService.findByAllCompany(parkingPlace.getCompany(), null, null, null, null, null);
                //if (data != null && data.size() == 1)
                //// smsService.sendPhone(data.get(0).getPhone(),"点击链接后跳转到小程序"+"https://tuguiyao-gd.com/UWB/page/jump.html?placeId="+parkingPlace.getId());
                //{
                //    smsService.sendPhone(data.get(0).getPhone(), "点击链接后跳转到小程序" + "http://suo.nz/1EbisI");
                //}

            //}
            LocalDateTime now = LocalDateTime.now();

            // String ip = IpUtil.getIpAddr(request);
            // String address = ip2regionSearcher.getAddressAndIsp(ip);
            // operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.PLACE_PLANNING)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
    @RequiresPermissions({"pak:del"})
    @RequestMapping(value = "/delPlace/{ids}")
    @ApiOperation(value = "删除车位信息", notes = "111")
    public CommonResult<Object> delPlace(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            parkingService.deletePlace(ids.split(","));
            eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, null));
            LocalDateTime now = LocalDateTime.now();

            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.PLACE_PLANNING)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @PostMapping(value = "exportFloorLockExcel/{mapId}")
    public void exportFloorLockExcel(@PathVariable("mapId") Integer mapId, HttpServletResponse response) {
        try {
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            List<ExcelDataVo> list = parkingService.getParkingPlaceList(mapId);
            list.forEach(item -> {
                String type = item.getTypeId();
                switch (type) {
                    case "0":
                        item.setTypeId("200401");  // 普通车位
                        break;
                    case "1":
                        item.setTypeId("340860");  // 充电车位
                        break;
                    case "2":
                        item.setTypeId("340862");  // 专属车位
                        break;
                    case "3":
                        item.setTypeId("340859");  // 无障碍
                        break;
                    case "4":
                        item.setTypeId("340861");  // 超宽
                        break;
                    case "5":
                        item.setTypeId("340863");  // 子母
                        break;
                    case "6":
                        item.setTypeId("340864");  // 小型
                        break;
                }
            });
            String fileName = list.get(0).getMapName() + "-" + list.get(0).getFMapId() + ".xls";
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            EasyExcel.write(response.getOutputStream(), ExcelDataVo.class)//对应的导出实体类
                    .excelType(ExcelTypeEnum.XLS)//excel文件类型，包括CSV、XLS、XLSX
                    .sheet("车位信息表")//导出sheet页名称
                    .doWrite(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/getPlaceByCompanyName")
    @ApiOperation(value = "获取车位信息", notes = "111")
    public CommonResult<Object> getPlaceByCompanyName(String name, String companyName, String license, Integer map, Integer company, String floor, Short state, Short type, Short charge, String fid, Integer pageIndex, Integer pageSize) {
        try {
            Object member = SecurityUtils.getSubject().getPrincipal();
            if (member instanceof ParkingCompany) {
                ParkingCompany com = (ParkingCompany) member;
                company = com.getId();
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            if (license != null) {
                license = license.toUpperCase();
            }
            List<ParkingPlace> data = parkingService.findByAllCompanyName(null, name, companyName, map, license, state, company, floor, charge, type, null, fid, null, null);
            PageInfo<ParkingPlace> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());

            if (pageSize != null && pageSize != -1) {
                res.setData(result);
            } else {
                res.setData(data);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    /***
     *
     * @param companyName
     * @param map
     * @param x
     * @param y
     * @param floor
     * @param carBitType 是否偏好车位 0普通；1充电车位
     * @param isVip 是否VIP 0不是1是VIP车位
     * @param placeName 车位名
     */
    @RequestMapping(value = "/getWechatPlaceByCompanyName")
    @ApiOperation(value = "获取车位信息", notes = "recommend")
    public CommonResult<Object> getWechatPlaceByCompanyName(String companyName,Long userId,Integer map,double x,double y,String floor,String carBitType,String isVip,String placeName) {
        try {
            // log.error("车位推荐请求参数:map="+map+"&x="+x+"&y="+y+"&floor="+floor+"&carBitType="+carBitType+"&isVip="+isVip+"&placeName="+placeName+"&companyName="+companyName);
            Session session = SecurityUtils.getSubject().getSession();
            String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            ArrayList<ParkingPlace> recommendedPlace = new ArrayList<>();
            //内测期间不推荐车位
            List<CompanyPlace> data = companyName == null || companyName.trim().isEmpty() ? null : parkMapper.findPlaceCountByCompanyContainPlaceList(map, 1, companyName);
            List<Object> exclusiveAndFreePlaces = null;
            if("null".equals(carBitType)){
                carBitType = "0";
            }
            Integer preferenceCarBit = NullUtils.isEmpty(carBitType) ? null : Integer.valueOf(carBitType);
            Integer vipPlace = "0".equals(isVip) ? null : Integer.valueOf(isVip);
            if (!NullUtils.isEmpty(data)) {//绑定公司
                exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, preferenceCarBit, 0,null);//查询关键字公司车位
                if(NullUtils.isEmpty(exclusiveAndFreePlaces)){
                    exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,0, 0,0,null);//查询关键字公司车位
                }
            }else {//没有绑定公司、随便点
                exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, preferenceCarBit,0,null);//查询关键字公司车位
                //无障碍没有车推荐普通
//                if("3".equals(carBitType)&&NullUtils.isEmpty(exclusiveAndFreePlaces)){
//                    exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, 0,0,null);//查询关键字公司车位
//                }
            }

            if("1".equals(isVip)){//VIP有地锁
                String[] placeId=null;
                //车位被占用，查询公司下没带地锁的VIP车位
                List<FloorLock> floorLockInfo = floorLockService.getFloorLockInfo(Long.valueOf(map), null, placeName, null,null, null, null, null, null, null);
                //绑定地锁不推荐，推荐未被绑定地锁并未占用车位
                StringBuilder stringBuilder = new StringBuilder ();
                List<FloorLock> occupied = floorLockService.getFloorLockInfo(Long.valueOf(map), null, null, null,null, null, null, null, null, null);
                if(!NullUtils.isEmpty(occupied)){
                    for (FloorLock floorLock : occupied) {
                        stringBuilder.append(floorLock.getPlace()).append(",").toString();
                    }
                    placeId = stringBuilder.substring(0,stringBuilder.length()-1).split(",");
                }
                if(!NullUtils.isEmpty(floorLockInfo)){
                    if(floorLockInfo.get(0).getState()==1){
                        Integer companyId = floorLockInfo.get(0).getCompany();
                        if(NullUtils.isEmpty(companyId)){//车位未绑定公司，并且无地锁车位
                            exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId, 1,placeId,null);
                        }else {//推荐当前公司下的无地锁VIP车位包括充电车位
                            exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId,1,placeId,null);
                        }
                        if(NullUtils.isEmpty(exclusiveAndFreePlaces)){
                            exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, 0,0,null);//查询关键字公司车位
                        }
                    }else {
                        exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, preferenceCarBit,vipPlace,placeName);//查询关键字公司车位
                    }
                    //没有就普通
                }else {//VIP没有地锁
                    if (!NullUtils.isEmpty(data)) {//绑定公司
                            exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,data.get(0).getId(), preferenceCarBit,vipPlace,null);//查询关键字公司车位
                    }else {//没有绑定公司
                        List<ParkingPlace> currentPlacesBindCompany = parkingService.getCurrentPlacesBindCompany(map, placeName, null);//查该车位绑定的公司的VIP车位
                         Integer companyId = currentPlacesBindCompany.get(0).getCompany();
                         if(NullUtils.isEmpty(companyId)){//无公司属性车位
                            exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId, 1,placeId,null);
                         } else if (!NullUtils.isEmpty(companyId)) {
                             exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId,1,placeId,null);
                         } else {
                             exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId, 0,placeId,0);
                         }
                        if(!NullUtils.isEmpty(currentPlacesBindCompany)&&!NullUtils.isEmpty(companyId)){
                            exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,companyId,1,placeId, null);
                            if(NullUtils.isEmpty(exclusiveAndFreePlaces)){
                                exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,null,0,placeId, 0);//查询关键字公司车位
                            }
                        }
                        if(NullUtils.isEmpty(exclusiveAndFreePlaces)){//绑定公司和未绑定公司的车位推荐完，直接推荐普通车位
                                exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,null,0,placeId, 0);//查询关键字公司车位
                        }
                        if(NullUtils.isEmpty(companyId)){
                            if("0".equals(currentPlacesBindCompany.get(0).getCarbittype())){
                                exclusiveAndFreePlaces = parkingService.getOrdinaryPlaces(map,null,0,placeId, 0);//查询关键字公司车位
                            }
                        }
                    }
                }
            }

            if ("1".equals(isVip)&&floor != null){//优先查找公司专属车位
                 exclusiveAndFreePlaces = getSortPlace(exclusiveAndFreePlaces, x, y, floor, String.valueOf(preferenceCarBit));
            } else{//点击地图位置查找当前位置附近空车位
                exclusiveAndFreePlaces = getSortPlace(exclusiveAndFreePlaces, x, y, floor,String.valueOf(preferenceCarBit));
            }
            // log.error("根据查询条件排序的车位："+ exclusiveAndFreePlaces);
            //推荐车位
            List<ParkingPlace> resData;
                resData = getCunnrentWeightPlace(exclusiveAndFreePlaces, SpringContextHolder.parkingPlaceConcurrentHashMap);
                if("0".equals(carBitType)&&"1".equals(isVip)&&!NullUtils.isEmpty(placeName)&&NullUtils.isEmpty(resData)){
                    exclusiveAndFreePlaces = parkingService.getExclusiveAndFreePlaces(map,null, 0,0,null);//查询关键字公司车位
                    if(!NullUtils.isEmpty(exclusiveAndFreePlaces)){
                        exclusiveAndFreePlaces = getSortPlace(exclusiveAndFreePlaces, x, y, floor,String.valueOf(preferenceCarBit));
                        resData = getCunnrentWeightPlace(exclusiveAndFreePlaces, SpringContextHolder.parkingPlaceConcurrentHashMap);
                    }
                }
            // log.error("根据排序权重的车位："+ resData);

            List<ParkingPlace> parkingPlaceList = CommonUtils.toList(resData);
            Integer place = NullUtils.isEmpty(parkingPlaceList) ? null : parkingPlaceList.get(0).getId();
            if(!NullUtils.isEmpty(place)){
                ParkingPlace parkingPlace = parkingService.getPlaceByPlaceId(NullUtils.isEmpty(parkingPlaceList) ? null : parkingPlaceList.get(0).getId(),null,null,null);
                if(!NullUtils.isEmpty(parkingPlace)){
                    recommendedPlace.add(parkingPlace);
                    res.setData(recommendedPlace);
                    // log.error("推荐车位是："+parkingPlace.getName());

                    ParkingInfoStatistics infoStatistics = new ParkingInfoStatistics();
                    infoStatistics.setUserId(userId);
                    infoStatistics.setMap(Long.valueOf(map));
                    infoStatistics.setPlace(Long.valueOf(parkingPlace.getId()));
                    infoStatistics.setPlacename(parkingPlace.getName());
                    infoStatistics.setStartTime(LocalDateTime.now());
                    infoStatistics.setEndTime(LocalDateTime.now());
                    parkingInfoStatisticsService.addParkingInfoStatisticsRecommend(infoStatistics);

                    JSONObject jsonArea = new JSONObject();
                    List<ViewVo> findCarFrequency = viewMapper.getRecommendCarFrequency();
                    jsonArea.put("uid", "-1");
                    jsonArea.put("type", 30);
                    jsonArea.put("data",findCarFrequency);
                    appletsWebSocket.sendAll(jsonArea.toString());
                }
            }


            if ((resData != null ? resData.size() : 0) == 0 && NullUtils.isEmpty(recommendedPlace)) {
                res.setMessage("当前没有空车位，请稍后重试");
                res.setCode(500);
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }





    @RequestMapping(value = "/reservePlace")
    @ApiOperation(value = "获取车位信息", notes = "recommend")
    public CommonResult<Object> reservePlace(Integer map, double x, double y, String floor,
                                             String carBitType, String isVip) {
        try {
             log.error("车位推荐请求参数:map="+map+"&x="+x+"&y="+y+"&floor="+floor+"&carBitType="+carBitType+"&isVip="+isVip);

            // 参数预处理
            carBitType = "null".equals(carBitType) ? "0" : carBitType;
            Integer preferenceCarBit = NullUtils.isEmpty(carBitType) ? null : Integer.valueOf(carBitType);
            Integer vipPlace = "0".equals(isVip) ? null : Integer.valueOf(isVip);

            // 只处理VIP车位的情况
            if (!"1".equals(isVip)) {
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            }

            // 获取可用车位
            List<Object> availablePlaces = parkingService.getExclusiveAndFreePlaces(map, 12, null, vipPlace, null);
            if (NullUtils.isEmpty(availablePlaces)) {
                return createNoPlacesResponse();
            }

            // 排序并转换车位数据
            List<Object> sortedPlaces = getSortPlace(availablePlaces, x, y, floor, String.valueOf(preferenceCarBit));
            if (NullUtils.isEmpty(sortedPlaces)) {
                return createNoPlacesResponse();
            }

            // 转换为ParkingPlace列表
            List<ParkingPlace> parkingPlaces = convertToParkingPlaces(sortedPlaces);
            if (NullUtils.isEmpty(parkingPlaces)) {
                return createNoPlacesResponse();
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), parkingPlaces);

        } catch (Exception e) {
            log.error("获取车位信息失败", e);
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    // 辅助方法：转换为ParkingPlace列表
    private List<ParkingPlace> convertToParkingPlaces(List<Object> sortedPlaces) {
        return sortedPlaces.stream()
                .map(object -> {
                    if (!(object instanceof ParkingPlaceSort)) {
                        return null;
                    }
                    ParkingPlaceSort sort = (ParkingPlaceSort) object;
                    return new ParkingPlace().setId(sort.getId()).setName(sort.getName()).setCompany(sort.getCompany()).setCompanyName(sort.getCompanyName()).setFid(sort.getFid()).setFloor(sort.getFloor1()).setX(sort.getX()).setY(sort.getY());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // 辅助方法：创建无车位响应
    private CommonResult<Object> createNoPlacesResponse() {
        return new CommonResult<>(500, "当前没有空车位，请稍后重试");
    }

    @RequestMapping(value = "/testt")
    public String testt(@RequestBody List<Object> parkingPlaceList, @RequestParam double x, @RequestParam double y, @RequestParam String floor, @RequestParam String carBitType) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        List<Object> places = new ArrayList<>();
        for (Object obj : parkingPlaceList) {
            ParkingPlace place = mapper.convertValue(obj, ParkingPlace.class);
            places.add(place);
        }
        // 现在places中包含了转换后的ParkingPlace对象
        getSortPlace(places,x,y,floor,carBitType);
        return "";
    }

    private List<Object> getSortPlace(List<Object> parkingPlaces, double x, double y, String floor,String carBitType){
        int maxFloor = 0; // 初始化最大值为最小的floor值
        for (Object obj : parkingPlaces) {
            ParkingPlace parkingPlace = (ParkingPlace) obj;
            Integer floors = Integer.valueOf(parkingPlace.getFloor());
            if (floors.compareTo(maxFloor) > 0) {
                maxFloor = floors; // 更新最大值
            }
        }
        ArrayList<ParkingPlaceSort> res=new ArrayList<>();
        maxFloor++;
        for (Object object:parkingPlaces) {
            ParkingPlace parkingPlace=(ParkingPlace) object;
            double placeFloor = Double.parseDouble(parkingPlace.getFloor());
            double floorResults =  1 - (placeFloor / maxFloor);
            ParkingPlaceSort placeSort=new ParkingPlaceSort();
            placeSort.setId(parkingPlace.getId());
            placeSort.setLevel(parkingPlace.getPlaceLevel()*-1);
            placeSort.setName(parkingPlace.getName());
            placeSort.setCompany(parkingPlace.getCompany());
            placeSort.setCompanyName(parkingPlace.getCompanyName());
            placeSort.setFid(parkingPlace.getFid());
            placeSort.setFloor1(parkingPlace.getFloor());
            placeSort.setX(parkingPlace.getX());
            placeSort.setY(parkingPlace.getY());
            if(parkingPlace.getCarbittype().equals(carBitType)){
                placeSort.setPreferenceType(0);
            }else {
                placeSort.setPreferenceType(1);
            }
            if(parkingPlace.getCompany()!=null){
                placeSort.setExclusive(0);
            }else {
                placeSort.setExclusive(1);
            }
            if(parkingPlace.getFloor().equals(floor)){
                placeSort.setFloor(0);
                placeSort.setDis(calculateDistance(x,y,parkingPlace.getX(),parkingPlace.getY()));
            }else {
                placeSort.setFloor(floorResults);
                placeSort.setDis(calculateDistance(x,y,parkingPlace.getX(),parkingPlace.getY()));
            }
            res.add(placeSort);
        }
        Object[] array = res.toArray();
        Arrays.sort(array);
        // log.error("Arrays.sort排序后的车位是： " + Arrays.toString(array));
        return Arrays.asList(array);
    }

    private double calculateDistance(double x, double y, String placeX, String placeY) {
        return Math.sqrt((Math.pow(x-Double.parseDouble(placeX),2)+Math.pow(y-Double.parseDouble(placeY),2)));
    }

    //根据权重推荐车位
    private List<ParkingPlace> getCunnrentWeightPlace(List<Object> final_res, ConcurrentHashMap<Integer, ParkingPlace> weight){
        int len=final_res.size();
        ArrayList<ParkingPlace> sortList=new ArrayList<>();
        Date current=new Date();
        // log.error("当前时间是："+current);
//        int overTime=420000;
        int overTime=300000;
        //int overTime=480000;
        for (int k=0;k<len;k++) {
            ParkingPlaceSort placeSort=(ParkingPlaceSort) final_res.get(k);//车位排序后的值 按专属，楼层，以及距离排序
            ParkingPlace place=new ParkingPlace();// 进行排序

            place.setId(placeSort.getId());
            place.setDis(k);
            if(weight.containsKey(placeSort.getId())){
                ParkingPlace  parkingPlace_weight=weight.get(placeSort.getId());
                place.setWeight(parkingPlace_weight.getWeight());
                place.setLastRecommand(parkingPlace_weight.getLastRecommand());
                // log.error("当前车位上次推荐的时间是："+parkingPlace_weight.getLastRecommand());
                if(parkingPlace_weight.getLastRecommand()!=null&&(current.getTime()-parkingPlace_weight.getLastRecommand().getTime()>overTime)){
                    place.setWeight(0);
                    place.setLastRecommand(null);
                }
                sortList.add(place);
            }else{
                place.setWeight(0);
                ParkingPlace coPakingPlace=new ParkingPlace();
                coPakingPlace.setWeight(0);
                coPakingPlace.setId(placeSort.getId());
                weight.put(placeSort.getId(),coPakingPlace);
                sortList.add(place);
            }
        }
        Object[] array = sortList.toArray();
        Arrays.sort(array);
        List<ParkingPlace> res=new ArrayList<>();
        for (Object recommand:array) {
            ParkingPlace recommandPlace = (ParkingPlace) recommand;
            if(recommandPlace.getWeight()==0&&(recommandPlace.getLastRecommand()==null||current.getTime()-recommandPlace.getLastRecommand().getTime()<overTime)){
                res.add(recommandPlace);
                ParkingPlace refresh = weight.get(recommandPlace.getId());
                refresh.setWeight(refresh.getWeight()+1);
                refresh.setLastRecommand(current);
                weight.replace(recommandPlace.getId(),refresh);
                break;
            }
        }
        return res;
    }
    
    @RequestMapping("delPlaceRecycleById")
    public String delPlaceRecycleById(Integer id){
        del(id, SpringContextHolder.parkingPlaceConcurrentHashMap);
        return JSON.toJSONString(SpringContextHolder.parkingPlaceConcurrentHashMap);
    }
   public void del(Integer id,ConcurrentHashMap<Integer, ParkingPlace> weight){
       weight.entrySet().removeIf(entry -> entry.getKey().equals(id));
   }

        /**
         * @param list
         * @param x
         * @param y
         * @param floor
         * @return 对停车位列表进行排序的方法，并返回排好序的停车位列表。
         */
    public static Object[] sortPlace(List<ParkingPlace> list, String x, String y, String floor) {
        if (x != null) {
            for (ParkingPlace p : list) {
                if (floor == null || p.getFloor() == null || (Integer.valueOf(floor.trim()).intValue() != Integer.valueOf(p.getFloor().trim()).intValue())) {
                    p.setDis(2000 + Math.sqrt(Math.pow((Double.parseDouble(x) - Double.parseDouble(p.getX())), 2) + Math.pow((Double.parseDouble(y) - Double.parseDouble(p.getY())), 2d)));
                } else {
                    p.setDis(Math.sqrt(Math.pow((Double.parseDouble(x) - Double.parseDouble(p.getX())), 2) + Math.pow((Double.parseDouble(y) - Double.parseDouble(p.getY())), 2d)));
                }
            }
        }
        Object[] array = list.toArray();
        Arrays.sort(array);
        return array;
    }



    @RequestMapping(value = "/updatePlaces")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public CommonResult<Object> updatePlaces(String placeids, Integer companyid) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            parkingService.updatePlaces(placeids.split(","), companyid, null);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/updatePlaceDataByPlaceId")
    @ApiOperation(value = "更新车位信息", notes = "111")
    public CommonResult<Object> updatePlaceDataByPlaceId(@RequestBody Map<String,Object> params){
        try {

            PlaceUseRecord placeRecord = new PlaceUseRecord();
            placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
            PlaceVo placeVo = JSONObject.parseObject(JSONObject.toJSONString(params.get("params")), PlaceVo.class);
            ParkingPlace place = new ParkingPlace();
            place.setId(placeVo.getId());
            place.setMap(placeVo.getMap());
            place.setState(placeVo.getState());
            place.setLicense(placeVo.getLicense());
            place.setEntryTime(placeVo.getEntryTime());
            parkMapper.updatePlace(place);
            List<ParkingPlace> places = parkMapper.getPlaceById(place.getId());
            JSONObject jsonArea = new JSONObject();
            List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
            if(!NullUtils.isEmpty(placeVo.getLicense())){
                placeRecord.setPlace(place.getId());
                placeRecord.setMap(place.getMap());
                // log.error("updatePlaceDataByPlaceId：selectPlaceUseRecordByPlaceidAndMapid"+place.getMap());
                LocalDateTime startTime = TimeUtil.strTimeToLocalDateTime(placeRecord.getStart());
                // log.error("updatePlaceDataByPlaceId："+placeRecords.toString());
                if(placeRecords.size()>1){
                    places.forEach(placeIds -> bookMapper.delPlaceUseRecord(place.getMap(), place.getId()));
                    placeRecord.setMap(placeRecord.getMap());
                    placeRecord.setPlace(placeRecord.getPlace());
                    placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
                    bookMapper.addPlaceUseRecord(placeRecord);
                }else {
                    if(NullUtils.isEmpty(placeRecords) || !(placeRecords.size() ==1)) {
                        if (bookMapper.addPlaceUseRecord(placeRecord) > 0) {
                            ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
                            jsonArea.put("type", 27);
                            jsonArea.put("data", realTimeInAndOutData);
                            appletsWebSocket.sendAll(jsonArea.toString());
                        }
                    }
                }
            }else {
                if(!NullUtils.isEmpty(placeRecords)) {
                    placeRecord.setId(placeRecords.get(0).getId());
                    placeRecord.setEnd(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placeRecord.getTimestamp()), ZoneId.of("Asia/Shanghai"));
                    LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Shanghai"));
                    Duration duration = Duration.between(localDateTime, currentDateTime);
                    long diffInMinutes = Math.abs(duration.toMinutes());
                    if (diffInMinutes < 5) {
                        SpringContextHolder.parkingPlaceConcurrentHashMap.remove(place.getId());
                    }

                    bookMapper.UpdatePlaceUseRecordByid(placeRecord);
                    ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
                    jsonArea.put("type", 27);
                    jsonArea.put("data", realTimeInAndOutData);
                    appletsWebSocket.sendAll(jsonArea.toString());
                }
            }
            return new CommonResult<>(200, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    public void add(PlaceUseRecord placeRecord,ParkingPlace place,List<ParkingPlace> places) throws ParseException {
        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),"time");
        if(placeRecords.size()>1){
            places.forEach(placeIds -> bookMapper.delPlaceUseRecord(place.getMap(), place.getId()));
        }else {
            List<PlaceUseRecord> place2 = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(),null);
            if(place2.size()<1){
                if (bookMapper.addPlaceUseRecord(placeRecord) > 0) {
                    JSONObject jsonArea = new JSONObject();
                    ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
                    jsonArea.put("type", 27);
                    jsonArea.put("data", realTimeInAndOutData);
                    appletsWebSocket.sendAll(jsonArea.toString());
                }
            }
        }

    }

    public void update(PlaceUseRecord placeRecord,ParkingPlace place) throws ParseException {
        net.sf.json.JSONObject jsonArea = new net.sf.json.JSONObject();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placeRecord.getTimestamp()), ZoneId.of("Asia/Shanghai"));
        LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Shanghai"));
        Duration duration = Duration.between(localDateTime, currentDateTime);
        long diffInMinutes = Math.abs(duration.toMinutes());
        if (diffInMinutes < 5) {
            SpringContextHolder.parkingPlaceConcurrentHashMap.remove(place.getId());
        }

        bookMapper.UpdatePlaceUseRecordByid(placeRecord);
        ViewVo realTimeInAndOutData = viewMapper.getSingleAddOrUpdateCarBitUseRecord(place.getMap(), place.getId());
        jsonArea.put("type", 27);
        jsonArea.put("data", realTimeInAndOutData);
        appletsWebSocket.sendAll(jsonArea.toString());
    }

    /**
     * 导入车位
     */
    @RequestMapping(value = "/importPlace")
    @ApiOperation(value = "导入车位", notes = "111")
    public CommonResult<String> importPlace(String fmapID, MultipartFile file) {
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename()).substring(Objects.requireNonNull(file.getOriginalFilename()).lastIndexOf("."));
        boolean filename = ".xls".equalsIgnoreCase(originalFilename.substring(originalFilename.lastIndexOf(".")));
        boolean filename2 = ".xlsx".equalsIgnoreCase(originalFilename.substring(originalFilename.lastIndexOf(".")));
        if (!filename && !filename2) {
            return new CommonResult<>(400, LocalUtil.get("对不起，导入数据格式必须是xls或xlsx格式文件哦，请您调整格式后重新上传，谢谢 ！"));
        }

        String res = "";
        if (!file.isEmpty()) {
            try {
                JSONObject jsonArea = new JSONObject();
                res = parkingService.importLabelFromExcel(file, fmapID);

                List<ViewVo> carBitAndHardwareInfo = viewMapper.getCarBitAndHardwareInfo();
                jsonArea.put("uid", "-1");
                jsonArea.put("type", 22);
                jsonArea.put("data",carBitAndHardwareInfo);//车位以及硬件统计信息
                appletsWebSocket.sendAll(jsonArea.toString());
                return new CommonResult<>(220, res);
            } catch (ImportUsersException ex) {
                return new CommonResult<>(400, ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
            }
        } else {
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.IMPORT_FAIL_EMPTYFILE));
        }
    }

    /**
     * 导入出入口
     */
    @RequestMapping(value = "/importExit")
    public CommonResult<String> importExit(String fmapID, MultipartFile file) {
        if (!file.isEmpty()) {
            try {

                int i = parkingService.importExitFromExcel(file, fmapID);
                if (i == 0) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.CONTENT_EMPTY));
                } else if (i > 0) {
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.IMPORT_SUCCESS) + i + LocalUtil.get(KafukaTopics.N_COUNTINFO));
                } else {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.IMPORT_FAIL_ERRORFORMAT));
                }
            } catch (ImportUsersException ex) {
                return new CommonResult<>(400, ex.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
            }
        } else {
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.IMPORT_FAIL_EMPTYFILE));
        }
    }

    @RequestMapping(value = "/bindInfraredtoPlace")
    @ApiOperation(value = "绑定车位检测器到指定车位", notes = "111")
    public CommonResult<Object> bindPlaces(String mac, Integer map, String placeName, String fid) {
        try {

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get("绑定成功"));
            String rawProductId = mac.substring(mac.length()-6);
            Integer decimalNum = Integer.parseInt(rawProductId,16);

            List<ParkingPlace> places = parkingService.findPlaceByMapAndName(map, placeName.toUpperCase(), fid);
            if (places == null || places.size() == 0) {
                res.setMessage("未查询到该车位信息");
                res.setCode(500);
                return res;
            }
            if (places.size() > 1) {
                res.setMessage("该地图存在同名车位,请输入有效fid");
                res.setCode(500);
            }

            List<Infrared> infreds = tagMapper.findIredByIdAndName(null, null,String.valueOf(decimalNum));
            if (infreds != null && infreds.size() > 0) {
                Infrared infred = (Infrared) infreds.get(0);
                infred.setPlace(places.get(0).getId());
                infred.setMap(map);
                tagMapper.updateInfrared(infred);
            } else {
                Infrared infred = new Infrared();
                infred.setPlace(places.get(0).getId());
                infred.setMap(map);
                infred.setNum(String.valueOf(decimalNum));
                infred.setRawProductId(mac);
                infred.setNetworkstate((short) 0);
                infred.setStatus((short) 1);
                infred.setLocalDateTime(LocalDateTime.now());
                tagMapper.addInfrared(infred);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    /**
     * 获取查询
     */
    @RequestMapping(value = "/getSimulateTrail")
    public CommonResult<Object> importExit(Integer map) {
        CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get("查询成功"));
        try {
            List<SimulateTrail> data = parkMapper.getSimulateTrail(map);
            Map_2d mapInf = map2dService.findById(map);
            if(NullUtils.isEmpty(mapInf)){
                return new CommonResult<>(400, LocalUtil.get("当前展示的地图已经不存在，请关闭当前页面"));
            }
            if(mapInf.getEnable().equals(0)){
                return new CommonResult<>(400, LocalUtil.get("当前展示的地图已经被禁用，请关闭当前页面，启用地图！！！"));
            }
            JSONObject json = new JSONObject();
            json.put("fID", mapInf.getFmapID());
            json.put("fKey", mapInf.getMapKey());
            json.put("fName", mapInf.getName());
            json.put("appName", mapInf.getAppName());
            json.put("themeName", mapInf.getThemeName());
            json.put("list", data);
            res.setData(json);
        } catch (ImportUsersException ex) {
            return new CommonResult<>(400, ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
        }
        return res;
    }

    /*
     * 获取查询
     * */
    @RequestMapping(value = "/getTrailRecordByMap")
    public CommonResult<Object> getTrailRecordByMap(Integer map) {
        CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get("查询成功"));
        try {
            List<RealTrail> reals = parkMapper.getRealTrail(map);
            JSONArray array = new JSONArray();
            for (RealTrail real : reals
            ) {
                List<WechatUserPosition> trailRecord = collectMapper.getWechatPosition(real.getUid(), map, real.getStart(), real.getEnd());
                JSONObject json = new JSONObject();
                json.put("id", real.getId());
                json.put("name", real.getName());
                json.put("list", trailRecord);
                array.add(json);
            }
            res.setData(array);

        } catch (ImportUsersException ex) {
            return new CommonResult<>(400, ex.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
        }
        return res;
    }

    @GetMapping("/queryUserPositions")
    public CommonResult<Object> queryUserPositions(@RequestParam int userId,@RequestParam int mapId,@RequestParam String startTime,@RequestParam String endTime) {
        Timestamp startTimestamp = Timestamp.valueOf(startTime + ":00:00");
        Timestamp endTimestamp = Timestamp.valueOf(endTime + ":59:59");
        List<WechatUserPosition> wechatUserPositionList = collectMapper.findByUserIdAndMapIdAndTimeBetween(userId, mapId, startTimestamp, endTimestamp);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),wechatUserPositionList);
    }
}
