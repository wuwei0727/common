package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class SingleBsRangeRes extends Struct {



    public  Unsigned32 tagId = new Unsigned32(); //
    public  Unsigned32 rangeid = new Unsigned32(); //
    public  Signed64 rx3=new Signed64();
   // public  Signed64 ft1=new Signed64();

    public  Unsigned32  bsid1 = new Unsigned32(); //
    public  Unsigned8  lr1 = new Unsigned8(); //
    public  Signed64 ft1_1=new Signed64();
    public  Signed64 ft2_1=new Signed64();
    public  Signed64 ft3_1=new Signed64();
    public  Signed64 ft4_1=new Signed64();

    public  Unsigned32  bsid2 = new Unsigned32(); // 当前尚未采集的记录数量
    public  Unsigned8  lr2 = new Unsigned8(); // 当前尚未采集的记录数量
    public  Signed64 ft1_2=new Signed64();
    public  Signed64 ft2_2=new Signed64();
    public  Signed64 ft3_2=new Signed64();
    public  Signed64 ft4_2=new Signed64();


    public Float32 rssi=new Float32();
    public Float32 rssifp=new Float32();
    public Float32 cl=new Float32();//置信度
    public Unsigned16 year=new Unsigned16();//year
    public  Unsigned8  month = new Unsigned8(); //
    public  Unsigned8  day = new Unsigned8(); //
    public  Unsigned8  hour = new Unsigned8(); //
    public  Unsigned8  minute= new Unsigned8(); //
    public  Unsigned8  second = new Unsigned8(); //
    public  Unsigned16  milisecond = new Unsigned16(); //
    public SingleBsRangeRes(){

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
