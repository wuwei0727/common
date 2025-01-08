package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class Range2D extends Struct {


    public  Signed64 group_poll_rx=new Signed64();
    public  Signed64 group_resp_tx=new Signed64();
    public  Signed64 group_resp_rx=new Signed64();

    public Range2D(){

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
