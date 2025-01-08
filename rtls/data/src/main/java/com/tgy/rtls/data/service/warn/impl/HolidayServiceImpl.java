package com.tgy.rtls.data.service.warn.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class HolidayServiceImpl {
    private static final String API_URL = "http://timor.tech/api/holiday/info/";

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 判断当天是否为节假日
     * @return true-节假日（周末或节日），false-工作日（普通工作日或调休）
     */
    public boolean isHoliday() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String cacheKey = "holiday:" + dateStr;
        
        // 先查询缓存
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        String cached = bucket.get();
        if (cached != null) {
            return Boolean.parseBoolean(cached);
        }
        
        try {
            String response = HttpUtil.get(API_URL);
            JSONObject json = JSONObject.parseObject(response);

            if (json.getInteger("code") == 0) {
                JSONObject type = json.getJSONObject("type");
                int typeValue = type.getInteger("type");

                // 判断是否为节假日
                // 工作日(0)和调休(3)返回false，周末(1)和节日(2)返回true
                boolean isHoliday = typeValue == 1 || typeValue == 2;

                // 缓存结果（保存24小时）
                bucket.set(String.valueOf(isHoliday), 24, TimeUnit.HOURS);

                log.info("日期: {}, 类型: {}, 名称: {}, 是否节假日: {}",
                        dateStr, typeValue, type.getString("name"), isHoliday);

                return isHoliday;
            }
        } catch (Exception e) {
            log.error("获取节假日信息失败：{}", dateStr, e);
            // API调用失败时，使用周末判断作为后备方案
            return LocalDate.now().getDayOfWeek().getValue() >= 6;
        }
        
        return false;
    }
    //这是一个main方法，程序的入口
    public static void main(String[] args){
        int value = LocalDate.now().getDayOfWeek().getValue();
        if(value >= 6){
            System.out.println(" = " + 7);
        }
            System.out.println(" = " + 5);
    }
}