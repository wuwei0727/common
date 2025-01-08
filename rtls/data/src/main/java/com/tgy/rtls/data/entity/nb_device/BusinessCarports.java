package com.tgy.rtls.data.entity.nb_device;

import lombok.Data;

@Data
public class BusinessCarports {

    private String type;
    private String    time;//事件时间戳
    private String    space;//-- 平台内部定义的设备接入类型，space+device_id 确定单个设备
    private String    device_id;//-- 一般为 IMEI
    private BusinessCarportsData data;

}
