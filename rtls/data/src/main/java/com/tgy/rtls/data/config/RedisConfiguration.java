//package com.tgy.rtls.data.config;
//
//import com.fasterxml.jackson.annotation.JsonAutoDetect;
//import com.fasterxml.jackson.annotation.PropertyAccessor;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericToStringSerializer;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//import java.io.Serializable;
//
//@Configuration
//public class RedisConfiguration {
//
//    @Autowired
//    private RedisConnectionFactory redisConnectionFactory;
//    @Value("${spring.redis.host}")
//    private String host;
//
//    @Value("${spring.redis.port}")
//    private String port;
//
// /*   @Bean
//    public RedisTemplate<String,Object> redisTemplate() {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        initDomainRedisTemplate(redisTemplate, redisConnectionFactory);
//        return redisTemplate;
//    }*/
//    /**
//     * @Title: getDefaultRedisTemplate
//     * @Description: Get a default redis cache template.
//     * @return
//     */
//   @Bean
//    public RedisTemplate<String, Serializable> getDefaultRedisTemplate() {
//        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate();
//        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        redisTemplate.setValueSerializer(new GenericToStringSerializer(Long.class));
//        redisTemplate.setExposeConnection(true);
//        //设置当前的redis连接工厂
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//
//    /**
//     * 设置数据存入 redis 的序列化方式
//     * @param template
//     * @param factory
//     */
//    private void initDomainRedisTemplate(RedisTemplate<String, Object> template, RedisConnectionFactory factory) {
//        // 定义 key 的序列化方式为 string
//        // 需要注意这里Key使用了 StringRedisSerializer，那么Key只能是String类型的，不能为Long，Integer，否则会报错抛异常。
//        StringRedisSerializer redisSerializer = new StringRedisSerializer();
//        template.setKeySerializer(redisSerializer);
//        // 定义 value 的序列化方式为 json
//        @SuppressWarnings({"rawtypes", "unchecked"})
//        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
//        ObjectMapper om = new ObjectMapper();
//        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
//        jackson2JsonRedisSerializer.setObjectMapper(om);
//        template.setValueSerializer(jackson2JsonRedisSerializer);
//
//        //hash结构的key和value序列化方式
//        template.setHashKeySerializer(jackson2JsonRedisSerializer);
//        template.setHashValueSerializer(jackson2JsonRedisSerializer);
//        template.setEnableTransactionSupport(true);
//        template.setConnectionFactory(factory);
//    }
//
//
//        private static final String REDIS_URL = "redis://127.0.0.1:6379";
//
//        public static RedissonClient create() {
//            Config config = new Config();
//            config.useSingleServer().setAddress(REDIS_URL);
//            return Redisson.create(config);
//        }
//
//
//}
