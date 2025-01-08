package com.tgy.rtls.data.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.tgy.rtls.data.config.CustomConverter;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
*@Author: wuwei
*@CreateTime: 2024/6/3 10:33
*/

@Data
@Builder
@ApiModel("设备")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(13)
public class DevicesVo {
    @ExcelProperty(value = "设备编号",index = 0)
    private String num;

    @ExcelProperty(value = "设备类型",index = 1, converter = CustomConverter.class)
    private String devicesType;

    @ExcelProperty(value = "设备状态",index = 2, converter = CustomConverter.class)
    private String devicesStatus;

    @ExcelProperty(value = "电量",index = 3)
    private String power;

    @ExcelProperty(value = "检测器总数",index = 5)
    private Integer totalDetectors;

    @ExcelProperty(value = "在线检测器",index = 6)
    private Integer onlineDetectors;

    @ExcelProperty(value = "离线检测器",index = 7)
    private Integer offlineDetectors;

//    @ExcelProperty(value = "低电量检测器",index = 8)
//    private Integer lowBatteryDetectors;

    @ExcelProperty(value = "信标总数",index = 10)
    private Integer totalBeacons;

    @ExcelProperty(value = "在线信标",index = 11)
    private Integer onlineBeacons;

    @ExcelProperty(value = "离线信标",index = 12)
    private Integer offlineBeacons;

//    @ExcelProperty(value = "低电量信标",index = 13)
//    private Integer lowBatteryBeacons;

    @ExcelProperty(value = "网关总数",index = 15)
    private Integer totalGateways;

    @ExcelProperty(value = "在线网关",index = 16)
    private Integer onlineGateways;

    @ExcelProperty(value = "离线网关",index = 17)
    private Integer offlineGateways;
}