package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class UwbRawInf extends Struct {


    public  Unsigned8		isTx=new Unsigned8();				//0 : 接收，1：发送
   public  Unsigned8[] 	uwb_data=array(new Unsigned8[128]);
    //public  UwbFrameTiming 	frameTiming=new UwbFrameTiming();
    public  Unsigned16 	uwb_data_len=new Unsigned16();

    public  Unsigned8 	has_diag=new Unsigned8();			//0 没有，1 有诊断信息
    //public  UwbDiagInf 	diag_info=new UwbDiagInf();
    public  Unsigned8[] 	diag_info=array(new Unsigned8[94]);


    public  Unsigned16		year=new Unsigned16();
    public   Unsigned8 		month=new Unsigned8();
    public  Unsigned8 		day=new Unsigned8();

    public  Unsigned8 		hour=new Unsigned8();
    public  Unsigned8 		min=new Unsigned8();
    public   Unsigned8 		sec=new Unsigned8();
    public Unsigned16		ms=new Unsigned16();

    public Signed64		timestamp=new Signed64();
    public Unsigned32 		uwb_error=new Unsigned32();

    public UwbRawInf(){
      //  frameTiming=new UwbFrameTiming();
      //  diag_info=new UwbDiagInf();
     /*  for(int i=0;i<128;i++){
           uwb_data[i]=new Unsigned8();
        }*/
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
