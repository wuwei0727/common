package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class Range2D_8bs extends Struct {


    public  Unsigned32 bss=new Unsigned32();
    public  Float64 tof=new Float64();
    public  Float32 los_poll=new Float32();
    public  Float32 los_resp=new Float32();



    public Range2D_8bs(){

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
