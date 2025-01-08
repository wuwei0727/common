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
 * @Author: wuwei
 * @CreateTime: 2024/6/3 10:31
 */
@Data
@Builder
@ApiModel("反向寻车")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@ColumnWidth(13)
@HeadFontStyle(fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
public class ReverseCarSearchVo {
    @ExcelProperty(value = "用户",index = 0)
    private String userId;

    @ExcelProperty(value = "反向寻车次数",index = 1)
    private Integer reverseCarSearchCount;

    @ExcelProperty(value = "反向寻车总数",index = 4)
    private Integer totalReverseCarSearches;
}