package com.tgy.rtls.data.tool;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    public static void main(String[] args){
        //当前时间
      //  Calendar cl = setCalendar(2014,01,01);
        Timestamp end=new Timestamp(new Date().getTime());
        Calendar c2 = Calendar.getInstance();
   //     Calendar c2 = setCalendar(2014,01,01,23,11,12);
        System.out.print("当前时间:");

     /*   printCalendar(c2);
        //前一天
         c2 = setCalendar(2014,01,01,23,11,12);
        getBeforeDay(c2);
        System.out.print("前一天:");
        printCalendar(c2);
        //后一天
         c2 = setCalendar(2014,01,01,23,11,12);
        getAfterDay(c2);*/
        System.out.print("后一天:"+ printCalendar(getBeforeN_Day(c2,10)));

    }

    /**
     * 设置时间
     * @param year
     * @param month
     * @param date
     * @return
     */
    public static Calendar setCalendar(int year,int month,int date){
        Calendar cl = Calendar.getInstance();
        cl.set(year, month-1, date);
        return cl;
    }


    /**
     * 设置时间 精确到秒
     * @param year
     * @param month
     * @param date
     * @param hour
     * @param minute
     * @param second
     * @return
     */
    public static Calendar setCalendar(int year,int month,int date,int hour,int minute,int second){
        Calendar cl = Calendar.getInstance();
        cl.set(year, month, date,hour,minute,second);
        return cl;
    }

    /**
     * 获取当前时间的前一天时间
     * @param cl
     * @return
     */
    public static Calendar getBeforeDay(Calendar cl){
        //使用roll方法进行向前回滚
        //cl.roll(Calendar.DATE, -1);
        //使用set方法直接进行设置
        int day = cl.get(Calendar.DATE);
        cl.set(Calendar.DATE, day-1);
        return cl;
    }

    /**
     * 获取当前时间的前一天时间
     * @param cl
     * @return
     */
    public static Calendar getBeforeN_Day(Calendar cl,int n){
        //使用roll方法进行向前回滚
        //cl.roll(Calendar.DATE, -1);
        //使用set方法直接进行设置
        int day = cl.get(Calendar.DATE);
        cl.set(Calendar.DATE, day-n);
        return cl;
    }



    /**
     * 获取当前时间的后一天时间
     * @param cl
     * @return
     */
    public static Calendar getAfterDay(Calendar cl){
        //使用roll方法进行回滚到后一天的时间
        //cl.roll(Calendar.DATE, 1);
        //使用set方法直接设置时间值
        int day = cl.get(Calendar.DATE);
        cl.set(Calendar.DATE, day+1);
        return cl;
    }


    /**
     * 打印时间
     * @param cl
     */
    public static String printCalendar(Calendar cl){
        int year = cl.get(Calendar.YEAR);
        int month = cl.get(Calendar.MONTH)+1;
        int day = cl.get(Calendar.DATE);
        int hour = cl.get(Calendar.HOUR_OF_DAY);
        int  minute= cl.get(Calendar.MINUTE);
        int  second = cl.get(Calendar.SECOND);
        //System.out.println(year+"-"+month+"-"+day+"-"+hour+"-"+minute+"-"+second);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     //   System.out.println( );
        return dateFormat.format(cl.getTime());
    }

}
