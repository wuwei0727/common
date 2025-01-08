package com.tgy.rtls.docking.controller.park;

import com.tgy.rtls.docking.service.park.PlaceVideoDetectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.controller.park
 * @Author: wuwei
 * @CreateTime: 2024-08-10 21:16
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/test")
public class ttttttt {
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;
    @Autowired
    private PlaceVideoScheduledTasks scheduledTasks;
    @Resource
    private PlaceVideoDetectionService placeVideoDetectionService;


    @GetMapping("/getPlaceVideoDetectionData1")
    public void getPlaceVideoDetectionData1() {
        // 获取逻辑核心数,如6核心12线程,那么返回的是12
        int i = Runtime.getRuntime().availableProcessors();
        log.error("getPlaceVideoDetectionData1"+i );

        placeVideoDetectionService.getPlaceVideoDetectionDataq();
    }



    //这是一个main方法，程序的入口
    public static void main(String[] args){
        int i = Runtime.getRuntime().availableProcessors();
        log.error("getPlaceVideoDetectionData1:"+i );
    }
}
