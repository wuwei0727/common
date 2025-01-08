package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class BsPower extends Struct {



    public  Unsigned32 pkgId = new Unsigned32();
    public Unsigned8 charge_state=new Unsigned8();// 0 未充电   1：充电
    public Float32 charge_volt=new Float32();// 充电电压
    public Float32 battery_volt=new Float32();//电池电压

    public BsPower(){

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
