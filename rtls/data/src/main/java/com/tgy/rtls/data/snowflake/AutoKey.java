package com.tgy.rtls.data.snowflake;


import com.tgy.rtls.data.entity.check.AutoidEntity;
import com.tgy.rtls.data.mapper.check.AutoidDao;
import com.tgy.rtls.data.service.check.AutoService;
import com.tgy.rtls.data.service.common.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class AutoKey {
    /**
     * 获取自增id,有效时间为当天,
     * @param key
     * @return
     */
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    AutoService autoService;
 /*   @Autowired(required = false)
    AutoidDao autoDao;*/
    private  volatile long storeid;

    public  long getAutoId(String key){
        long id;
        if (redisTemplate.getConnectionFactory() !=null){
            RedisAtomicLong aLong = new RedisAtomicLong("random",redisTemplate.getConnectionFactory());

             AutoidEntity autoid = autoService.getAutoId("redis");
            if(aLong.get()<autoid.getRedisvalue()){
                aLong.set(autoid.getRedisvalue());
            }


            long andIncrement = aLong.getAndIncrement();
            aLong.expireAt(getEndTime());
            id = (andIncrement);
            storeid=andIncrement;
            id=andIncrement;

        }else {

            if(storeid!=0){
               AutoidEntity autoid = autoService.getAutoId("redis");
              if(storeid>autoid.getRedisvalue()) {
                    autoid.setRedisvalue(storeid);
                  autoService.updateById(autoid);
                }
            }
            throw new RuntimeException("系统异常");
        }
        return id;
    }

    /**
     * 获取截止时间
     * @return
     */
    public static Date getEndTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");//注意月份是MM
        try {
            Date date = simpleDateFormat.parse("2040-04-02");
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }


    }
}

