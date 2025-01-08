package com.tgy.rtls.data.common;

public class MqttTopic {
    public static final String TOPIC_DW1001 = "dwm/#";//订阅已dwm/node开头的主题
    public static final String TOPIC_SUB1G = "dwm/gateway_sub1g/#";//订阅已dwm/node开头的主题
    public static final String TOPIC_LOCATION = "tgy/taglocation/";//标签定位数据
    public static final String TOPIC_SENSOR_TAG = "tgy/tagsensor/";//标签传感器数据
    public static final String TOPIC_BSSTATE = "tgy/bsstate/";//基站设备状态数据
    public static final String TOPIC_LOCATION_HAOXIANG = "taglocation";//标签定位数据
    public static final String TOPIC_SENSOR_TAG_HAOXIANG = "tagsensor";//标签传感器数据
    public static final String TOPIC_BSSTATE_HAOXIANG = "bsstate";//基站设备状态数据

}
