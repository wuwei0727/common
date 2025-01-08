package com.tgy.rtls.data.config;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;
import com.tgy.rtls.data.common.NullUtils;
import org.apache.poi.util.IOUtils;

import java.net.URL;

public class ImageUrlToExcelImageConverter implements Converter<String> {
    private final String FDFSURL="http://192.168.1.95:7003/";
    @Override
    public Class<?> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }


    @Override
    public WriteCellData<?> convertToExcelData(String url, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        try {
            if (NullUtils.isEmpty(url)){
                return new WriteCellData<>("图片链接为空");
            }


            URL imageUrl = new URL(FDFSURL+url);
            byte[] imageBytes = IOUtils.toByteArray(imageUrl.openStream());
            return new WriteCellData<>(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return new WriteCellData<>("图片获取异常："+url); // 如果转换失败则返回URL字符串
        }
    }
}