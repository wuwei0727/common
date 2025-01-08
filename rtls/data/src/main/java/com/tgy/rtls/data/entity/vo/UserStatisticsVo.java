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
*@CreateTime: 2024/6/3 10:27
*/
@Data
@Builder
@ApiModel("用户统计")
@NoArgsConstructor
@AllArgsConstructor
@HeadStyle(fillPatternType = FillPatternTypeEnum.SOLID_FOREGROUND)
@HeadFontStyle(fontName="微软雅黑",fontHeightInPoints = 12)
@ContentFontStyle(fontName = "微软雅黑",fontHeightInPoints = 12)
@ColumnWidth(12)
public class UserStatisticsVo {
    @ExcelProperty(value = "用户ID",index = 0)
    private String userId;

    @ExcelProperty(value = "用户名称",index = 1)
    private String userName="微信用户";

    @ColumnWidth(20)
    @ExcelProperty(value = "登录时间",index = 2)
    private String createTime;

//    @ExcelProperty(value = "是否是活跃用户",index = 3)
//    private String isActive="是";

    @ExcelProperty(value = "时间",index = 6)
    private String time;

    @ExcelProperty(value = "活跃用户数",index = 7)
    private Integer activeUserCount;

    @ExcelProperty(value = "用户总数",index = 10)
    private Integer totalUserCount;

    @ExcelProperty(value = "新增用户数",index = 11)
    private Integer newUserCount;

    @ExcelProperty(value = "访问总次数",index = 12)
    private Integer totalVisitCount;

    @ExcelProperty(value = "活跃用户总数",index = 13)
    private Integer totalActiveUserCount;
}
