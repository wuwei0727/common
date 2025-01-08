package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class Beacon extends Struct {



    public  Unsigned32 beacon_src = new Unsigned32(); //
    public  Signed64 beacon_id = new Signed64(); //
    public  Signed64 beacon_txTs=new Signed64();
    public  Signed64 beacon_rxTs=new Signed64();




    public Float32 beacon_rssi=new Float32();
    public Float32 beacon_fp=new Float32();


    public Beacon(){

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
