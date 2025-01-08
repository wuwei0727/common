package com.tgy.rtls.web.controller.park;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.ByteUtils;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.*;
import com.tgy.rtls.data.entity.park.floorLock.CarPlate;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.floorLock.impl.CarPlateService;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
import com.tgy.rtls.data.service.sms.impl.SmsService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.aspect.MyPermission;
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
import java.util.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/park")
/**
 * 公司管理
 */
public class ComController {
    @Autowired
    private ParkingService parkingService;
    @Autowired(required = false)
    private BookMapper bookMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MemberService memberService;
    @Autowired
    SmsService smsService;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private ParkMapper parkMapper;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private CarPlateService carPlateService;
    @Autowired
    private UserCompanyMapService userCompanyMapService;
    @Autowired
    private TimePeriodAdminService timePeriodAdminService;


    @MyPermission
    @RequestMapping(value = "/getCompany")
    @ApiOperation(value = "获取公司名", notes = "111")
    public CommonResult<Object> getCompany(String name, Integer map,String floorName, Integer pageIndex, Integer pageSize, String maps) {
        try {
            Object member = SecurityUtils.getSubject().getPrincipal();

            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if(pageSize!=-1) {
                PageHelper.startPage(pageIndex, pageSize);
            }
            List<ParkingCompany> data = parkingService.findByAllCompany2(null, name, map, null, null, null,floorName, mapids);
            List<ParkingCompanyVo> mapComName = parkMapper.getAllMap(mapids);
            PageInfo<ParkingCompany> pageInfo = new PageInfo<>(data);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("mapComName",mapComName);
            if(pageSize!=null&&pageSize!=-1) {
                res.setData(result);
            } else {
                if (member instanceof ParkingCompany) {
                    ParkingCompany p = (ParkingCompany) member;
                    ArrayList<Object> list = new ArrayList<>();
                    list.add(parkingService.findCompanyByPhone(p.getPhone()));
                    res.setData(list);
                }else {
                    res.setData(data);
                }

            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @MyPermission
    @RequestMapping(value = "/getCompany1")
    @ApiOperation(value = "获取公司名", notes = "111")
    public CommonResult<Object> getCompany1(Integer map) {
        try {

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));

            List<ParkingCompany> data = parkingService.findByAllCompany2(null, null, map, null, null, null,null, null);
            res.setData(data);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions(value = {"cpy:see","cpy:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getCompanyById/{id}")
    @ApiOperation(value = "获取公司信息", notes = "111")
    public CommonResult<Object> getCompany(@PathVariable("id") Integer id, String name) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ParkingCompany> data = parkingService.findByAllCompany(id, name, null, null, null, null);
            Map_2d map2ds = parkingService.findByCompanyMap(id);
            Map<String, Object> result = new HashMap<>();
            result.put("id", data.get(0).getId());
            result.put("user", data.get(0).getUser());
            result.put("phone", data.get(0).getPhone());
            result.put("name", data.get(0).getName());
            result.put("mapName", map2ds != null ? map2ds.getName() : null);
            result.put("pwd", data.get(0).getPwd());
            result.put("data", data);
            result.put("list", parkingService.findByAllPlace(null, null, null, null, null, null, id, null, null, null, null, null, null, null, null));

            res.setData(result);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"cpy:add"})
    @RequestMapping(value = "/addCompany")
    @ApiOperation(value = "添加公司", notes = "111")
    public CommonResult<Object> addCompany(ParkingCompany parkingCompany, String addplaceids, String delplaceids,HttpServletRequest request) {
        try {LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            if (parkingCompany.getPhone() == null || !ByteUtils.isPhoneLegal(parkingCompany.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }

            List<ParkingCompany> objectList = parkMapper.getComByxyf(parkingCompany.getX(),parkingCompany.getY(),parkingCompany.getMap(),parkingCompany.getFloor(),parkingCompany.getFid(),null);
            if(objectList.size() != 0) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.THERE_ARE_ALREADY_OTHER_COMPANY_AT_THE_CURRENT_LOCATION));
            }
            List<ParkingCompany> parkingCompanyList = parkingService.getComByName(parkingCompany.getName());
            if (!NullUtils.isEmpty(parkingCompanyList)) {
                return new CommonResult<>(400, LocalUtil.get("公司名称不能重复！！！"));
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String instanceid = redisService.get("instance" + uid);
            if (parkingCompany.getPhone() != null) {
                Member member1 = memberService.findByPhone(parkingCompany.getPhone());
                ParkingCompany company = parkingService.findCompanyByPhone(parkingCompany.getPhone());
                if (member1 != null || company != null) {
                    res.setCode(500);
                    res.setMessage("手机号码冲突");
                    return res;
                }
            }
            parkingCompany.setInstanceid(instanceid);
            parkingCompany.setAddTime(new Date());
            if ("".equals(parkingCompany.getX()) && "".equals(parkingCompany.getY())) {
                return new CommonResult<>(400, "请在下方地图选点");
            } else {
                parkingService.addCompany(parkingCompany);
            }
            if (addplaceids != null) {
                parkingService.updatePlaces(addplaceids.split(","), parkingCompany.getId(), null);
            }
            if (delplaceids != null) {
                parkingService.updatePlaces(delplaceids.split(","), null, parkingCompany.getId());
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.COMPANY_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions(value = {"cpy:edit"})
    @RequestMapping(value = "/updateCompany")
    @ApiOperation(value = "修改公司信息", notes = "111")
    public CommonResult<Object> updateCompany(ParkingCompany parkingCompany, String addplaceids, String delplaceids,HttpServletRequest request) {
        try {LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            List<ParkingCompany> parkingCompanyList = parkingService.getComByNameId(parkingCompany.getName(), parkingCompany.getId(),null);
            if (!NullUtils.isEmpty(parkingCompanyList)) {
                if (parkingCompanyList.size() > 1) {
                    return new CommonResult<>(400, LocalUtil.get("公司名称不能重复！！！"));
                }
            }

            List<ParkingCompany> objectList = parkMapper.getComByxyf(parkingCompany.getX(),parkingCompany.getY(),parkingCompany.getMap(),parkingCompany.getFloor(),parkingCompany.getFid(),parkingCompany.getId());
            if(objectList.size() != 0) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.THERE_ARE_ALREADY_OTHER_COMPANY_AT_THE_CURRENT_LOCATION));
            }

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            if (parkingCompany.getPhone() != null) {
                Member member1 = memberService.findByPhone(parkingCompany.getPhone());
                ParkingCompany company = parkingService.findCompanyByPhone(parkingCompany.getPhone());
                if (member1 != null || company != null && !company.getId().equals(parkingCompany.getId())) {
                    res.setCode(500);
                    res.setMessage("手机号码冲突");
                    return res;
                }
            }
            //之前的地图
            List<ParkingCompany> data = parkingService.findByAllCompany(parkingCompany.getId(),null, null, null, null, null);
            Map_2d map2ds = parkingService.findByCompanyMap(parkingCompany.getId());
            List<StorePlace> storePlaceByName =null;
            if(!parkingCompany.getMap().equals(String.valueOf(map2ds.getId()))){
                if(!data.get(0).getName().equals(parkingCompany.getName())){
                    storePlaceByName = bookMapper.getStorePlaceByName(data.get(0).getName());
                    if(!NullUtils.isEmpty(storePlaceByName)){
                        for (StorePlace storePlace : storePlaceByName) {
                            bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                        }
                    }
                }else {
                    storePlaceByName = bookMapper.getStorePlaceByName(data.get(0).getName());
                }

                List<StorePlace> storePlaceByFid=null;
                if(!data.get(0).getFid().equals(parkingCompany.getFid())){
                    storePlaceByFid = bookMapper.getStorePlaceByFid(data.get(0).getFid());
                    if(!NullUtils.isEmpty(storePlaceByFid)){
//                        for (StorePlace storePlace : storePlaceByFid) {
//                            storePlace.setMap(Integer.valueOf(parkingCompany.getMap()));
//                            storePlace.setX(parkingCompany.getX());
//                            storePlace.setY(parkingCompany.getY());
//                            storePlace.setFid(parkingCompany.getFid());
//                            storePlace.setFloor(parkingCompany.getFloor());
//                            bookMapper.updateStorePlace(storePlace);
                            String numStr = Integer.toString(storePlaceByFid.get(0).getId());
                            bookMapper.delStorePlace(numStr.split(","));
//                        }
                    }else {
                        for (StorePlace storePlace : storePlaceByFid) {
                            storePlace.setMap(Integer.valueOf(parkingCompany.getMap()));
                            storePlace.setX(parkingCompany.getX());
                            storePlace.setY(parkingCompany.getY());
                            storePlace.setFid(parkingCompany.getFid());
                            storePlace.setFloor(parkingCompany.getFloor());
                            bookMapper.updateStorePlace(storePlace);
                        }
                    }
                }

                parkingService.updateCompany(parkingCompany);
                if(!NullUtils.isEmpty(storePlaceByName)){
                    for (StorePlace storePlace : storePlaceByName) {
//                        storePlace.setId(storePlace.getId());
                        storePlace.setMap(Integer.valueOf(parkingCompany.getMap()));
                        storePlace.setX(parkingCompany.getX());
                        storePlace.setY(parkingCompany.getY());
                        storePlace.setFid(parkingCompany.getFid());
                        storePlace.setFloor(parkingCompany.getFloor());
                        bookMapper.updateStorePlace(storePlace);
                    }
                }
                if (addplaceids != null) {
                    parkingService.updatePlaces(addplaceids.split(","), parkingCompany.getId(), null);
                }
                if (delplaceids != null) {
                    parkingService.updatePlaces(delplaceids.split(","), null, parkingCompany.getId());
                }
                List<ParkingPlace> parkingPlaces = parkingService.getPlaceByComId(parkingCompany.getId());
                for (ParkingPlace parkingPlace : parkingPlaces) {
                    parkingPlace.setCompany(null);
                    parkingService.updatePlaceByComId(parkingPlace);
                }
                parkingService.updateCompany(parkingCompany);
                if (addplaceids != null) {
                    parkingService.updatePlaces(addplaceids.split(","), parkingCompany.getId(), null);
                }
                if(delplaceids!=null) {
                    parkingService.updatePlaces(delplaceids.split(","), null, parkingCompany.getId());
                }

            }else{
                //List<ParkingPlace> parkingPlaces = parkingService.getPlaceByComId(parkingCompany.getId());
                //for (ParkingPlace parkingPlace : parkingPlaces) {
                //    parkingPlace.setCompany(null);
                //    parkingService.updatePlaceByComId(parkingPlace);
                //}
                parkingService.updateCompany(parkingCompany);
                if (!NullUtils.isEmpty(addplaceids) ) {
                    parkingService.updatePlaces(addplaceids.split(","), parkingCompany.getId(), null);
                }
                if(delplaceids!=null) {
                    parkingService.updatePlaces(delplaceids.split(","), null, parkingCompany.getId());
                }
            }
            if(!data.get(0).getName().equals(parkingCompany.getName())){
                storePlaceByName = bookMapper.getStorePlaceByName(data.get(0).getName());
                if(!NullUtils.isEmpty(storePlaceByName)){
                    for (StorePlace storePlace : storePlaceByName) {
                        bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                    }
                }
            }else {
                storePlaceByName = bookMapper.getStorePlaceByName(data.get(0).getName());
            }
            if(!NullUtils.isEmpty(storePlaceByName)){
                for (StorePlace storePlace : storePlaceByName) {
//                    storePlace.setId(storePlace.getId());
                    storePlace.setMap(storePlace.getMap());
                    storePlace.setX(parkingCompany.getX());
                    storePlace.setY(parkingCompany.getY());
                    storePlace.setFid(parkingCompany.getFid());
                    storePlace.setFloor(parkingCompany.getFloor());
                    bookMapper.updateStorePlace(storePlace);
                }
            }
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.COMPANY_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequiresPermissions({"cpy:del"})
    @RequestMapping(value = "/delCompany/{ids}")
    @ApiOperation(value = "删除公司", notes = "111")
    public CommonResult<Object> delCompany(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {LocalDateTime now = LocalDateTime.now();
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));

            for (String id : ids.split(",")) {
                ParkingCompany com = parkingService.getComById(id);
                if(!NullUtils.isEmpty(com)){
                    List<StorePlace> storePlaceByName = bookMapper.getStorePlaceByName(com.getName());
                    if(!NullUtils.isEmpty(storePlaceByName)){
                        for (StorePlace storePlace : storePlaceByName) {
                            bookMapper.delStorePlaceById(String.valueOf(storePlace.getId()));
                        }
                    }
                }

                List<CarPlate> carPlateList = carPlateService.list(new QueryWrapper<CarPlate>().eq("company_id", id));
                if(!NullUtils.isEmpty(carPlateList)){
                    carPlateList.forEach(carPlate->{
                        carPlate.setCompanyId(null);
                        carPlate.setCompanyName(null);
                    });
                    carPlateService.updateBatchById(carPlateList);
                }

                List<UserCompanyMap> userList = userCompanyMapService.list(new QueryWrapper<UserCompanyMap>().eq("company_id", id));
                if(!NullUtils.isEmpty(userList)){
                    userList.forEach(user->{
                        user.setCompanyId(null);
                        user.setCompanyName(null);
                    });
                    userCompanyMapService.updateBatchById(userList);
                }

                List<TimePeriodAdmin> timeList = timePeriodAdminService.list(new QueryWrapper<TimePeriodAdmin>().eq("company_id", id));
                if(!NullUtils.isEmpty(timeList)){
                    timeList.forEach(time -> {
                        time.setCompanyId(null);
                        time.setCompanyName(null);
                    });
                    timePeriodAdminService.updateBatchById(timeList);
                }
            }
            parkingService.deleteCompany(ids.split(","));
            String ip = IpUtil.getIpAddr(request);
            String address = ip2regionSearcher.getAddressAndIsp(ip);
            operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.COMPANY_INFO)), now);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getFloor")
    @ApiOperation(value = "删除公司", notes = "111")
    public CommonResult<Object> getFloorByid(Integer map) {
        try {
            List floor = parkingService.findFloorByMapid(map);
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            res.setData(floor);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getRealTimeData")
    @ApiOperation(value = "删除公司", notes = "111")
    public CommonResult<Object> getRealTimeData(Integer map) {
        try {
            RealTimeData realTimeData = bookMapper.selectRealTimeData(map);
             BeaconCount data = subMapper.findCalcuuByMap(map);
            realTimeData.setTotal(data.getTotal());
            realTimeData.setOnLine(data.getOnLine());
            realTimeData.setOffLine(data.getOffLine());
            realTimeData.setSubLowPower(data.getSubLowPower());
            realTimeData.setDetectorLowPower(data.getDetectorLowPower());
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            res.setData(realTimeData);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getPlaceUseRatio")
    @ApiOperation(value = "查询车位使用率", notes = "111")
    public CommonResult<Object> getPlaceUseRatio(Integer map) {
        try {
            List floor = parkingService.findFloorByMapid(map);
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            res.setData(floor);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
}
