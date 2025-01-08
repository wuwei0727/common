package com.tgy.rtls.location.netty;


import com.tgy.rtls.data.kafukaentity.TagPara;
import com.tgy.rtls.location.check.BsUpdateCheck;
import com.tgy.rtls.location.check.ErrorCodeInf;
import com.tgy.rtls.location.model.*;
import io.netty.channel.Channel;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MapContainer {
    public volatile boolean flag=true;
    public  ConcurrentHashMap<Long , BsCheck> bsCheck=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String, byte[]> tagFirmWare=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String, Boolean> tagCapacities=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String , BsInf> bsInf=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<String , TagInf> tagInf=new ConcurrentHashMap<>();
  //  public   DefaultChannelGroup all_channels = new DefaultChannelGroup("NADRON-CHANNELS", GlobalEventExecutor.INSTANCE);
    public ConcurrentHashMap<String, Channel> all_channel = new ConcurrentHashMap<>();

    public  int FRAME_ID = 0;
    public ReentrantLock lock=new ReentrantLock();
    public  ConcurrentHashMap<Long , Ned> device_Ned=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<Long , Screen> device_Screen=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<Long , TagPara> currentUpdateTag=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<Long , TagPara> waitUpdateTag=new ConcurrentHashMap<>();
    public  ConcurrentHashMap<Long , Timestamp> lora_finalTIme=new ConcurrentHashMap<>();
    public DelayQueue<BsUpdateCheck> messageQueue = new DelayQueue<BsUpdateCheck>(); //基站升级延时


    @Value("${location.dis_cachelen_highfreq}")
    public   int discachelen_highfreq;
    @Value("${location.timedelay_highfreq}")
    public   int timedelay_highfreq;
    @Value("${location.location_cachelen_highfreq}")
    public   int locationcachelen_highfreq;

    @Value("${location.dis_cachelen_lowfreq}")
    public   int discachelen_lowfreq;
    @Value("${location.timedelay_lowfreq}")
    public   int timedelay_lowfreq;
    @Value("${location.location_cachelen_lowfreq}")
    public   int locationcachelen_lowfreq;
    @Value("${location.two_module_dis1D}")
    public   float two_module_dis1D;
    @Value("${location.two_module_dis2D}")
    public   float two_module_dis2D;
    @Value("${location.bsnum}")
    public   int location_bsnum;
    @Value("${location.strict}")
    public   int location_strictmode;

    @Value("${ztt}")
    public  boolean testFlag=false;
    @Value("${location.channeldis}")
    public  double channeldis;
    @Value("${location.channelpercent}")
    public  double channelpercent;

    @Value("${location.blesingledis}")
    public  double blesingledis;
    @Value("${location.ble1d}")
    public  double ble1d;

    @Value("${location.bleover}")
    public  double bleover;



    public  volatile  float d1=-1f;

    public  volatile  float d2=-1f;







}
