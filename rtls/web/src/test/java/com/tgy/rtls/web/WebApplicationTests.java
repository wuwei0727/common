package com.tgy.rtls.web;

import com.tgy.rtls.data.entity.view.PFindCar;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.mapper.view.FindCarMapper;
import com.tgy.rtls.data.service.sms.ALiYunSmsService;
import com.tgy.rtls.data.service.vip.VipParkingService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class WebApplicationTests {
    @Autowired
    private VipParkingService vipParkingService;
    @Autowired
    private ALiYunSmsService aLiYunSmsService;
    @Autowired
    private FindCarMapper findcarMapper;




    @Test
    public void testInsertFindCar(){
        PFindCar findcar = new PFindCar();
//        findcar.setId(1L);
        findcar.setMap(74L);
        findcar.setPlace(1111L);
        findcar.setPlaceName("111");
        findcar.setStartTime(LocalDateTime.now());
        findcarMapper.insertFindCar(findcar);
        }

    @Test
    public void testInsertFindCar1(){
        System.out.println("74".hashCode()%2);
        System.out.println(75%2);
        System.out.println("221".hashCode()%2);
        System.out.println("178".hashCode()%2);
    }

    void contextLoads() {
    }

    @Test
    public void conditionDebug() {
        for (int i = 0; i < 30; i++) {
            System.out.println(i);
        }
    }

    @Test
    public void thread() {
        System.out.println("主线程开始");
        new Thread(() -> {
            System.out.println("我是线程1-1");
            System.out.println("我是线程1-2");
            System.out.println("我是线程1-3");
        }, "线程1").start();

        new Thread(() -> {
            System.out.println("我是线程2-1");
            System.out.println("我是线程2-2");
            System.out.println("我是线程2-3");
        }, "线程2").start();

        System.out.println("主线程开始结束");
    }

    /**
     * 回到上一个断点
     */
    @Test
    public void back() {
        int num = 10;
        method1(num);
    }

    public void method1(int num) {
        num += 10;
        method2(num);
    }

    public void method2(int num) {
        num += 10;
        System.out.println(111);
        System.out.println(222);
        System.out.println(333);
        System.out.println(444);
        System.out.println(555);
    }

    @Test
    public  void method3() {
        LocalDateTime now = LocalDateTime.now();
        // 创建一个定时器,每分钟执行一次任务
        // 获取所有预约车位的信息，包括开始时间和结束时间
        List<VipParking> reservations = vipParkingService.getVipParkingOccupyReminderUser();
        // 遍历所有预约车位信息，针对每个预约车位计算差值并发送提醒消息
        for (VipParking reservation : reservations) {
            LocalDateTime endReservationTime = reservation.getEndTime();
            // 计算当前时间与预约结束时间的差值，单位为分钟
            long minutesLeft = ChronoUnit.MINUTES.between(LocalDateTime.now(), endReservationTime);
            Map<String, Object> map = new HashMap<>();
            // 如果差值小于等于10分钟，发送提醒消息给用户
            if (minutesLeft <= 10) {
                String message = String.format("您预约的车位被占用。", minutesLeft);
                map.put("mapName", reservation.getMapName());
                map.put("parkingName", reservation.getParkingName());
                aLiYunSmsService.sendMessage(reservation.getPhone(), "SMS_460815016",map);
            }
        }
    }
}
