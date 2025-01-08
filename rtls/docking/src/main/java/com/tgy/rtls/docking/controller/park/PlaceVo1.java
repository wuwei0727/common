package com.tgy.rtls.docking.controller.park;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontName="微软雅黑",fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(12)
public class PlaceVo1 implements Serializable {
    @ExcelProperty(value = "车位名称",index = 0)
    private String name;

    @ExcelProperty(value = "接口返回结果",index = 1)
    private String state;

    @ExcelProperty(value = "车牌",index = 2)
    private String license;

    @JSONField(name = "PlateNo")
    @ExcelProperty(value = "车牌",index = 3)
    private String plateNo;
}