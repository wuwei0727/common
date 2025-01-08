package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingCompany;
import com.tgy.rtls.data.entity.park.ShangJia;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.entity.park.WeChatUser;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.tool.Constant;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@EnableAsync
@RequestMapping(value = "/hot")
public class WeChatStoreHotDataController {
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private BookMapper bookMapper;
    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String appId = "wxf0f25ad3fc36365e";


    @RequestMapping(value = "/getHotData")
    @ApiOperation(value = "获取用户常用搜索地点信息", notes = "111")
    public CommonResult<Object> getStorePlace(Integer map,Integer userId) {
        try {

            Integer uid = 1;
            CommonResult<Object> res = new CommonResult<>(200, "");
            System.out.println("parent thread:" + Thread.currentThread().getId());
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
            uid = user.getId();
            Object[] array = {};
            Future<Object> future = redisService.getRedisData(uid);
            List<UserHotData> result = (List) future.get(2000, TimeUnit.MILLISECONDS);

            if (map != null) {
                List<UserHotData> map_hot = new ArrayList<>();
                for (UserHotData hotData : result) {
                    if (hotData.getMap() == null || hotData.getMap().equals(map)) {
                        if (hotData.getType().equals("3")) {
                            if (hotData.getType().equals("3") && hotData.getId() != null) {
                                List<Object> list = new ArrayList<>();
                                String name = hotData.getName();
                                List<ShangJia> shangjiaByName = parkingService.findShangjiaMapName(Integer.valueOf(hotData.getDatabaseId()), name, map);
                                List<ParkingCompany> comById = parkingService.getComByNameId(name, Integer.valueOf(hotData.getDatabaseId()), map);
                                if (name != null && !name.trim().isEmpty()) {
                                    List<ParkingCompany> companys = parkMapper.findByAllCompany(Integer.valueOf(hotData.getDatabaseId()), name, map, null, null, null);
                                    List<ShangJia> data = parkingService.findByAllShangjia(Integer.valueOf(hotData.getDatabaseId()), map, null, name, null, null, null);
                                    for (ParkingCompany parkingCompany : companys) {
                                        list.add(parkingCompany);
                                    }
                                    for (ShangJia shangJia : data) {
                                        list.add(shangJia);
                                    }
                                }

                                if (list.size() == 0) {
                                    if (hotData.getId() != null) {
                                        Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:" + uid, Double.valueOf(hotData.getId()), Double.valueOf(hotData.getId()));
                                        if (values != null && values.size() > 0) {
                                            redisTemplate.opsForZSet().remove("userhot:" + uid, values.toArray()[0]);
                                        }

                                    }
                                } else {
                                    Integer id = null;
                                    String fid = "";
                                    String x = "";
                                    String y = "";
                                    String floor = "";
                                    for (Object object : list) {
                                        if (object instanceof ParkingCompany) {
                                            ParkingCompany parkCompany = (ParkingCompany) object;
                                            id = parkCompany.getId();
                                            fid = parkCompany.getFid();
                                            x = parkCompany.getX();
                                            y = parkCompany.getY();
                                            floor = parkCompany.getFloor();

                                        } else if (object instanceof ShangJia) {
                                            ShangJia parkCompany = (ShangJia) object;
                                            id = parkCompany.getId();
                                            fid = parkCompany.getFid();
                                            x = parkCompany.getX();
                                            y = parkCompany.getY();
                                            floor = parkCompany.getFloor();
                                        }
                                    }
//                                    hotData.setId(id.longValue());
                                    hotData.setFid(fid);
                                    hotData.setX(x);
                                    hotData.setY(y);
                                    hotData.setFloor(Integer.valueOf(floor));

                                    if (hotData.getId() != null) {
                                        Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:" + uid, Double.valueOf(hotData.getId()), Double.valueOf(hotData.getId()));
                                        //  Set<Object> values = redisTemplate.opsForZSet().range("userhot:" + uid, Long.valueOf(id), Long.valueOf(id));
                                        if (values != null && values.size() > 0)
                                            redisTemplate.opsForZSet().remove("userhot:" + uid, values.toArray()[0]);
                                    }
                                    Boolean add = redisTemplate.opsForZSet().add("userhot:" + uid, hotData, hotData.getId());
                                    map_hot.add(hotData);
                                    for (Object object : list) {
                                        if (object instanceof ParkingCompany) {
                                            if (NullUtils.isEmpty(comById)) {
                                                Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:" + uid, Double.valueOf(hotData.getId()), Double.valueOf(hotData.getId()));
                                                if (values != null && values.size() > 0) {
                                                    redisTemplate.opsForZSet().remove("userhot:" + uid, values.toArray()[0]);
                                                }
                                            }
                                        } else if (object instanceof ShangJia) {
                                            if (NullUtils.isEmpty(shangjiaByName)) {
                                                Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:" + uid, Double.valueOf(hotData.getId()), Double.valueOf(hotData.getId()));
                                                if (values != null && values.size() > 0) {
                                                    redisTemplate.opsForZSet().remove("userhot:" + uid, values.toArray()[0]);
                                                }
                                            }
                                        }
                                    }


                                }
                            }
                        } else {
                            map_hot.add(hotData);
                        }
                    }


                }
                array = map_hot.toArray();
            } else array = result.toArray();
            if (array != null) Arrays.sort(array);
            res.setData(array);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }


    @RequestMapping(value = "/getHomeHotData")
    @ApiOperation(value = "获取用户常用搜索地点信息", notes = "111")
    public CommonResult<Object> getHomeHotData() {
        try {

            Integer uid = 1;
            CommonResult<Object> res = new CommonResult<>(200, "");
            System.out.println("parent thread:" + Thread.currentThread().getId());
            String openid = null;
            Session session = SecurityUtils.getSubject().getSession();
            openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
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
            uid = user.getId();
            Object[] array = {};
            Future<Object> future = redisService.getRedisHomeData("userHomeHot:" + uid);
            List<UserHotData> result = (List) future.get(2000, TimeUnit.MILLISECONDS);
            List<UserHotData> map_hot = new ArrayList<>();
            for (UserHotData hotData : result) {
                map_hot.add(hotData);
            }
            array = map_hot.toArray();
            if (array != null) {
                Arrays.sort(array);
            }
            res.setData(array);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }


    @RequestMapping(value = "/addHotData")
    @ApiOperation(value = "添加用户常用位置信息", notes = "111")
    public CommonResult<Object> storePlace(UserHotData userHotData,Integer userId) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, "");
            Integer uid = 1;
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
            uid = user.getId();

            redisTemplate.opsForZSet().add("userhot:" + uid, userHotData, 2278637208000L - System.currentTimeMillis());
            res.setCode(200);
            res.setMessage("添加成功");
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/addHomeHotData")
    @ApiOperation(value = "添加用户常用位置信息", notes = "111")
    public CommonResult<Object> addHomeHotData(UserHotData userHotData) {
        try {
            CommonResult<Object> res = new CommonResult<>(200, "");
            Integer uid = null;
            String openid = null;
            Session session = SecurityUtils.getSubject().getSession();
            openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
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
            uid = user.getId();

            redisTemplate.opsForZSet().add("userHomeHot:" + uid, userHotData, 2278637208000L - System.currentTimeMillis());
            res.setCode(200);
            res.setMessage("添加成功");
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }

    @RequestMapping(value = "/delHotData/{ids}")
    @ApiOperation(value = "删除用户常用位置信息", notes = "111")
    public CommonResult<Object> delStorePlace(@PathVariable("ids") String ids,Integer userId) {
        try {
            Integer uid;
            String openid;
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
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
            uid = user.getId();

            String[] dd = ids.split(",");
            for (String id : dd) {
                Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:" + uid, Double.parseDouble(id), Double.parseDouble(id));
                if (values != null && values.size() > 0) {
                     double score = Double.parseDouble(id);
                     redisTemplate.opsForZSet().removeRangeByScore("userhot:" + uid, score,score);
                }
            }

            //  bookMapper.delStorePlace();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping("qqq")
    public Long aaa(String id){
        Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userhot:2180", Double.parseDouble(id), Double.parseDouble(id));
        Long remove=0L;
        if (values != null && values.size() > 0) {
             remove = redisTemplate.opsForZSet().remove("userhot:2180", values.toArray()[0]);
        }

        // double score = Double.parseDouble(id);
        // Long remove = redisTemplate.opsForZSet().removeRangeByScore("userhot:2180", score,score);
        return remove;
    }

    @RequestMapping(value = "/delHomeHotData/{ids}")
    @ApiOperation(value = "删除用户常用位置信息", notes = "111")
    public CommonResult<Object> delHomeHotData(@PathVariable("ids") String ids) {
        try {
            Integer uid = null;
            String openid = null;
            CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            Session session = SecurityUtils.getSubject().getSession();
            openid = (String) session.getAttribute(Constant.USER_WXSESSION_ID);
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
            uid = user.getId();
            if ("-1".equals(ids)) {
                redisTemplate.opsForZSet().removeRange("userHomeHot:" + uid, 0, -1);
            } else {
                String[] dd = ids.split(",");
                for (String id : dd) {
                    Set<Object> values = redisTemplate.opsForZSet().rangeByScore("userHomeHot:" + uid, Double.parseDouble(id), Double.parseDouble(id));
                    if (values != null && values.size() > 0) {
                        redisTemplate.opsForZSet().remove("userHomeHot:" + uid, values.toArray()[0]);
                    }
                }
            }

            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
