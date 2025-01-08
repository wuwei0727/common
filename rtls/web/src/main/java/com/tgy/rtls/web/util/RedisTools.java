package com.tgy.rtls.web.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2022-07-30 09:35
 * @Description: TODO
 * @Version: 1.0
 */
@Component
public class RedisTools {
    @Autowired
    private RedisCachesTools redisCacheManager;
    /**
     * 根据sessionID删除redis存储的用户信息
     * @param sessionID
     */
    public void delUserInfoFromRedis(String sessionID){
        redisCacheManager.del(sessionID);
    }
}
