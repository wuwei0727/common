//package com.tgy.rtls.web.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.connection.Message;
//import org.springframework.data.redis.listener.RedisMessageListenerContainer;
//import org.springframework.test.context.junit4.SpringRunner;
//
///**
// * @author wuwei
// * @date 2024/3/1 - 11:44
// */
//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//public class RedisEaxTest {
//
//    /**
//     * 测试检测器离线 redis超时回调修改车位状态为占用
//     */
//    @Test
//    public void redisTest(){
//        // 使用Mockito框架创建模拟的Message对象
//        Message message = Mockito.mock(Message.class);
//        // 设置模拟的消息内容
//        Mockito.when(message.toString()).thenReturn("infrared,18534372");
//
//        // 创建一个模拟的byte数组，模拟Redis消息失效事件
//        byte[] pattern = "pattern".getBytes();
//
//        // 创建一个模拟的RedisListener对象
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        RedisKeyExpirationListener redisListener = new RedisKeyExpirationListener(container); // 请替换为你实际的RedisListener类名
//
//        // 调用onMessage方法进行测试
//        redisListener.onMessage(message, pattern);
//    }
//
//}
