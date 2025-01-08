package com.tgy.rtls.web.util;

import com.tgy.rtls.data.entity.equip.InfraredMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Date;
import java.util.Properties;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.util
 * @Author: wuwei
 * @CreateTime: 2023-06-15 15:24
 * @Description: TODO
 * @Version: 1.0
 */
public class a {
    public static void main(String[] args) {

        String bootstrapServers = "192.168.1.95:9092"; // Kafka broker地址
        String topic = "infrared_state1"; // Kafka主题名称

        // 配置Kafka Producer
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer .class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // 创建Kafka Producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);


        InfraredMessage message = new InfraredMessage();
        message.setId(191901123);
        message.setNum("6144");
        message.setNetworkName("在线");
        message.setNetworkstate(1);
        message.setPower((short) 60);
        message.setFloor(1);
        message.setX(12634560.745891642);
        message.setY(2653504.083253146);
        message.setRawProductId("TGY24050001800");

        message.setMap(75);
        message.setPlace(15097);
        message.setPlaceName("E359");
        message.setAddTime(new Date());
        message.setBatteryTime(new Date());
        message.setStatus(1);

// 转换为JSON字符串
        String jsonString = message.toString();

        long time = System.currentTimeMillis();
        // 创建消息
//        String message ="{\"fid\":\"\",\"mapImg\":\"\",\"parkingName\":\"\",\"deviceNum\":\"210\",\"networkstate\":1,\"company\":0,\"model\":\"3\",\"id\":18,\"mapName\":\"鲁商广场停车场\",\"place\":111,\"power\":\"8\",\"state\":1,\"floor\":\"\",\"map\":162,\"floorLockState\":\"1\",\"themeImg\":\"\",\"appName\":\"material\",\"mapKey\":\"cec2557bbcc60f0a2da3ed4ceb7a6f36\",\"fmapID\":\"1341330958641369090\",\"x\":\"\",\"floorLockId\":0,\"y\":\"\",\"mapId\":\"162\"}";

        // 创建ProducerRecord并发送消息
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, jsonString);
        producer.send(record, (metadata, exception) -> {
            if (exception == null) {
                System.out.println("消息发送成功，offset: " + metadata.offset());
            } else {
                System.err.println("消息发送失败，原因：" + exception.getMessage());
            }
        });

        // 关闭Kafka Producer
        producer.close();
    }
}

