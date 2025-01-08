package com.tgy.rtls.data.service.common.impl;

import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.service.common.RedisService;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service("redisService")
public class RedisServiceImpl implements RedisService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean set(final String key, final String value) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.keys("*".getBytes());
                    connection.set(serializer.serialize(key), serializer.serialize(value));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
        return result;
    }
    @Override
    public boolean setex(final String key,final int time,final String value) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    connection.setEx(serializer.serialize(key),time, serializer.serialize(value));
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });
        return result;
    }

    /**
     * 普通缓存放入
     *
     * @param key
     *            键
     * @param value
     *            值
     * @return true成功 false失败
     */
    @Override
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public String get(final String key) {
        String result = redisTemplate.execute(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                byte[] value = connection.get(serializer.serialize(key));
                return serializer.deserialize(value);
            }
        });
        return result;
    }



    @Override
    public boolean expire(final String key, long expire) {
        return redisTemplate.expire(key, expire, TimeUnit.SECONDS);
    }

    @Override
    public boolean remove(final String key) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                connection.del(key.getBytes());
                return true;
            }
        });
        return result;
    }

    /**
     * 缓存TDOA数据，支持原始数据查询
     * @param key
     * @param cache
     */
    @Override
    public void setTagData(String key, Object cache) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    //RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    //connection.set(serializer.serialize(key), serializer.serialize(cache.toString()));
                    //转成输出字节流
                    ByteArrayOutputStream bai = new ByteArrayOutputStream();
                    ObjectOutputStream obi = new ObjectOutputStream(bai);
                    obi.writeObject(cache);
                    byte[] byt = bai.toByteArray();
                   connection.set(key.getBytes(),byt, Expiration.from(5,TimeUnit.SECONDS), RedisStringCommands.SetOption.UPSERT);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                 return true;
            };
        });

    }

    @Override
    public Map getTagData(String key)

    {
        Map result = redisTemplate.execute(new RedisCallback<Map>() {
            @Override
            public Map doInRedis(RedisConnection redisConnection) throws DataAccessException {
                {
                    //获取map
                    Map<String, Object> result1=null;
                    byte[] tagByte = redisConnection.get(key.getBytes());

                    try {
                        ObjectInputStream oii = null;
                        ByteArrayInputStream bis = null;

                        //转换成输入字节流
                        bis = new ByteArrayInputStream(tagByte);
                        oii = new ObjectInputStream(bis);
                        result1 = (Map<String, Object>) oii.readObject();
                        oii = new ObjectInputStream(bis);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    return result1;

                }
            }
        });
        return result;



    }

    @Override
    public void setPersonList(String key, Object cache) {
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    //RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
                    //connection.set(serializer.serialize(key), serializer.serialize(cache.toString()));
                    //转成输出字节流
                    ByteArrayOutputStream bai = new ByteArrayOutputStream();
                    ObjectOutputStream obi = new ObjectOutputStream(bai);
                    obi.writeObject(cache);
                    byte[] byt = bai.toByteArray();
                   // connection.set(key.getBytes(),byt, Expiration.from(5,TimeUnit.SECONDS), RedisStringCommands.SetOption.UPSERT);
                    connection.set(key.getBytes(),byt);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            };
        });
    }

    @Override
    public List getPersonList(String key) {

        List result = redisTemplate.execute(new RedisCallback<List>() {
            @Override
            public List doInRedis(RedisConnection redisConnection) throws DataAccessException {
                {
                    //获取map
                   List result1=null;
                    byte[] tagByte = redisConnection.get(key.getBytes());
                    if(tagByte==null)
                        return null;

                    try {
                        ObjectInputStream oii = null;
                        ByteArrayInputStream bis = null;

                        //转换成输入字节流
                        bis = new ByteArrayInputStream(tagByte);
                        oii = new ObjectInputStream(bis);
                        result1 = (List) oii.readObject();
                       // oii = new ObjectInputStream(bis);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return result1;

                }
            }
        });
        return result;
    }



    /**
     * 删除redis数据
     */
    @Override
    public void delateAllData(String in) {
    // Set<String> keys = redisTemplate.delete()
      boolean has=  redisTemplate.hasKey(in);
        redisTemplate.keys("*");
        redisTemplate.delete(in);
        boolean result = redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {

                    Set<byte[]> set = connection.keys("*".getBytes());
                    for (byte[] bb : set                         ) {
                        connection.del(bb);
                    }


                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            }
        });


    }
    @Override
    public boolean addZset(Integer uid, Object userHotData){
        return redisTemplate.opsForZSet().add("userhot:" +uid , userHotData,1d/ System.currentTimeMillis());
    }

    @Override
    public List<Object> getListKey_value(String prefix) {
        /**
         * 获取所有的key
         */
           Set<String> keys = redisTemplate.keys("*");
            List<Object> values = redisTemplate.opsForValue().multiGet(keys);
            return values;
    }

    @Override
    public    @Async
    Future<Object> getRedisData(Integer uid) throws InterruptedException {
        Cursor<ZSetOperations.TypedTuple<Object>> data = redisTemplate.opsForZSet().scan("userhot:" + uid, ScanOptions.NONE);
        System.out.println("children thread:"+Thread.currentThread().getId());
        List list=new ArrayList();
        while (data.hasNext()){
            ZSetOperations.TypedTuple<Object> object = data.next();
            UserHotData hot = (UserHotData) object.getValue();
            Double score = object.getScore();
            //Long index = redisTemplate.opsForZSet().rank("userhot:" +  uid, hot);
            hot.setId(score.longValue());
            list.add(hot);
        }
        //Thread.sleep(5000);
        //AsyncResult<Object> ss = new AsyncResult<Object>(list);
        return new AsyncResult<>(list);
    }

    @Override
    public @Async Future<Object> getRedisHomeData(String uid) throws InterruptedException {
        Cursor<ZSetOperations.TypedTuple<Object>> data = redisTemplate.opsForZSet().scan(uid, ScanOptions.NONE);
        System.out.println("children thread:"+Thread.currentThread().getId());
        List list=new ArrayList();
        while (data.hasNext()){
            ZSetOperations.TypedTuple<Object> object = data.next();
            UserHotData hot = (UserHotData) object.getValue();
            Double score = object.getScore();
            hot.setId(score.longValue());
            list.add(hot);
        }
       // AsyncResult<Object> ss = new AsyncResult<Object>(list);
        return new AsyncResult<>(list);
    }





}