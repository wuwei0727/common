package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class TagSensorHead extends Struct {



    public  Unsigned32 tagId = new Unsigned32(); // 当前尚未采集的记录数量
    public  Unsigned32 rangeId = new Unsigned32(); // 当前尚未采集的记录数量
    public  Signed64 rxTimestamp = new Signed64(); // 当前尚未采集的记录数量

    public Float32 rssi=new Float32();
    public Float32 rssifp=new Float32();
    public  Unsigned16 sensorLen = new Unsigned16(); // 当前尚未采集的记录数量
    public Unsigned8 cmd=new Unsigned8();

    public TagSensorHead(){

    }


    // 一定要加上这个，不然会出现对齐的问题
    @Override
    public boolean isPacked() {
        return true;
    }

    // 设置为小端数据
    @Override
    public ByteOrder byteOrder() {
        return ByteOrder.LITTLE_ENDIAN;
    }
}
