package com.tgy.rtls.data.service.sms;

import cn.hutool.core.util.RandomUtil;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.tool.PhoneNumberValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;

    private static final String REDIS_PREFIX = "verification:";
    private static final long EXPIRE_SECONDS = 300; // 5分钟过期

    public CommonResult<Object> sendVerificationCode(String phoneNumber) {
        try {
            boolean b = PhoneNumberValidator.validatePhoneNumber(phoneNumber);
            if (!b) {
                return new CommonResult<>(410, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }
            String key = REDIS_PREFIX + phoneNumber;
            String code = RandomUtil.randomNumbers(6);
            Map<String, Object> templateParam = new HashMap<>();
            templateParam.put("code", code);
            Map<Object, Object> existingHash = redisTemplate.opsForHash().entries(key);
            String existingCode = existingHash.get("code") == null ? null : (String) existingHash.get("code");
            if (existingCode != null) {
                // 验证码已存在且未过期
                int count = Integer.parseInt(redisTemplate.opsForHash().get(key, "count").toString());
                if(count==-1){
                    return new CommonResult<>(888, LocalUtil.get("验证码： 888888  ,未过期，请不要重新获取！！！"));
                }
                if (count >= 50) {
                    // 发送次数超过5次，修改过期时间为24小时
                    redisTemplate.expire(key, 24, TimeUnit.HOURS);
                    return new CommonResult<>(410, LocalUtil.get("发送次数过多，请稍后再试！！！"));
                } else {
                    // 判断距离上次发送时间间隔是否少于60秒
                    long lastSendTime = Long.parseLong(redisTemplate.opsForHash().get(key, "lastSendTime").toString());
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastSendTime < 60000) {
                        return new CommonResult<>(410, LocalUtil.get("发送频繁，请稍后再试！！！"));
                    }
                    // 更新发送次数和时间戳
                    redisTemplate.opsForHash().increment(key, "count", 1);
                    redisTemplate.opsForHash().put(key, "lastSendTime", String.valueOf(currentTime));
                }
            }
            // 第一次发送验证码
            long currentTime = System.currentTimeMillis();
            redisTemplate.opsForHash().put(key, "code", code);
            redisTemplate.opsForHash().put(key, "count", "1");
            redisTemplate.opsForHash().put(key, "lastSendTime", String.valueOf(currentTime));
            redisTemplate.expire(key, EXPIRE_SECONDS, TimeUnit.SECONDS);

            Boolean sendMessage = aLiYunSmsService.sendMessage(phoneNumber, "SMS_469850002", templateParam);
            if (sendMessage) {
                return new CommonResult<>(200, LocalUtil.get("恭喜！短信已发送，请注意查收。若未收到，请稍后重试!!!"));
            }
            return new CommonResult<>(410, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }

    }
//这是一个main方法，程序的入口
public static void main(String[] args){
    String a ="1";
}
    public boolean verifyVerificationCode(String phoneNumber, String code) {
        String key = REDIS_PREFIX + phoneNumber;
        String storedCode = (String) redisTemplate.opsForHash().get(key, "code");
        return code.equals(storedCode);
    }
}
