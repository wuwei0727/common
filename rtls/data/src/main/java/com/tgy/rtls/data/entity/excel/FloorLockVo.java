package com.tgy.rtls.data.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuwei
 * @createTime 2023/4/5 16:13
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontName="微软雅黑",fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(12)
public class FloorLockVo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ExcelProperty(value = "设备编号",index = 0)
    private String deviceNum;

    @ExcelProperty(value = "地图名称",index = 1)
    private String mapName;

    @ExcelProperty(value = "车位名称",index = 2)
    private String parkingName;

    @ExcelProperty(value = "车位状态",index = 3)
    private String state;

    @ExcelProperty(value = "楼层",index = 4)
    private String floorName;

    @ApiModelProperty(value="地锁状态.0：降锁 1：升锁,3/4：位置异常状态")
    @ExcelProperty(value = "地锁状态",index = 5)
    private String floorLockState;

    @ExcelProperty(value = "地锁模式",index = 6)
    @ApiModelProperty(value="2:正常模式3:升锁模式4：降锁模式")
    private String model;

    @ExcelProperty(value = "电量",index = 7)
    private String power;

    @ExcelProperty(value = "网络状态",index = 8)
    @ApiModelProperty(value="0离线1在线2低电量")
    private String networkName;


    @ExcelProperty(value = "检测状态",index = 9)
    private String detectionState;

    @ExcelProperty(value = "最后在线时间信息",index = 10)
    private LocalDateTime offlineTime;

    @ExcelProperty(value = "添加时间",index = 11)
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime startTime;

    @ExcelProperty(value = "更新时间",index = 12)
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime endTime;

}