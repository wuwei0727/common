package com.tgy.rtls.data.config;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class CustomConverter implements Converter<String> {

    @Override
    public Class<String> supportJavaTypeKey() {
        return String.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(String value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if ("devicesType".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "1":
                    return new WriteCellData<>("信标");
                case "2":
                    return new WriteCellData<>("网关");
                case "3":
                    return new WriteCellData<>("检测器");
                case "7":
                    return new WriteCellData<>("道钉");
                default:
                    return new WriteCellData<>("未知");
            }
        } else if ("devicesStatus".equals(contentProperty.getField().getName())||"devicesStatus".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("离线");
                case "1":
                    return new WriteCellData<>("在线");
//                case "2":
//                    return new WriteCellData<>("低电量");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("parkingType".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("普通车位");
                case "1":
                    return new WriteCellData<>("充电车位");
                case "2":
                    return new WriteCellData<>("专属车位");
                case "3":
                    return new WriteCellData<>("无障碍车位");
                case "4":
                    return new WriteCellData<>("超宽车位");
                case "5":
                    return new WriteCellData<>("字母车位");
                case "6":
                    return new WriteCellData<>("小型车位");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("parkingStatus".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("空闲");
                case "1":
                    return new WriteCellData<>("占用");
                case "2":
                    return new WriteCellData<>("已预约");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("vip".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("否");
                case "1":
                    return new WriteCellData<>("是");
                case "2":
                    return new WriteCellData<>("已预约");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("source".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "1":
                    return new WriteCellData<>("小程序");
                case "2":
                    return new WriteCellData<>("后台管理系统");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("status".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("已失效");
                case "1":
                    return new WriteCellData<>("已生效");
                case "2":
                    return new WriteCellData<>("未生效");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("configurationMethod".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "1":
                    return new WriteCellData<>("超声方式");
                case "2":
                    return new WriteCellData<>("视频方式");
                case "3":
                    return new WriteCellData<>("超声加视频方式");
                case "0":
                    return new WriteCellData<>("无");
                default:
                    return new WriteCellData<>("未知");
            }
        }else if ("chargingStatus".equals(contentProperty.getField().getName())) {
            switch (value) {
                case "0":
                    return new WriteCellData<>("未充电");
                case "1":
                    return new WriteCellData<>("充电中");
                case "3":
                    return new WriteCellData<>("无");
                default:
                    return new WriteCellData<>("未知");
            }
        }
        return new WriteCellData<>("未知");
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if ("type".equals(contentProperty.getField().getName())) {
            switch (cellData.getStringValue()) {
                case "信标":
                    return "1";
                case "网关":
                    return "2";
                case "检测器":
                    return "3";
                default:
                    return null;
            }
        } else if ("status".equals(contentProperty.getField().getName())) {
            switch (cellData.getStringValue()) {
                case "离线":
                    return "0";
                case "在线":
                    return "1";
                default:
                    return null;
            }
        }
        return null;
    }

}