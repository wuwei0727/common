package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.park.UserHotData;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-08-28 10:03
 * @Description: TODO
 * @Version: 1.0
 */
public interface MapHotspotDataRedisService {

    Double storeHotSearch(UserHotData userHotData,double delta);

    List<UserHotData> getHotSearchByMap(int map);

    Map<String, Set<ZSetOperations.TypedTuple<Object>>> getKeysAndMembersByPattern(String map);
}
