package com.tgy.rtls.web.controller.test;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.InfraredMessage;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.service.park.PlaceVideoDetectionService;
import com.tgy.rtls.data.service.video.VideoPlaceStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private VideoPlaceStatusService videoPlaceStatusService;
    @Autowired
    private ParkingService parkingService;
    @Autowired
    private PlaceVideoDetectionService placeVideoDetectionService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private TagMapper tagMapper;
    private static final String B_SERVICE_URL = "http://localhost:8081/api/data/upload"; // B 服务的 URL

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @PostMapping("/send")
    public String sendMessage(Integer num) {
        List<InfraredMessage> infrareds = tagMapper.findIredByIdAndName1(num);
        InfraredMessage infraredMessage ;
        if(NullUtils.isEmpty(infrareds)){
            return "检查检测器编号是否存在";
        }
        infraredMessage= infrareds.get(0);
        infraredMessage.setBatteryTime(new Date());

        String topic = "infrared_state1"; // Kafka主题名称

        // 发送消息
        kafkaTemplate.send(topic, infraredMessage.toString()).addCallback(
                result -> {
                    if (result != null) {
                        System.out.println("消息发送成功，offset: " + result.getRecordMetadata().offset());
                    } else {
                        System.err.println("消息发送失败");
                    }
                },
                ex -> System.err.println("消息发送失败，原因：" + ex.getMessage())
        );

        return "消息已发送";
    }
    // A 服务上传数据的接口
//    @PostMapping("/upload")
//    public String uploadData(@RequestParam String data) {
//        // 打印接收到的数据
//        System.out.println("接收到的数据: " + data);
//
//        // 更新 A 服务的最后上传时间
//        p.updateLastUploadTime(data);
//
//        return "数据上传成功";
//    }

    // A 服务上传数据的接口
    @PostMapping("/sendData")
    public String sendData() {
        // 设置请求头
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        // 创建请求实体
//        HttpEntity<String> request = new HttpEntity<>(data, headers);
//        // 向 B 服务发送数据
//        ResponseEntity<String> response = restTemplate.postForEntity(B_SERVICE_URL, request, String.class);
        placeVideoDetectionService.updateLastUploadTime(String.valueOf(178));
        // 返回 B 服务的响应
        return "A to B is ok and status is ok: ";
    }
}
