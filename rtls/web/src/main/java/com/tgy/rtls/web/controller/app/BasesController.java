package com.tgy.rtls.web.controller.app;

import com.tgy.rtls.web.util.RedisCachesTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-07-27 09:43
 * @Description: TODO
 * @Version: 1.0
 */
@Component
@Slf4j
public class BasesController {
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
