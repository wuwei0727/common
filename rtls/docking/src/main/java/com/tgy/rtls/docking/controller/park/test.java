package com.tgy.rtls.docking.controller.park;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2024-08-08 14:55
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class test {
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private PlaceVideoScheduledTasks scheduledTasks;

    @GetMapping("/setPlaceName")
    public String setPlaceName(@RequestParam String placeName) {
        scheduledTasks.setPlaceName(placeName);
        return "地点参数已设置为：" + placeName;
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        for (int i = 0; i < 1360; i++) {
            System.out.println("LocalDateTime.now() = " + LocalDateTime.now());
        }
    }

}
