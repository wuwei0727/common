package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class BsRangeRes extends Struct {

    public   Unsigned32	range_id= new Unsigned32();			//测距消息ID
    public  Unsigned32	target_type= new Unsigned32();		//0:基站，1：标签
    public  Unsigned32	target_id= new Unsigned32();			//测距目标设备ID

    public Unsigned32	error_code= new Unsigned32();		//0:成功，其他：失败（如超时等）

    public  Signed64	host_tx=new Signed64();
    public Signed64	client_rx=new Signed64();
    public Float32		client_rssi=new Float32();		//rssi强度
    public Float32		client_fp_pwr=new Float32();		//FirstPath强度

    public Signed64	client_tx=new Signed64();
    public Signed64	host_rx=new Signed64();
    public Float32		host_rssi=new Float32();			//rssi强度
    public Float32		host_fp_pwr=new Float32();		//FirstPath强度



    public BsRangeRes(){

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
