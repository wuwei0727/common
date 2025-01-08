package com.tgy.rtls.data.tool;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class VoltTool {

    public static int volt_percent(float volt) {
        if (volt >= 3.1) {
            return 100;
        }
        if (volt < 3.1) {
            return 10;
        }
        if (volt >= 3.1 && volt <= 3.3) {
            return 80;
        }
        return 0;
    }

    public static void main(String[] args) {
        // 安装时间（示例）
        LocalDateTime installTime = LocalDateTime.of(2024, 1, 1, 10, 30, 0);

        // 当前时间
        LocalDateTime now = LocalDateTime.now();

        // 计算时间差
        Duration duration = Duration.between(installTime, now);

        // 获取不同单位的时间差
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        // 打印结果
        System.out.printf("安装时间：%s%n", installTime);
        System.out.printf("当前时间：%s%n", now);
        System.out.printf("已运行：%d天%d小时%d分钟%d秒%n",
                days, hours, minutes, seconds);

        // 使用ChronoUnit计算各个时间单位
        System.out.println("\n使用ChronoUnit计算：");
        System.out.printf("总天数：%d天%n", ChronoUnit.DAYS.between(installTime, now));
        System.out.printf("总小时：%d小时%n", ChronoUnit.HOURS.between(installTime, now));
        System.out.printf("总分钟：%d分钟%n", ChronoUnit.MINUTES.between(installTime, now));
        System.out.printf("总秒数：%d秒%n", ChronoUnit.SECONDS.between(installTime, now));
    }
}
