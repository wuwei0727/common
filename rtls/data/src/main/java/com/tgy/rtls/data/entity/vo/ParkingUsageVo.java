package com.tgy.rtls.data.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.alibaba.fastjson.JSON;
import com.tgy.rtls.data.entity.vip.FloorLock;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @Author: wuwei
 * @CreateTime: 2024/6/3 10:31
 */

@Data
@Builder
@ApiModel("车位使用实体类")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(13)
public class ParkingUsageVo {
    @ExcelProperty(value = "车位号",index = 0)
    private String parkingSpotNumber;

    @ExcelProperty(value = "空闲时长(单位:小时)",index = 1)
    private String idleDurationSeconds;

    @ExcelProperty(value = "使用次数",index = 2)
    private Integer usageCount;

    @ExcelProperty(value = "导航次数",index = 3)
    private Integer navigationCount;

    @ExcelProperty(value = "车位导航总数",index = 6)
    private Integer totalParkingNavigations;

    @ExcelProperty(value = "车位使用总数",index = 7)
    private Integer totalParkingUsages;

    @ExcelProperty(value = "车位导航使用率",index = 8)
    private String parkingNavigationUsageRate;

    @ExcelProperty(value = "平台车位利用率",index = 9)
    private String mapPlatformUtilizationRate;

    @ExcelProperty(value = "停车场车位空闲率",index = 10)
    private String parkingLotIdleRate;

    //这是一个main方法，程序的入口
    public static void main(String[] args){

        String json = "{\"fid\":\"\",\"mapImg\":\"\",\"parkingName\":\"\",\"deviceNum\":\"210\",\"networkstate\":1,\"company\":0,\"model\":\"3\",\"id\":18,\"mapName\":\"鲁商广场停车场\",\"place\":0,\"power\":\"8\",\"state\":1,\"floor\":\"\",\"map\":162,\"floorLockState\":\"1\",\"themeImg\":\"\",\"appName\":\"material\",\"mapKey\":\"cec2557bbcc60f0a2da3ed4ceb7a6f36\",\"fmapID\":\"1341330958641369090\",\"x\":\"\",\"floorLockId\":0,\"y\":\"\",\"mapId\":\"162\"}";
        FloorLock floorLock = JSON.parseObject(json, FloorLock.class);
        // 将当前时间转换为时间戳
        long timestamp = LocalDateTime.now().atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();

//        FloorLock floorLock = JSON.parseObject(json, FloorLock.class);
        // Print the JSON string
        System.out.println(timestamp);
        System.out.println(floorLock);
    }
}