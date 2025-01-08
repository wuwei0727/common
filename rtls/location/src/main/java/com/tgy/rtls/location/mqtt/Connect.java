package com.tgy.rtls.location.mqtt;

import com.tgy.rtls.data.entity.equip.Gateway_uwb;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @author 许强
 * @Package com.example.config.mqtt
 * @date 2020/2/26
 * 项目启动时：连接网关 清理缓存
 */
@Component
public class Connect implements CommandLineRunner {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private GatewayService gatewayService;
    public static Hashtable<Integer, Client> connect=new Hashtable<>();


    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void run(String... strings) {
        scheduledExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                {
                    try {
                        //加载网关
                        List<Gateway_uwb> gateway_uwbs = gatewayService.findByAll(null,null,null,null,null);
                        for (Gateway_uwb gateway_uwb : gateway_uwbs) {
                            if (gateway_uwb.getMap()!=null) {
                               Client clinet = Connect.connect.get(gateway_uwb.getId());
                                if(clinet!=null){
                                    clinet.startReconnect();
                                }else{
                                    Client client = new Client();
                                    client.start(gateway_uwb.getId());
                                    connect.put(gateway_uwb.getId(), client);
                                }
                         /*       Client client = new Client();
                                if (client.start(gateway_uwb.getId())) {
                                    connect.put(gateway_uwb.getId(), client);
                                    gatewayService.updateGatewayConnect(gateway_uwb.getId(), 1);
                                } else {
                                    logger.error(gateway_uwb.getName() + "连接失败");
                                    gatewayService.updateGatewayConnect(gateway_uwb.getId(), 0);
                                }*/
                            }
                        }

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
                        Set<String> keys5 = redisTemplate.keys("gatewayId*");//网关缓存
                        for (String key : keys5) {
                            redisService.remove(key);
                        }
                        Set<String> keys6 = redisTemplate.keys("bsconfignum*");//分站参数缓存
                        for (String key : keys6) {
                            redisService.remove(key);
                        }
                        Set<String> keys7 = redisTemplate.keys("tag*");//标签定位信息缓存
                        for (String key : keys7) {
                            redisService.remove(key);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        });
    }
}
