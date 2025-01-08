package com.tgy.rtls.data.service.park.impl;

import cn.hutool.core.util.StrUtil;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-08-28 10:03
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class MapHotspotDataRedisServiceImpl implements MapHotspotDataRedisService {

    @Resource
    private RedisTemplate<String,Object> mapHotspotDataRedisTemplate;

    @Override
    public Double storeHotSearch(UserHotData userHotData,double delta) {
        return mapHotspotDataRedisTemplate.opsForZSet ().incrementScore ("hotSearch:" + userHotData.getMap(), userHotData,delta);
    }

    @Override
    public List<UserHotData> getHotSearchByMap(int map) {
        List<UserHotData> list = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<Object>> elementsWithScores =
                mapHotspotDataRedisTemplate.opsForZSet().reverseRangeWithScores( "hotSearch:"+map, 0, -1);
            if(!Objects.requireNonNull(elementsWithScores).isEmpty()){
                for (ZSetOperations.TypedTuple<Object> typedTuple : elementsWithScores) {
                    if (typedTuple.getValue() instanceof UserHotData) {
                        UserHotData user = (UserHotData) typedTuple.getValue();
                        Objects.requireNonNull(user).setScore(typedTuple.getScore());
                        list.add(user);
                    }
                }
            }
        return Objects.requireNonNull (list);
    }

    @Override
    public Map<String, Set<ZSetOperations.TypedTuple<Object>>> getKeysAndMembersByPattern(String map) {
        Map<String, Set<ZSetOperations.TypedTuple<Object>>> result = new HashMap<>();
        Set<String> keys = mapHotspotDataRedisTemplate.keys(StrUtil.isEmptyIfStr(map)?"hotSearch:*":"hotSearch:"+map);
        if (keys != null) {
            for (String key : keys) {
                Set<ZSetOperations.TypedTuple<Object>> members = mapHotspotDataRedisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
                result.put(key, members);
            }
        }
        return result;


    }
}
