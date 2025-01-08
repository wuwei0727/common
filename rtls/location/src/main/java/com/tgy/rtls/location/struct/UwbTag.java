package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class UwbTag extends Struct {
    public  Unsigned8		isTx=new Unsigned8();				//0 : 接收，1：发送
 public Unsigned8     type=new Unsigned8();  //1
 public Unsigned32    tagid=new Unsigned32();//4
 public   Unsigned32    dst=new Unsigned32();//4

 public Unsigned32     seq=new Unsigned32();//1

 public Float32     vbat=new Float32();//2

    public   Unsigned8 	status=new Unsigned8();//标签状态数据
/*    public   Unsigned8 	reserved=new Unsigned8();//:1;
    public Unsigned8	pg=new Unsigned8();//:1;					//是否连接充电线
    public Unsigned8	charge=new Unsigned8();//:1;					//是否正在为电池充电
    public Unsigned8	is_active=new Unsigned8();//:1;				//是否判定为活动状态
    public Unsigned8	is_stationary=new Unsigned8();//:1;			//是否判定处于完全静止状态

    public Unsigned8 	has_sos=new Unsigned8();//: 1;				//是否处于SOS 求救状态状态
    public Unsigned8	has_motion_sensor=new Unsigned8();//:1;		//G-Sensor是否正常
    public Unsigned8	has_motion=new Unsigned8();//: 1;			//是否检测到motion*/


	/*
	G-Sensor是否检测到活动.
	与is_active的区别:
	即使G-sensor没有检测到运动，is_active也会持续[15]秒之后才转换为0(inactive)
	*/


 public Unsigned8    r=new Unsigned8();     //毫秒
 public Float32 temp=new Float32();				//温度（摄氏度）
     public Float32    pressure=new Float32();				//气压（pa）
    public Unsigned32 steps=new Unsigned32();//步数

 public Unsigned16	crc=new Unsigned16();
 public Unsigned8[]	reserver=array(new Unsigned8[95]);
    public  Unsigned16 	uwb_data_len=new Unsigned16();

    public  Unsigned8 	has_diag=new Unsigned8();			//0 没有，1 有诊断信息
 public   Unsigned16     maxNoise=new Unsigned16() ;          // LDE max value of noise
 public    Unsigned16     firstPathAmp1=new Unsigned16() ;    // Amplitude at floor(index FP) + 1
 public    Unsigned16     stdNoise=new Unsigned16() ;         // Standard deviation of noise
 public    Unsigned16     firstPathAmp2=new Unsigned16() ;    // Amplitude at floor(index FP) + 2
 public    Unsigned16     firstPathAmp3=new Unsigned16() ;    // Amplitude at floor(index FP) + 3
 public    Unsigned16     maxGrowthCIR=new Unsigned16() ;   	// Channel Impulse Response max growth CIR
 public    Unsigned16     rxPreamCount=new Unsigned16() ;    // Count of preamble symbols accumulated
 public    Unsigned16     firstPath=new Unsigned16() ;      	// First path index (10.6 bits fixed point integer)
 public    Unsigned32     pacc=new Unsigned32();
 public    Unsigned32 	   carrier=new Unsigned32();
 public    Unsigned8      ntm1=new Unsigned8();
 public    Unsigned16     peak_path_index=new Unsigned16();
 public    Unsigned16     peak_path_amp=new Unsigned16();
 public    Unsigned8[]    accum_data=array(new Unsigned8[65]);


    public  Unsigned16		year1=new Unsigned16();
    public   Unsigned8 		month1=new Unsigned8();
    public  Unsigned8 		day=new Unsigned8();

    public  Unsigned8 		hour1=new Unsigned8();
    public  Unsigned8 		min=new Unsigned8();
    public   Unsigned8 		sec=new Unsigned8();
    public Unsigned16		ms1=new Unsigned16();

    public Signed64		timestamp=new Signed64();
    public Unsigned32 		uwb_error=new Unsigned32();

    public UwbTag(){
      //  frameTiming=new UwbFrameTiming();
      //  diag_info=new UwbDiagInf();
     /* for(int i=0;i<96;i++){
           reserver[i]=new Unsigned8();
        }
     for(int i=0;i<65;i++){
      accum_data[i]=new Unsigned8();
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
