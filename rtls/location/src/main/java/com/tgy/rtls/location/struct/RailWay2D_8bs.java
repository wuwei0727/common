package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class RailWay2D_8bs extends Struct {
    public Unsigned16 year=new Unsigned16();//year
    public  Unsigned8  month = new Unsigned8(); //
    public  Unsigned8  day = new Unsigned8(); //
    public  Unsigned8  hour = new Unsigned8(); //
    public  Unsigned8  minute= new Unsigned8(); //
    public  Unsigned8  second = new Unsigned8(); //
    public  Unsigned16  milisecond = new Unsigned16(); //

    public  Unsigned8  type = new Unsigned8(); //



    public  Unsigned32 src = new Unsigned32(); //
    public  Unsigned32 dst = new Unsigned32(); //
    public  Unsigned32 rangeid = new Unsigned32(); //


    public  Range2D_8bs[] ts_master=new Range2D_8bs[8];
    public  Range2D_8bs[] ts_slave=new Range2D_8bs[8];
    public Unsigned16 crc=new Unsigned16();


    public RailWay2D_8bs(){
        for(int i=0;i<8;i++){
            ts_master[i]=new Range2D_8bs();
            ts_slave[i]=new Range2D_8bs();
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
