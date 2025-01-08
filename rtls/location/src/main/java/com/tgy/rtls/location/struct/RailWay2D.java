package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class RailWay2D extends Struct {

    public  Unsigned32 tagId = new Unsigned32(); //
    public  Unsigned32 rangeid = new Unsigned32(); //

    public  Unsigned32[]  bss = new Unsigned32[8]; //


    public  Signed64 group_poll_tx=new Signed64();
    public  Signed64 group_final_tx=new Signed64();
    public  Range2D[] ts=new Range2D[8];

    public Float32 cl=new Float32();//置信度
    public Unsigned16 year=new Unsigned16();//year
    public  Unsigned8  month = new Unsigned8(); //
    public  Unsigned8  day = new Unsigned8(); //
    public  Unsigned8  hour = new Unsigned8(); //
    public  Unsigned8  minute= new Unsigned8(); //
    public  Unsigned8  second = new Unsigned8(); //
    public  Unsigned16  milisecond = new Unsigned16(); //
    public RailWay2D(){
        for(int i=0;i<8;i++){
            ts[i]=new Range2D();
            bss[i]=new Unsigned32();
        }
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
