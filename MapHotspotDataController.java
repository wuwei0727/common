<<<<<<< HEAD
package com.tgy.rtls.web.controller.map;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import com.tgy.rtls.data.service.park.impl.MapHotspotDataRedisServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-08-28 10:01
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping ("/mapHotspotData")
public class MapHotspotDataController {
    @Resource
    private MapHotspotDataRedisService mapHotspotDataRedisService;
@Resource
    private MapHotspotDataRedisServiceImpl m;


    @PostMapping ("/addHotSearch")
    public CommonResult<Object> addHotSearch(@RequestBody UserHotData userHotData) {
        userHotData.setId(Long.valueOf(userHotData.getDatabaseId()));
        Double hotspotValue = mapHotspotDataRedisService.storeHotSearch (userHotData,1);
        if(!NullUtils.isEmpty (hotspotValue)){
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), hotspotValue);
        }
        return new CommonResult<>(500, "redis"+LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }

    @GetMapping("/getHotSearchByMap")
    public CommonResult<Object> getHotSearchByMap(@RequestParam int map) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), mapHotspotDataRedisService.getHotSearchByMap(map));
    }
    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @GetMapping("/get")
    public CommonResult<Object> get(String map) {
        List<Object> list = new ArrayList<>();
// 获取全部元素及其 score
        Set<ZSetOperations.TypedTuple<Object>> elementsWithScores =
                redisTemplate.opsForZSet().rangeWithScores("hotSearch:"+map, 0, -1);
        log.info("调用get方法---->elementsWithScores:"+elementsWithScores);
        Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores("hotSearch:"+map, 0, -1);
        list.add(reverseRangeWithScores);
        log.info("调用get方法---->reverseRangeWithScores:"+reverseRangeWithScores);
        // for (int i = 0; i < 1800; i++) {
        //     UserHotData userHotData1 = new UserHotData();
        //     userHotData1.setId(null);
        //     userHotData1.setName("212"+i);
        //     userHotData1.setType("231");
        //     userHotData1.setX("31");
        //     userHotData1.setY("321");
        //     userHotData1.setZ("312");
        //     userHotData1.setMap(75);
        //     userHotData1.setIcon("231");
        //     userHotData1.setEname("321");
        //     userHotData1.setFloor(2);
        //     userHotData1.setFid("3213");
        //     userHotData1.setDesc("321");
        //     userHotData1.setOutdoorType("321");
        //     userHotData1.setDatabaseId("312");
        //     mapHotspotDataRedisService.storeHotSearch (userHotData1);
        // }
        Map<String, Set<ZSetOperations.TypedTuple<Object>>> hotSearch = mapHotspotDataRedisService.getKeysAndMembersByPattern(map);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),hotSearch);
    }



}
=======
package com.tgy.rtls.web.controller.map;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import com.tgy.rtls.data.service.park.impl.MapHotspotDataRedisServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2023-08-28 10:01
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping ("/mapHotspotData")
public class MapHotspotDataController {
    @Resource
    private MapHotspotDataRedisService mapHotspotDataRedisService;
@Resource
    private MapHotspotDataRedisServiceImpl m;


    @PostMapping ("/addHotSearch")
    public CommonResult<Object> addHotSearch(@RequestBody UserHotData userHotData) {
        userHotData.setId(Long.valueOf(userHotData.getDatabaseId()));
        Double hotspotValue = mapHotspotDataRedisService.storeHotSearch (userHotData,1);
        if(!NullUtils.isEmpty (hotspotValue)){
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), hotspotValue);
        }
        return new CommonResult<>(500, "redis"+LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
    }

    @GetMapping("/getHotSearchByMap")
    public CommonResult<Object> getHotSearchByMap(@RequestParam int map) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), mapHotspotDataRedisService.getHotSearchByMap(map));
    }
    @Resource
    private RedisTemplate<String,Object> redisTemplate;


    @GetMapping("/get")
    public CommonResult<Object> get(String map) {
        List<Object> list = new ArrayList<>();
// 获取全部元素及其 score
        Set<ZSetOperations.TypedTuple<Object>> elementsWithScores =
                redisTemplate.opsForZSet().rangeWithScores("hotSearch:"+map, 0, -1);
        log.info("调用get方法---->elementsWithScores:"+elementsWithScores);
        Set<ZSetOperations.TypedTuple<Object>> reverseRangeWithScores =
                redisTemplate.opsForZSet().reverseRangeWithScores("hotSearch:"+map, 0, -1);
        list.add(reverseRangeWithScores);
        log.info("调用get方法---->reverseRangeWithScores:"+reverseRangeWithScores);
        // for (int i = 0; i < 1800; i++) {
        //     UserHotData userHotData1 = new UserHotData();
        //     userHotData1.setId(null);
        //     userHotData1.setName("212"+i);
        //     userHotData1.setType("231");
        //     userHotData1.setX("31");
        //     userHotData1.setY("321");
        //     userHotData1.setZ("312");
        //     userHotData1.setMap(75);
        //     userHotData1.setIcon("231");
        //     userHotData1.setEname("321");
        //     userHotData1.setFloor(2);
        //     userHotData1.setFid("3213");
        //     userHotData1.setDesc("321");
        //     userHotData1.setOutdoorType("321");
        //     userHotData1.setDatabaseId("312");
        //     mapHotspotDataRedisService.storeHotSearch (userHotData1);
        // }
        Map<String, Set<ZSetOperations.TypedTuple<Object>>> hotSearch = mapHotspotDataRedisService.getKeysAndMembersByPattern(map);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),hotSearch);
    }



}
>>>>>>> a52e230533672924aa45f4b18aba0c7a14438dd8
