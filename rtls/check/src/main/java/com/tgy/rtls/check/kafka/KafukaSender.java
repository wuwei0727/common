package com.tgy.rtls.check.kafka;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component("checksender")
@Slf4j
public class KafukaSender {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;


    //发送消息方法
    public  void send(String topic,String data) {

       // logger.error("topic:"+topic+"data"+data);
        //kafkaTemplate.send("test", "AAAA");
        kafkaTemplate.send(topic, data);
    }
}
