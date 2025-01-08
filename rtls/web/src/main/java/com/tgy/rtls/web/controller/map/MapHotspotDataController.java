package com.tgy.rtls.web.controller.map;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.Todo;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    private static final String TODO_PREFIX = "todo:";
    private static final String TODO_PREFIX2 = "todo2:";



    @PostMapping("/saveTodo2")
    public CommonResult<Object> saveTodo2(String userId, @RequestBody Todo todo) {
        String key = TODO_PREFIX2 + userId;
        Long aLong = redisTemplate.opsForList().rightPush(key, todo);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS),aLong);
    }

    @GetMapping("/getAndDeleteTodos2")
    public CommonResult<Object> getAndDeleteTodos2(String userId) {
        String key = TODO_PREFIX2 + userId;
        List<Object> todos = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);

        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),todos);
    }

    @PostMapping("/saveTodo")
    public CommonResult<Object> saveTodo(String userId, @RequestBody Todo todo) {
        String key = TODO_PREFIX + userId;
        Long aLong = redisTemplate.opsForList().rightPush(key, todo);
        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS),aLong);
    }

    @GetMapping("/getAndDeleteTodos")
    public CommonResult<Object> getAndDeleteTodos(String userId) {
        String key = TODO_PREFIX + userId;
        List<Object> todos = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);

        return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),todos);
    }

    @PostMapping("/saveData")
    public CommonResult<Object> saveData(@RequestParam String userId, @RequestBody String jsonData) {
        redisTemplate.opsForValue().set("socket:"+userId, jsonData, 5, TimeUnit.SECONDS);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));

    }

    @GetMapping("/getData")
    public CommonResult<Object> getData(String userId) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),redisTemplate.opsForValue().get("socket:"+userId));
    }



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


    @GetMapping("/removeElementsWithScoreOne")
    public void removeElementsWithScoreOne(String key,Integer score) {
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // 使用RedisTemplate操作Redis的有序集合
        Set<Object> elementsWithScoreOne = zSetOps.rangeByScore(key, score, score);

        if (elementsWithScoreOne != null && !elementsWithScoreOne.isEmpty()) {
            elementsWithScoreOne.forEach(element -> zSetOps.remove(key, element));
        }
    }

    @GetMapping("/get")
    public CommonResult<Object> get(Integer map) {
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
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
    }

//这是一个main方法，程序的入口
public static void main(String[] args){
    for (int i = 0; i <2; i++) {
        System.out.println("\t"+ i);
    }
}

}
