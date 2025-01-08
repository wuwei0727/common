package com.tgy.rtls.location.config.deviceconfig;

import com.tgy.rtls.data.kafukaentity.BsPara;
import com.tgy.rtls.location.struct.UwbRawInfAll;

import java.nio.ByteBuffer;
import java.text.ParseException;


public interface BsParaConfig {

    public boolean sendHeartData(Long bsid);

    public boolean setBsCompanyText(Long bsid,String text);

    public void getBsUpdate(Long bsid,ByteBuffer data);

    public boolean getBsVersionInf(Long bsid);

    public boolean setBsNetInf(Long bsid,String ip,String netmask,String network,String gateway,int ip_type);

    public boolean setBsLocationText(Long bsid,String text);

    public boolean setBsCompanyImg(Long bsid,String url,String fileName);

    public boolean setBsPower(Long bsid,int gain,int  rw);

    public boolean setBsGeneral(Long bsid,int gegeral);

    public boolean setBsBeep(Long bsid,int beepState,int  period);

    public boolean setTagModeViaBs(Long bsid,Byte mode,short superFrame_interval,short slot_duration,float bsrssi,float  bsrange);

    public boolean setBsWarning(Long bsid,BsPara warning);

    public boolean sendBsFile(Integer instanceId,Long bsid,Integer target,byte fileType,int fileId,String fileUrl,String fileName);

    public void setCoalBs_TagDis(Long bsid,String tagid,float dis,byte lr,float rssi,float rssiFp,byte flag,String name,byte move,float volt,Long rangeBs);

    public boolean startBsRange(Long bsid,int rangeId,int type, Long targetId);

    public void setBsOldTimeAndDis(Long bsid ,float  dis,short time);

    public void getBsNetConfigRes(Long bsid ,byte res);

    public void getBsTagDisRes(Long bsid , ByteBuffer buffer);


    public UwbRawInfAll getBsBeacon(Long bsid , ByteBuffer buffer) throws ParseException;

    public void sendScreenCache(long bsid);

    public void getBsVersion(long bsid, String ver);

    public void setRandomKey(Long bsid , BsPara bsPara);

    public long  getBsid(Long target);

    public long  processBeacon(Long bsid, UwbRawInfAll uwbRawInfAll) throws ParseException;

    public   long processTagPoll(Long bsid, byte[] data1);

    public   long storageRawData(Long bsid,UwbRawInfAll uwbRawInfAll, byte[] data1);

    /**
     * 对车位检测设备返回数据响应
     * @param bsid
     * @param data
     */
  public void  processInfraredPark(Long bsid,ByteBuffer data,byte[] cmd,byte[] msgid,long len);

    /**
     * 对433Mhz网关心跳数据
     * @param bsid
     * @param data
     */
    public void  processGateway_lora(Long bsid,ByteBuffer data,byte[] cmd,byte[] msgid);

    public void  processCat1(Long bsid,ByteBuffer data,byte[] cmd,byte[] msgid);



    /**
     * 网关版本
     * @param bsid
     * @param data
     */
    public void  processGateway_lora_version(Long bsid,byte[] data,byte[] cmd,byte[] msgid);



    /**
     * 车位检测设备固件版本
     * @param bsid
     * @param data
     */
    public void  processInfrared_version(Long bsid,byte[]  data,ByteBuffer buffer,byte[] cmd,byte[] msgid);




    /**
     * 配置红外车位检测器参数
     * @param bsid
     */
    public Boolean  processInfrared_para(Long bsid,Long parkid,byte check_interval,byte heart_interval );



    /**
     * 配置网关参数
     * @param bsid
     */
    public Boolean  processGatewayLora_para(Long bsid,String doamin,String port);




    /**
     * LOra网关升级
     * @param bsid
     */
    public Boolean  processGatewayLora_update(Long bsid,String url);

    /**
     * 红外升级
     * @param bsid
     */
    public Boolean  processInfrared_update(Long bsid,Long parkid,String url);



    /**
     * 记录车位检测器状态码
     * @param bsid
     */
    public Boolean  processInfrared_state(Long bsid,ByteBuffer buffer);


    /**
     * 记录网关状态码
     * @param bsid
     */
    public Boolean  processGate_state(Long bsid,ByteBuffer buffer);



    /**
     * 控制网关重启
     * @param bsid
     */

    public Boolean  processGate_reboot(Long bsid,byte[] msgid);


    /**
     * 收集地锁数据
     * @param bsid
     */
    public Boolean  processNed_data(Long bsid,ByteBuffer buffer,byte[] msgid);


    /**
     * LOra网关升级
     * @param bsid
     */
    public Boolean  processCat1_cmd(Long bsid,int  cmd,int data);



}
