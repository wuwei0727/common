package com.tgy.rtls.data.entity.excel;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelDataVo {
    @ExcelProperty("SID")
    private String sid;
    
    @ExcelProperty("类型id")
    private String typeId="200401";
    
    @ExcelProperty("高度")
    private Double height;
    
    @ExcelProperty("名称")
    private String name;
    
    @ExcelProperty("英文名称")
    private String englishName;
    
    @ExcelProperty("x坐标")
    private Double xCoordinate;
    
    @ExcelProperty("y坐标")
    private Double yCoordinate;
    
    @ExcelProperty("面积")
    private Double area;

    @ExcelIgnore
    private String mapName;
    @ExcelIgnore
    private String fMapId;
}