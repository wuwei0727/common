package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class TagCurrentInf extends Struct {

     public  Unsigned8 boot_ver_h = new Unsigned8(); // BOOT 版本
    public  Unsigned8 boot_ver_m = new Unsigned8(); // BOOT 版本
    public  Unsigned8 boot_ver_l = new Unsigned8(); // BOOT 版本
    public  Unsigned8 fm_ver_h = new Unsigned8(); // 固件版本
    public  Unsigned8 fm_ver_m = new Unsigned8(); // 固件版本
    public  Unsigned8 fm_ver_l = new Unsigned8(); // 固件版本
    public  Unsigned8 hd_ver_h = new Unsigned8(); // 固件版本
    public  Unsigned8 hd_ver_m = new Unsigned8(); // 固件版本
    public  Unsigned8 hd_ver_l = new Unsigned8(); // 固件版本
    public  Unsigned32 loc_period = new Unsigned32(); // 定位周期
    public  Unsigned8 gain = new Unsigned8(); // 增益
    public  Unsigned16 move_level = new Unsigned16(); // 运动检测阈值 mg
    public  Unsigned32 heart_on_ms = new Unsigned32(); // 心率模块开启时间
    public  Unsigned32 heart_off_ms = new Unsigned32(); //心率模块关闭时间





    public TagCurrentInf(){

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
