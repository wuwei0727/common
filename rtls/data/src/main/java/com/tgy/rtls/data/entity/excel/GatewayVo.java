package com.tgy.rtls.data.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.equip.Infrared;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;


/**
*@Author: wuwei
*@CreateTime: 2024/6/24 16:53
*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontName="微软雅黑",fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(12)
public class GatewayVo implements Serializable {
    @ExcelProperty(value = "设备编号",index = 0)
    private String num;
    @ExcelProperty(value = "关联地图",index = 1)
    private String mapName;

    @ExcelProperty(value = "楼层",index = 2)
    private String floorName;
    @ExcelProperty(value = "网络状态",index = 3)
    private String networkName;

    @ExcelProperty(value ="网关IP",index = 4)
    private String ip;

    @ExcelProperty(value = "电压检测时间",index = 5)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String batteryTime;

    @ExcelProperty(value = "创建时间",index = 6)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String addTime;

    @ExcelProperty(value = "最后在线时间",index = 7)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String offlineTime;


    public static void main(String[] args) {
        // 创建具体的测试时间
        LocalDateTime installTime = LocalDateTime.parse("2024-10-24T22:29:13");
        int lifetimeMonths = 60;

        Infrared infrared = new Infrared(installTime, lifetimeMonths);

        // 测试电池电量
        testBattery(infrared, "实际场景测试");

        // 额外显示详细信息
        printDetailedInfo(infrared);
    }

    private static void testBattery(Infrared infrared, String testName) {
        short batteryPercentage = 100;
        Integer lifetimeMonths = infrared.getLifetimeMonths();

        if (lifetimeMonths != null && lifetimeMonths > 0) {
            LocalDateTime installTime = infrared.getLocalDateTime();
            if(installTime == null) {
                installTime = LocalDateTime.now();
            }
            LocalDateTime now = LocalDateTime.now();
            long monthsBetween = ChronoUnit.MONTHS.between(installTime, now);

            double percentage = (1 - ((double)monthsBetween / lifetimeMonths)) * 100;
            percentage = Math.round(percentage);
            batteryPercentage = (short) Math.max(0, Math.min(100, percentage));
        }

        System.out.println(testName + " - 电池电量: " + batteryPercentage + "%");
    }

    private static void printDetailedInfo(Infrared infrared) {
        System.out.println("\n详细信息：");
        System.out.println("安装时间：" + infrared.getLocalDateTime());
        System.out.println("预期寿命：" + infrared.getLifetimeMonths() + "个月");
        System.out.println("当前时间：" + LocalDateTime.now());

        long monthsBetween = ChronoUnit.MONTHS.between(
                LocalDateTime.now(),
                infrared.getLocalDateTime()
        );
        System.out.println("距离安装时间相差：" + Math.abs(monthsBetween) + "个月");
    }
}
