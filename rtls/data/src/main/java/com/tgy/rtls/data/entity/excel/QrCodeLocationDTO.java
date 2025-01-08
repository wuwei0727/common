package com.tgy.rtls.data.entity.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.tgy.rtls.data.config.ImageUrlToExcelImageConverter;
import lombok.Data;

@Data
public class QrCodeLocationDTO {

    @ExcelProperty(value = "地图")
    private String mapName;
    @ExcelProperty(value = "区域名称")
    private String areaName;
    @ExcelProperty(value = "楼层")
    private String floorName;
    @ExcelProperty(value = "二维码",converter = ImageUrlToExcelImageConverter.class)
    private String qrCodeUrl;
}