package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class BsLocMode extends Struct {



       public      Unsigned8  mode=new Unsigned8();
    public     Unsigned16 superFrame_interval=new Unsigned16();
    public     Unsigned16 slot_duration=new Unsigned16();
    public      Float32 bsrssi=new Float32();
    public     Float32 bsrange=new Float32();
    public     Unsigned8  year=new Unsigned8();
    public      Unsigned8  month=new Unsigned8();
    public     Unsigned8  day=new Unsigned8();
    public      Unsigned8  hour=new Unsigned8();
    public       Unsigned8  minute=new Unsigned8();
    public       Unsigned8  second=new Unsigned8();
    public       Unsigned16  milisecond=new Unsigned16();

    public BsLocMode(){

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
