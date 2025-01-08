package com.tgy.rtls.data.common;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.util
 * @date 2020/10/28
 * 时间相关的判断
 */
@Slf4j
public class TimeUtil {
    /*
    * 用于考勤报表计算工时
    * */
    public static String StatementDuration(String inTime,String outTime){
        try {
            String[] time1=inTime.split(":");
            String[] time2=outTime.split(":");
            Integer hour=Integer.parseInt(time2[0])-Integer.parseInt(time1[0]);
            Integer minute=Integer.parseInt(time2[1])-Integer.parseInt(time1[1]);
            return hour+"小时"+minute+"分钟";
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /*
    * 用于计算考勤报表状态
    * */
    public static Integer StatementStaus(String inTime,String outTime){
        try {
            String[] time1=inTime.split(":");
            String[] time2=outTime.split(":");
            int hour=Integer.parseInt(time2[0])-Integer.parseInt(time1[0]);
            int min=Integer.parseInt(time2[1])-Integer.parseInt(time1[1]);
            return min+(hour*60);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
    /**
     * 获取当天开始时间(2021-10-28 00:00:00)
     *
     **/
    public static String getDayStartTimeStr() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        String todayStartTime = todayStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return todayStartTime;
    }

    /**
     * 获取当天结束时间(2021-10-28 23:59:59)
     *
     **/
    public static String getDayEndTimeStr() {
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        String todayEndTime = todayEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return todayEndTime;
    }

    public static String getCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1; // Calendar 类的月份是从 0 开始的
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        //log.info("当前时间：" + year + "年" + month + "月" + day + "日" + hour + ":" + minute + "分");
        return year + "年" + month + "月" + day + "日" + hour + ":" + minute + "分";
    }

    public static String timestampToStr(Long timestamp){
        String timeStr = null;
        if(!NullUtils.isEmpty(timestamp)){
            LocalDateTime beijingTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("Asia/Shanghai"));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            timeStr = beijingTime.format(formatter);
        }
        return timeStr;
    }

    public static LocalDateTime strTimeToLocalDateTime(String time){
        LocalDateTime startTime = null;
        if(!NullUtils.isEmpty(time)){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            startTime = LocalDateTime.parse(time, formatter);
        }
        return startTime;
    }

    public static String localDateTimeToStrTime(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }

    // Helper method to parse timestamp string to Date object
    public static Date parseTimestamp(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
