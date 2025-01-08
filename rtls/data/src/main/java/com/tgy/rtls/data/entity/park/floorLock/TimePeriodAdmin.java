package com.tgy.rtls.data.entity.park.floorLock;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 11:27
*@Description: TODO
*@Version: 1.0
*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "time_period_admin")
public class TimePeriodAdmin extends BaseEntitys implements Serializable {

    @TableField(value = "company_id", updateStrategy = FieldStrategy.IGNORED)
    private Integer companyId;

    @TableField(value = "company_name", updateStrategy = FieldStrategy.IGNORED)
    private String companyName;

    @TableField(value = "`mapId`", updateStrategy = FieldStrategy.IGNORED)
    private Integer mapId;
    @TableField(value = "map_name", updateStrategy = FieldStrategy.IGNORED)
    private String mapName;

    @TableField(value = "day_of_week")
    private String dayOfWeek;

    @TableField(value = "start_time")
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime startTime;

    @TableField(value = "end_time")
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime endTime;

    @TableField(value = "period_type",exist = false)
    private String periodType;

    private static final long serialVersionUID = 1L;
    //这是一个main方法，程序的入口
    public static void main(String[] args) {
        // 获取当前时间
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);

        // 格式化时间为小时和分钟
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        String formattedTime = now.format(formatter);
        System.out.println("当前时间（格式化后）：" + formattedTime);

        // 假设另一个时间点（比如说2小时后的时间）
        LocalTime otherTime = now.plusHours(2).minusMinutes(1);  // 示例时间为当前时间加1小时59分钟
        String formattedOtherTime = otherTime.format(formatter);
        System.out.println("另一个时间（格式化后）：" + formattedOtherTime);

        // 比较时间差是否小于2小时
        long hoursBetween = ChronoUnit.HOURS.between(now, otherTime);
        long minutesBetween = ChronoUnit.MINUTES.between(now, otherTime);

        System.out.println("小时差：" + hoursBetween);
        System.out.println("分钟差：" + minutesBetween);

        if (hoursBetween < 2) {
            System.out.println("两个时间点的差距小于2小时");
        } else {
            System.out.println("两个时间点的差距不小于2小时");
        }
    }
}