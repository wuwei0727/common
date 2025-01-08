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
@ApiModel("车位预约")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@ColumnWidth(13)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
public class ParkingReservationVo {
    @ExcelProperty(value = "车位号",index = 0)
    private String parkingSpotNumber;

    @ExcelProperty(value = "车牌号",index = 1)
    private String licensePlateNumber;

    @ExcelProperty(value = "预约人",index = 2)
    private String reserved;

    @ExcelProperty(value = "手机号码",index = 3)
    private String phoneNumber;

    @ColumnWidth(20)
    @ExcelProperty(value = "预约开始时间",index = 4)
    private String reservationStartTime;

    @ColumnWidth(20)
    @ExcelProperty(value = "预约结束时间",index = 5)
    private String reservationEndTime;

    @ExcelProperty(value = "车位状态",index = 6,converter = CustomConverter.class)
    private String parkingStatus;

    @ExcelProperty(value = "用户ID",index = 7)
    private String userId;
    @ExcelProperty(value = "预约来源",index = 8,converter = CustomConverter.class)
    private String source;
    @ExcelProperty(value = "预约状态",index = 9,converter = CustomConverter.class)
    private String status;

    @ExcelProperty(value = "车位预约总数",index = 12)
    private Integer totalReservations;
}