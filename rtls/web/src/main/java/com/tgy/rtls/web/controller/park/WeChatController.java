package com.tgy.rtls.web.controller.park;

import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.common.DateKit;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.*;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.entity.vip.ParkingInfoStatistics;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.view.EachMapSearchBusinessService;
import com.tgy.rtls.data.service.vip.FloorLockService;
import com.tgy.rtls.data.service.vip.ParkingInfoStatisticsService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import com.tgy.rtls.data.tool.Constant;
import com.tgy.rtls.data.tool.Gps_xy;
import com.tgy.rtls.web.controller.view.AppletsWebSocket;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping(value = "/wechat")
public class WeChatController {
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private BookMapper bookMapper;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Autowired
    private ParkingInfoStatisticsService parkingInfoStatisticsService;
    @Autowired
    private AppletsWebSocket appletsWebSocket;
    @Autowired
    private ViewMapper viewMapper;
    @Autowired
    private FloorLockService floorLockService;
    @Autowired
    private EachMapSearchBusinessService businessService;
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private RedissonClient redissonClient;

    private static final String appId = "wxf0f25ad3fc36365e";





    @RequestMapping(value = "/getPlaceByMap")
    @ApiOperation(value = "地图选择", notes = "111")
    public CommonResult<Object> getPlace(Integer place_type, Integer mapId,Integer hasVIP) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),parkingService.findPlaceCountGroupByMap(1,place_type,mapId,hasVIP).get());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }


    @RequestMapping(value = "/getPlaceById")
    @ApiOperation(value = "当前地图下可预约车位", notes = "111")
    public CommonResult<Object> getPlaceByMapAndCompanyName(Integer map, String name, String status,Integer place_type) {
        try {
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), parkingService.findPlaceCountByMap(map, 1, name, status,place_type).get());
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/bookPlace")
    @ApiOperation(value = "预定车位", notes = "111")
    @Transactional
    public CommonResult<Object> getPlaceByMap(Integer id, String license, String start, String end,String phone,String floor) {
        try {
            if (license != null) {
                license.toUpperCase();
            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
            SimpleDateFormat SF = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date startDate = (SF.parse(start));
            Date endDate = (SF.parse(end));
            if (startDate.getTime() > endDate.getTime()) {
                res.setCode(500);
                res.setMessage("出场时间早于入场时间，请重新选择时间");
                return res;
            }

            List<ParkingPlace> places = parkingService.findByAllPlace(id, null, null, null, null, null, null, null, null, null, null, null, null, null,null);
            List<VipParking> existingBookings = vipParkingService.selectBookPlaceByLicenseAndTime(places.get(0).getMap(),license,start,end);
            if (!NullUtils.isEmpty(existingBookings)) {
                res.setCode(500);
                res.setMessage("该车牌已经在相同时间段内预约了其他车位");
                return res;
            }

            List<BookPlace> bookinfs = bookMapper.selectBookPlaceConfix(id);
            List<FloorLock> flId = floorLockService.getConditionData(null, id, null, null);
            ParkingPlace place = null;
            BookPlace bookPlace = new BookPlace();



           if (places != null && places.size() > 0) {
                bookPlace.setPlaceName(places.get(0).getName());
                place = places.get(0);
                if (bookinfs != null && bookinfs.size() > 0) {
                    Boolean confixFinal = false;
                    for (BookPlace bookinf : bookinfs) {
                        String[] history = {bookinf.getStart(), bookinf.getEnd()};
                        String[] bookTime = {start, end};
                        boolean confix = DateKit.isContainEnd(history, bookTime, "yyyy-MM-dd HH:mm");
                        confixFinal = confixFinal || confix;
                    }

                    if (confixFinal) {
                        res.setCode(500);
                        res.setMessage("该车位时段已经被预约");
                        return res;
                    }
                }
           } else {
                res.setCode(500);
                res.setMessage("未查询到该车位信息");
                return res;
           }
            try {
                Session session = SecurityUtils.getSubject().getSession();
                String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
                if (openid == null) {
                    res.setCode(400);
                    res.setMessage("用户未登录");
                    return res;
                }

                WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
                if (user == null) {
                    user = new WeChatUser();
                    user.setUserid(openid);
                    bookMapper.addWechat(user);
                }
                bookPlace.setUserid(user.getId());
            } catch (Exception e) {
                res.setCode(500);
                res.setMessage("系统错误");
                return res;
            }
            // place.setState((short)2);
            //  parkingService.updatePlace(place);
            bookPlace.setPlace(id);
            if(!NullUtils.isEmpty(flId)){
                bookPlace.setFloorLockId(String.valueOf(flId.get(0).getDeviceNum()));
            }
            bookPlace.setTime(LocalDateTime.now());
            bookPlace.setPlaceName(places.get(0).getName());
            bookPlace.setLicense(license);
            bookPlace.setMap(place.getMap());
            bookPlace.setStart(start);
            bookPlace.setEnd(end);
            bookPlace.setFee(3f);
            bookPlace.setPhone(phone);
            bookPlace.setFloor(place.getFloor());
            bookPlace.setSource(1);

            // 添加状态处理
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));


            if (startTime.isAfter(now)) {
                bookPlace.setStatus(2);
            } else {
                bookPlace.setStatus(1);
            }
            bookMapper.addBookPlace(bookPlace);
            if(bookPlace.getStatus()==1){
                setExpirationHandler(bookPlace.getId(), endTime);
            }else if(bookPlace.getStatus()==2&&startTime.isAfter(now)) {
                String activeKey = "vip:parking:active," + bookPlace.getId();
                RBucket<String> activeBucket = redissonClient.getBucket(activeKey);
                long activeTime = Duration.between(now, startTime).toMillis();
                activeBucket.set(bookPlace.getId().toString(), activeTime, TimeUnit.MILLISECONDS);

                setExpirationHandler(bookPlace.getId(), endTime);
            }
            res.setMessage("预约成功");
            res.setData(bookPlace);
            return res;

        } catch (Exception e) {

            e.printStackTrace();
            return new CommonResult<>(500, "预约失败");
        }

    }

    private void setExpirationHandler(Integer id,LocalDateTime endTime) {
        String redisKey = "vip:parking:expire," + id;
        RBucket<String> bucket = redissonClient.getBucket(redisKey);

        // 计算过期时间
        long expireTime = Duration.between(LocalDateTime.now(), endTime).toMillis();
        if (expireTime > 0) {
            bucket.set(id.toString(), expireTime, TimeUnit.MILLISECONDS);
        }
    }

    @RequestMapping(value = "/getFee")
    @ApiOperation(value = "获取缴费信息", notes = "111")
    public CommonResult<Object> geFee(String license, Integer map) {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            Object openid = session.getAttribute(Constant.USER_WXSESSION_ID);
            String uid = "12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List bookPlace = bookMapper.selectFeeByLicenseAndMap(license, map, 0);
            res.setData(bookPlace);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getStorePlace")
    @ApiOperation(value = "地点收藏", notes = "111")
    public CommonResult<Object> getStorePlace(Integer map,Integer userId) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_MESSAGE));
            String openid = null;
            Session session = SecurityUtils.getSubject().getSession();
            openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);
            if (openid == null&&NullUtils.isEmpty(user)) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            if (NullUtils.isEmpty(user)) {
                user = new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            List data = bookMapper.getStorePlace(user.getId(), map);
            res.setData(data);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/addStorePlace")
    @ApiOperation(value = "地点收藏", notes = "111")
    public CommonResult<Object> storePlace(StorePlace storePlace,Integer userId) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.STORE_SUCCESS));
            String openid = null;
            Session session = SecurityUtils.getSubject().getSession();
            openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);
            if (openid == null&&NullUtils.isEmpty(user)) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            if (NullUtils.isEmpty(user)) {
                user = new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            storePlace.setUserid(user.getId());
            bookMapper.addStorePlace(storePlace);
            res.setData(storePlace);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/delStorePlace/{ids}")
    @ApiOperation(value = "取消收藏", notes = "111")
    public CommonResult<Object> delStorePlace(@PathVariable("ids") String ids) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UNSTORE_SUCCESS));
            bookMapper.delStorePlace(ids.split(","));
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getExitWechat")
    @ApiOperation(value = "获取出入口信息", notes = "111")
    public CommonResult<Object> geFeeExit(Integer map) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List exit = parkMapper.findExit(null, null, map, 1);
            res.setData(exit);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getCrossFloor")
    @ApiOperation(value = "获取跨楼层出入口", notes = "111")
    public CommonResult<Object> getCrossFloor(Integer map) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List exit = parkMapper.getCrossFloorByMap(map);
            res.setData(exit);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getExitPlatForm")
    @ApiOperation(value = "获取出入口信息", notes = "111")
    public CommonResult<Object> getExitPlatForm(Double lng, Double lat, Integer map, String x, String y) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ParkingExit> exits = parkMapper.findExit(null, null, map, 1);
            double mini = 10000000000000d;
            ParkingExit near_exit = null;
            for (ParkingExit exit1 : exits
            ) {
                double[] xy = Gps_xy.lonLat2Mercator(lng, lat);

                double[] pos1 = {xy[0], xy[1], 0};
                double[] pos2 = {Double.valueOf(exit1.getDoorx() == null ? exit1.getX() : exit1.getDoorx()), Double.valueOf(exit1.getDoory() == null ? exit1.getY() : exit1.getDoory()), 0};
                //double dis = PercentToPosition.getDis(pos1, pos2);
                double dis =0;
                double dis0 = 0;
                if (x != null) {
                    double[] pos3 = {Double.valueOf(x), Double.valueOf(y), 0};
                    double[] pos4 = {Double.valueOf(exit1.getX()), Double.valueOf(exit1.getY()), 0};
                    dis0 = PercentToPosition.getDis(pos3, pos4);
                }
                dis = dis + dis0;
                if (dis < mini) {
                    mini = dis;
                    near_exit = exit1;
                }
            }

            res.setData(near_exit);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getBookInf")
    @ApiOperation(value = "微信获取用户预订信息", notes = "111")
    public CommonResult<Object> geFee(String license, HttpServletRequest request,Integer mapId,Integer userId) {
        try {
            String openid = null;
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            try {
                Session session = SecurityUtils.getSubject().getSession();
                openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            } catch (Exception ignored) {

            }

            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);
            if (openid == null&& NullUtils.isEmpty(user)) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }

            if (NullUtils.isEmpty(user)) {
                user = new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            List<BookPlace> list = bookMapper.findBookInfoByUserid(license, user.getId(),mapId);
            res.setData(list);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/bookPlaceUpdate")
    @ApiOperation(value = "更新预约信息", notes = "111")
    public CommonResult<Object> bookPlaceUpdate(BookPlace bookPlace) {
        try {
            String openid = null;
            try {
                Session session = SecurityUtils.getSubject().getSession();
                openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            } catch (Exception e) {

            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            bookMapper.updateBookPlace(bookPlace);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/bookPlaceDel/{ids}")
    @ApiOperation(value = "删除预订信息", notes = "111")
    public CommonResult<Object> bookPlaceDel(@PathVariable("ids") String ids) {
        try {
            String openid = null;
            try {
                Session session = SecurityUtils.getSubject().getSession();
                openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            } catch (Exception e) {

            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.CANCEL_SUCCESS));
            bookMapper.delBookPlace(ids.split(","));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/addLicense")
    @ApiOperation(value = "添加车牌", notes = "111")
    public CommonResult<Object> addLicens(String license) {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            String uid = "12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            if (openid == null) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if (user == null) {
                user = new WeChatUser();
                user.setUserid(openid);
                bookMapper.addWechat(user);
            }
            List<WeChatLicense> data = bookMapper.findWeChatUserLicense(user.getId(),license);
            if(!NullUtils.isEmpty(data)){
                return new CommonResult<>(501, LocalUtil.get("重复添加车牌"));
            }
            WeChatLicense license1 = new WeChatLicense();
            license1.setLicense(license);
            license1.setUserid(user.getId());
            bookMapper.addWechatLicense(license1);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/getLicense")
    @ApiOperation(value = "获取车牌", notes = "111")
    public CommonResult<Object> geLicense() {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            String uid = "12";

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            if (openid == null) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            WeChatUser user = bookMapper.findWeChatUserByUserid(null,openid);
            if (user != null) {
                List data = bookMapper.findWeChatUserLicense(user.getId(),null);
                res.setData(data);
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/licenseDel/{ids}")
    @ApiOperation(value = "删除车牌信息", notes = "111")
    public CommonResult<Object> licenseDel(@PathVariable("ids") String ids) {
        try {
            String openid = null;
            try {
                Session session = SecurityUtils.getSubject().getSession();
                openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            } catch (Exception e) {

            }
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.CANCEL_SUCCESS));
            bookMapper.delLicense(ids.split(","));
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/addLicensePos")
    @ApiOperation(value = "添加车辆停放位置", notes = "111")
    public CommonResult<Object> addLicensePos(LicensePos licensePos,Integer userId) {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            String uid = "12";
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);
            if (openid == null&& NullUtils.isEmpty(user)) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            if (licensePos.getLicense() != null) {
                licensePos.setLicense(licensePos.getLicense().toUpperCase());
            }
            if (user != null) {
                licensePos.setUserid(user.getId());
                licensePos.setUpdatetime(new Date());
                licensePos.setState(0);
            }

            LicensePos data = parkingService.findLicensePosByLicenseAndMap(licensePos.getMap(), licensePos.getLicense(), user == null ? null : user.getId());
            if (data != null) {
                licensePos.setId(data.getId());
                parkingService.updateLicensePos(licensePos);
            } else {
                parkingService.addLicensePos(licensePos);
            }

            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getLicensePos")
    @ApiOperation(value = "获取车辆停放位置", notes = "寻车")
    public CommonResult<Object> addLicensePos(String license, Integer map,Integer userId) {
        try {
            Session session = SecurityUtils.getSubject().getSession();
            String openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
            WeChatUser user = bookMapper.findWeChatUserByUserid(userId,openid);

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            String uid = "12";
            if (openid == null&& NullUtils.isEmpty(user)) {
                res.setCode(400);
                res.setMessage("未获取到用户id");
                return res;
            }
            if (license != null) {
                license = license.toUpperCase();
            }

            LicensePos data = parkingService.findLicensePosByLicenseAndMap(map, license, user == null ? null : user.getId());
            if(NullUtils.isEmpty(data)) {
                res.setCode(500);
                res.setMessage("未标记车位，请先标记车位");
                return res;

            }
            ParkingPlace parkingPlace = parkingService.getPlaceByPlaceNames(map,data.getName(),null,null);
            ParkingInfoStatistics infoStatistics = new ParkingInfoStatistics();
            infoStatistics.setUserId(Long.valueOf(user.getId()));
            infoStatistics.setMap(Long.valueOf(map));
            if(NullUtils.isEmpty(parkingPlace)){
                infoStatistics.setPlace(null);
            }else {
                infoStatistics.setPlace(Long.valueOf(parkingPlace.getId()));
            }
            infoStatistics.setPlacename(data.getName());
            infoStatistics.setStartTime(LocalDateTime.now());
            infoStatistics.setEndTime(LocalDateTime.now());
            parkingInfoStatisticsService.addParkingInfoStatisticsfindCar(infoStatistics);
            JSONObject jsonArea = new JSONObject();

//            List<ViewVo> findCarFrequency = viewMapper.getFindCarFrequency();
            jsonArea.put("uid", "-1");
            jsonArea.put("type", 28);//27进出实时更新
            jsonArea.put("data",infoStatistics);
            appletsWebSocket.sendAll(jsonArea.toString());
            res.setData(data);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPlaceDetail")
    @ApiOperation(value = "获取某个车位情况", notes = "111")
    public CommonResult<Object> getPlacecDetail(String fid, String name, Integer map) {
        try {

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            List<ParkingPlace> palces = parkMapper.findPlaceByMapAndName(map, name, fid);
            if (palces != null && palces.size() > 0) {
                res.setData(palces.get(0));
            }
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getParkList")
    @ApiOperation(value = "获取停车场列表", notes = "111")
    public Object addLicensePos(Double lng, Double lat, String key, Double minDis, Integer place_type, Integer mapId) {
        try {
            return parkingService.addLicensePos (lng, lat, key, minDis, place_type, mapId).get();
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getTargetByName")
    @ApiOperation(value = "单个地图根据关键字获取后台公司和商家信息", notes = "单地图搜索")
    public CommonResult<Object> bookPlaceDel(Integer map, String name) {
        try {
            List<Object> list = new ArrayList<>();
            if (name != null && !name.trim().isEmpty()) {
                List<ParkingCompany> companys = parkMapper.findByAllCompany(null, name, map, null, null, null);
                List<ShangJia> data = parkingService.findByAllShangjia(null, map, null, name, null, null, null);
                // if(!NullUtils.isEmpty(data)){
                //     EachMapSearchBusiness business = new EachMapSearchBusiness();
                //     business.setMap(String.valueOf(data.get(0).getMap()));
                //     business.setMapName(String.valueOf(data.get(0).getMapName()));
                //     business.setBusinessId(String.valueOf(data.get(0).getId()));
                //     business.setBusinessName(data.get(0).getName());
                //     businessService.insert(business);
                // }
                for (ParkingCompany parkingCompany : companys) {
                    parkingCompany.setTypes("8");
                    list.add(parkingCompany);
                }
                for (ShangJia shangJia : data) {
                    shangJia.setTypes("3");
                    list.add(shangJia);
                }
            }

            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            res.setData(list);
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

}
