package com.tgy.rtls.data.config;


import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Funnel;
import org.apache.ibatis.cache.Cache;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class RedisCache implements Cache {

    private String id;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private BloomFilterHelper<String> orderBloomFilterHelper = new BloomFilterHelper<>((Funnel<String>) (from, into) -> into.putString(from, Charsets.UTF_8)
            .putString(from, Charsets.UTF_8), 100 , 0.01);

    public RedisCache(String id) {
        this.id = id;
    }

    private RedisTemplate<Object, Object> getRedisTemplate(){
        return SpringContextHolder.getBean("redisTemplate");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void putObject(Object key, Object value) {

        getRedisTemplate().boundHashOps(getId()).put(key, value);
        //System.out.println("布隆过滤器插入key"+key.toString());
      //  addByBloomFilter(orderBloomFilterHelper,"bool" ,key.toString());

    }

    @Override
    public Object getObject(Object key) {
       // System.out.println("get key---------------------"+key);
     //   if(includeByBloomFilter(orderBloomFilterHelper,"bool",key.toString())) {
        Object object=getRedisTemplate().boundHashOps(getId()).get(key);
      //  System.out.println(object);
            return object;
       /* }else{
            return null;
        }*/
    }

    @Override
    public Object removeObject(Object key) {
        return getRedisTemplate().boundHashOps(getId()).delete(key);
    }

    @Override
    public void clear() {

        getRedisTemplate().delete(getId());
    }

    @Override
    public int getSize() {
        Long size = getRedisTemplate().boundHashOps(getId()).size();
        return size == null ? 0 : size.intValue();
    }

    @Override
    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }
    /**
     * 根据给定的布隆过滤器添加值
     */
    public <T> void addByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            getRedisTemplate().opsForValue().setBit(key, i, true);
        }
    }

    /**
     * 根据给定的布隆过滤器判断值是否存在
     */
    public <T> boolean includeByBloomFilter(BloomFilterHelper<T> bloomFilterHelper, String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            if (!getRedisTemplate().opsForValue().getBit(key, i)) {
                return false;
            }
        }

        return true;
    }
}

