package com.tgy.rtls.data.service.common;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public interface RedisService {

    /**
     * set存数据
     * @param key
     * @param value
     * @return
     */
    boolean set(String key, String value);

    boolean setex(String key, int time, String value);

    boolean set(String key, Object value);

    /**
     * get获取数据
     * @param key
     * @return
     */
    String get(String key);

    /**
     * 设置有效天数
     * @param key
     * @param expire
     * @return
     */
    boolean expire(String key, long expire);

    /**
     * 移除数据
     * @param key
     * @return
     */
    boolean remove(String key);

    /**
     * tdoa data
     * @param key
     * @param cache
     */
    void setTagData(String key,Object cache);

    Map getTagData(String key);

    /**
     * subrecord data
     * @param key
     * @param cache
     */
    void setPersonList(String key,Object cache);

    List getPersonList(String key);


    void delateAllData(String in);
    boolean addZset(Integer uid,Object userHotData);

    List<Object> getListKey_value(String prefix);

    Future<Object> getRedisData(Integer uid) throws InterruptedException;

    Future<Object> getRedisHomeData(String uid) throws InterruptedException;

}