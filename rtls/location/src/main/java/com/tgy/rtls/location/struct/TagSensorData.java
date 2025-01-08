package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class TagSensorData extends Struct {


    public Unsigned32 time=new Unsigned32();//采集时间
    public  Float32 volt = new Float32(); // 当前尚未采集的记录数量
    public  Float32 temper = new Float32(); // 当前尚未采集的记录数量
    public  Unsigned8 alarm = new Unsigned8(); // 当前尚未采集的记录数量
    public  Unsigned8 move = new Unsigned8(); // 当前尚未采集的记录数量
    public  Unsigned8 sos = new Unsigned8(); // 当前尚未采集的记录数量
    public  Unsigned16 hr_num = new Unsigned16(); // 当前尚未采集的记录数量



    public TagSensorData(){

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
