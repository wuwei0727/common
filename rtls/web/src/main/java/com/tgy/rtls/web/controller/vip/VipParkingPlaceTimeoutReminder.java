package com.tgy.rtls.web.controller.vip;

import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.vip
 * @Author: wuwei
 * @CreateTime: 2023-05-09 15:47
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@Configuration
@EnableAsync
public class VipParkingPlaceTimeoutReminder {
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;


    private static VipParkingService  vipParking;

    /**
     *解决spring静态注入
     */
    @PostConstruct
    public void init() {
        vipParking=vipParkingService;
    }


    // 启动定时任务
    @Async(value = "VipCarBitTimeoutOrOccupyTaskExecutor")
    @Scheduled(cron="0 * * * * ?")
    public void parkingReminder() {
        LocalDateTime now = LocalDateTime.now();
        // log.error(Thread.currentThread().getName()+"===task run"+now);
        // 获取所有预约车位的信息，包括开始时间和结束时间
        List<VipParking> reservations = vipParkingService.getVipParkingPlaceTimeoutParking();
        // 遍历所有预约车位信息，针对每个预约车位计算差值并发送提醒消息
        for (VipParking reservation : reservations) {
            LocalDateTime endReservationTime = reservation.getEndTime();
            // 计算当前时间与预约结束时间的差值，单位为分钟
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), endReservationTime);

            Map<String, Object> map = new HashMap<>();
            // 如果差值小于等于10分钟，发送提醒消息给用户
            if (minutesLeft <= 10) {
                // String message = String.format("您预约的车位将在不到%d分钟后到期，请合理安排时间。", minutesLeft);
                // log.error(message+now);
                map.put("mapName", reservation.getMapName());
                map.put("parkingName", reservation.getParkingName());
                aLiYunSmsService.sendMessage(reservation.getPhone(),"SMS_460765018",map);
            }
        }
    }

    @Async(value = "VipCarBitTimeoutOrOccupyTaskExecutor")
    @Scheduled(cron="0 * * * * ?")
    public void parkingOccupyReminder() {
        LocalDateTime now = LocalDateTime.now();
        // log.info(Thread.currentThread().getName()+"===task run");
        // 创建一个定时器,每分钟执行一次任务
        // 获取所有预约车位的信息，包括开始时间和结束时间
        List<VipParking> reservations = vipParkingService.getVipParkingOccupyReminderUser();
        // 遍历所有预约车位信息，针对每个预约车位计算差值并发送提醒消息
        for (VipParking reservation : reservations) {
            LocalDateTime endReservationTime = reservation.getStartTime();
            // 计算当前时间与预约结束时间的差值，单位为分钟
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), endReservationTime);
            Map<String, Object> map = new HashMap<>();
            // 如果差值小于等于10分钟，发送提醒消息给用户
            if (minutesLeft <= 10) {
                // String message = String.format("您预约的车位被占用。", minutesLeft);
                // log.error(message+now);
                map.put("mapName", reservation.getMapName());
                map.put("parkingName", reservation.getParkingName());
                aLiYunSmsService.sendMessage(reservation.getPhone(), "SMS_460815016",map);
            }
        }
    }
}

