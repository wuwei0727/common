package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class UwbRawInfAll extends Struct {

    public  Unsigned8		isTx=new Unsigned8();				//0 : 接收，1：发送
 public Unsigned8     type=new Unsigned8();  //1
 public Unsigned32    src=new Unsigned32();//4
 public   Unsigned32    dst=new Unsigned32();//4

 public Unsigned8     mode=new Unsigned8();//1
 //工作模式：
 //0:默认模式；保留

 public Unsigned16     superframe_interval=new Unsigned16();//2
 //超帧（即0x64帧）的发送周期,单位毫秒
 //如果superframe_interval等于0，则标签无需按照时隙工作，即标签按照最初的随机方式发送

 public Unsigned16     slot_duration=new Unsigned16();//2
 //每隔时隙的长度，单位ms, 时隙数量=superframe_interval/slot_duration;

 public Float32       bss_rssi_limit=new Float32();//4
 //基站可以接受的信号质量范围，标签应该保存这个值；
 //如果标签收到基站所发出的帧（见下文）信号强度低于这个值，则标签应该切换到广播测距模式，这些帧包括：
 //【0x62 测距应答帧】，【0x64 授时同步帧】
 //   这个值等于0时，则表示标签无需判断信号强度。

 public Float32       bss_range_limit=new Float32();//4
 //对于已经入网的标签，当标签端计算出来的距离大于这个值时，标签应该转换为广播测距模式。
 //   这个值等于0时，则表示标签无需判断距离或标签自行判断距离。

 public Unsigned8     year=new Unsigned8();   //2000+year,2020-->20
 public Unsigned8     mon=new Unsigned8();    //月1-12
 public Unsigned8     date=new Unsigned8();    //日1-31

 public Unsigned8     hour=new Unsigned8();   //时
 public Unsigned8     minute=new Unsigned8(); //分
 public Unsigned8     second=new Unsigned8(); //秒

 public Unsigned16    ms=new Unsigned16();     //毫秒

 public Unsigned16	crc=new Unsigned16();
 public Unsigned8[]	reserver=array(new Unsigned8[96]);
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

    public UwbRawInfAll(){
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
