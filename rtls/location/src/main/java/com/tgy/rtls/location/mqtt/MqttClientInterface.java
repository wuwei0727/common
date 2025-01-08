package com.tgy.rtls.location.mqtt;

import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.kafukaentity.BsState;
import com.tgy.rtls.data.kafukaentity.TagLocation;
import com.tgy.rtls.data.kafukaentity.TagSensor;

public interface MqttClientInterface {
    void startReconnect();//mqtt重连
    void  start(Integer id);//创建连接
    void disconnect();//断开连接
    void publishLocationData(String tagid, TagLocation tagLocation);//发布定位数据
    void publishSensorData(String tagid, TagSensor tagSensor);//发布标签状态数据
    void publishBsLocation(Basestation base);//修改基站坐标
    void publishBsStateData(String bsid, BsState bsState);//发布基站状态数据
}
