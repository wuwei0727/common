//package com.tgy.rtls.web.kafka;
//
//import com.tgy.rtls.data.common.KafukaTopics;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.text.ParseException;
//
///**
// * @BelongsProject: rtls
// * @BelongsPackage: com.tgy.rtls.web.kafka
// * @Author: wuwei
// * @CreateTime: 2024-06-26 09:46
// * @Description: TODO
// * @Version: 1.0
// */
//@Component
//public class KafkaMessageListener {
//
//    @KafkaListener(topics = {KafukaTopics.INFRARED_STATE},groupId = "visual-data-group")
//    public void VisualDataSend(ConsumerRecord<?, ?> msg) {
//        System.err.printf("监听器【A】收到消息，offset = %d，partition=%s，key=%s，value =%s%n,",
//                msg.offset(),msg.partition(), msg.key(), msg.value());
//    }
//
//    @KafkaListener(topics = {KafukaTopics.INFRARED_STATE},groupId = "infrared-state-group")
//    //@KafkaListener(topics = {KafukaTopics.INFRARED_STATE1})
//    public void infrared(ConsumerRecord<?, ?> msg) throws ParseException {
//        System.err.printf("监听器【B】收到消息，offset = %d，partition=%s，key=%s，value =%s%n,",
//                msg.offset(),msg.partition(), msg.key(), msg.value());
//    }
//}
