package com.tgy.rtls.data.entity.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentFontStyle;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.FillPatternTypeEnum;
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
@ApiModel("热门地点表")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontName="微软雅黑",fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(12)
public class HotLocationVo {
    @ExcelProperty(value = "地点名称",index = 0)
    private String locationName;

    @ExcelProperty(value = "搜索次数",index = 1)
    private Integer searchCount;

//    @ExcelProperty(value = "分享次数",index = 1)
//    private Integer shareCount;

    @ExcelProperty(value = "是否是商家",index = 2)
    private String isBusiness="否";

    @ExcelProperty(value = "用户检索总数",index = 4)
    private Integer totalUserSearchCount;

    @ExcelProperty(value = "分享总数",index = 5)
    private Integer totalShareCount;
}