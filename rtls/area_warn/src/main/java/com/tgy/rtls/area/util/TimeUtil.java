package com.tgy.rtls.area.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.area.util
 * @date 2020/10/28
 * 时间相关的判断
 */
public class TimeUtil {
    /*
    * 用于判断规则是否生效
    * */
    public static Boolean RuleEfficient(String startTime,String endTime){
        //当前时间段
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
        Integer currentTime = Integer.valueOf(dateFormat.format(new Date()));
        //如果startTime>=endTime  说明endTime为另一天的时间   startTime<endTime   说明endTime为当天时间
        Integer start= Integer.valueOf(startTime.replace(":",""));
        Integer end= Integer.valueOf(endTime.replace(":",""));
        if (start>=end){
            if (start<currentTime || currentTime<end){
                return true;
            }
        }else {
            if(start<currentTime&&end>currentTime){
                return true;
            }
        }
        return false;
    }
}
