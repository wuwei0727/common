package com.tgy.rtls.web.config;

import com.tgy.rtls.data.service.common.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author 许强
 * @Package com.example.config.mqtt
 * @date 2020/2/26
 * 项目启动清除redis数据
 */
@Component
public class Connect implements CommandLineRunner {
    @Autowired
    private RedisService redisService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    Executor scheduledExecutorService;

    @Override
    public void run(String... strings) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        Set<String> keys = redisTemplate.keys("personnum*");//人员缓存
                        for (String key : keys) {
                            redisService.remove(key);
                        }
                        Set<String> keys2 = redisTemplate.keys("subnum*");//分站缓存
                        for (String key : keys2) {
                            redisService.remove(key);
                        }
                        Set<String> keys3 = redisTemplate.keys("tagnum*");//标签缓存
                        for (String key : keys3) {
                            redisService.remove(key);
                        }
                        Set<String> keys4 = redisTemplate.keys("instanceid*");//项目缓存
                        for (String key : keys4) {
                            redisService.remove(key);
                        }
                   } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
