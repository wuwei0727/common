package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class TagFirmware extends Struct {

 public    Unsigned8 update_ask=new Unsigned8();
    public    Unsigned16  Hardware_rev=new Unsigned16();
    public   Unsigned8    Pre_firmware_state=new Unsigned8();
    public   Unsigned16 Pre_firmware_rev=new Unsigned16();
    public   Unsigned32 size =new Unsigned32();










    public TagFirmware(){

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
