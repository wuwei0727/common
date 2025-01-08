package com.tgy.rtls.location.struct;


import javolution.io.Struct;

import java.nio.ByteOrder;


public class UwbDiagInf extends Struct {


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
    public    Unsigned8[]    accum_data=new Unsigned8[65];

    public UwbDiagInf(){
      for(int i=0;i<65;i++){
          accum_data[i]=new Unsigned8();
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
