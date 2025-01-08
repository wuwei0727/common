package com.tgy.rtls.location.config;

import com.tgy.rtls.data.config.SpringContextHolder;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.location.mqtt.HaoXiangMqtt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.Date;


public class RedisKeyExpiration extends KeyExpirationEventMessageListener {
   /* @Autowired
    HaoXiangMqtt haoXiangMqtt;*/
    @Autowired
    private RedisService redisService;


    public RedisKeyExpiration(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }
    /*public RedisKeyExpiration(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }*/
    /**
     * 针对redis数据失效事件，进行数据处理
     *
     * @param message
     * @param pattern
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        // 用户做自己的业务处理即可,注意message.toString()可以获取失效的key
        try {
            String expiredKey = message.toString();
            String[] split = expiredKey.split(",");
            if(split[0].equals("tag")){//标签离线

            }else if (split[0].equals("base")){//微基站离线
                redisService.remove(expiredKey);
                BsState bsState=new BsState();
                bsState.setState((short) 1);
                bsState.setErrorCode((short)2);
                bsState.setBsid(split[1]);
                bsState.setTime(new Date().getTime());
                bsState.setType(2);
                HaoXiangMqtt haoXiangMqtt = SpringContextHolder.getBean(HaoXiangMqtt.class);
                haoXiangMqtt.publishBsStateData(split[1],bsState);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
