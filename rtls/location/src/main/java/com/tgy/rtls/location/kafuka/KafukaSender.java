package com.tgy.rtls.location.kafuka;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafukaSender {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;


    //发送消息方法
    public  void send(String topic,String data) {

      //  logger.error("topic:"+topic+"data"+data);
        //kafkaTemplate.send("test", "AAAA");
     //   ListenableFuture<SendResult<String, String>> res = kafkaTemplate.send(topic, data);
        try {
            kafkaTemplate.send(topic,data);
        }catch (Exception e){

        }

      /* res.addCallback(result -> logger.error("生产者成功发送消息到topic:{} partition:{}的消息", *//*result.getRecordMetadata().topic(), result.getRecordMetadata().partition(),*//*result.getRecordMetadata().hasOffset(),result.getRecordMetadata().offset()),
                ex -> logger.error("生产者发送消失败，原因：{}", ex.getMessage()));*/
     //  kafkaTemplate.send(topic,"1", data);
    }
}
