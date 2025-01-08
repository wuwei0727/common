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
 * @Author: wuwei
 * @CreateTime: 2024/6/3 10:31
 */
@Data
@Builder
@ApiModel("车位数据实体类")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(13)
public class ParkingDataVo {
    @ExcelProperty(value = "车位号",index = 0)
    private String parkingSpotNumber;

    @ExcelProperty(value = "公司",index = 1)
    private String company;

    @ExcelProperty(value = "状态",index = 2,converter = CustomConverter.class)
    private String parkingStatus;

    @ExcelProperty(value = "类型",index = 3,converter = CustomConverter.class)
    private String parkingType;

    @ExcelProperty(value = "是否VIP车位",index = 4,converter = CustomConverter.class)
    private String vip;

    @ExcelProperty(value = "配置方式",index =5,converter = CustomConverter.class)
    private String configurationMethod;

    @ExcelProperty(value = "充电状态",index = 6,converter = CustomConverter.class)
    private String chargingStatus;

    @ColumnWidth(20)
    @ExcelProperty(value = "开始时间",index = 8)
    private String hourStart;

    @ColumnWidth(20)
    @ExcelProperty(value = "结束时间",index = 9)
    private String hourEnd;

    @ExcelProperty(value = "空车位数",index = 10)
    private Integer availableParkingSpots;

    @ExcelProperty(value = "车位总数",index = 11)
    private Integer totalParkingSpots;

    @ExcelProperty(value = "占用车位",index = 14)
    private Integer occupiedParkingSpots;

    @ExcelProperty(value = "空闲车位",index = 15)
    private Integer freeParkingSpots;

    @ExcelProperty(value = "充电车位",index = 16)
    private Integer chargingParkingSpots;

    @ExcelProperty(value = "专用车位",index = 17)
    private Integer dedicatedParkingSpots;

    @ExcelProperty(value = "VIP车位",index = 18)
    private Integer vipParkingSpots;
}