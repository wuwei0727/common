package com.tgy.rtls.location.config.deviceconfig;

import com.tgy.rtls.location.struct.TagFirmware;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


public interface TagParaConfig {




    public boolean setTagId(Long bsid,long tagid,long newId);

    public boolean setTagBeep(Long bsid,long tagid,int beepState,int period);

    public   boolean setTagLocaPara(Long bsid,long tagid,int loc_inval,int rx_inval);

    public boolean setTagPower(Long bsid,long tagid,byte pa,byte gain);

    public boolean setTagMode(Long bsid,long tagid,byte mode,byte period);

    public  boolean setTagReboot(Long bsid,long tagid,byte reboot);

    public   boolean setTagLowPowerMode(Long bsid,long tagid,int state);

    public   boolean setTagSensorPeriod(Long bsid,long tagid,int period);

    public   boolean setTagMoveLevel(Long bsid,long tagid,int moveLevel);

    public   boolean setTagHeartPeriod(Long bsid,long tagid,int period);

    public boolean setTagWarningText(Long bsid,long tagid,int fileId,String text,short level,long time) throws UnsupportedEncodingException;

    public boolean setTagCommonText(Long bsid,long tagid,int fileId,String text,long time) throws UnsupportedEncodingException;

    public boolean setTagPosition(Long bsid,long tagid,int messageid,byte state,String detail,long time) throws UnsupportedEncodingException;

    public Boolean getTagVersionInf(long bsid, long tagid);

    public Boolean setTagTimestamp(long bsid, long tagid,Long time) ;

    public Boolean setTagGroupBslist(long bsid, long tagid,byte type,String bsids) ;

    public Boolean setTagGroupPeriod(long bsid, long tagid,byte type,Integer time) ;

    public Boolean processTagUpdate(Long bsid,Long tagid,TagFirmware tagFirmware,String fileUrl,String fileName,Integer pkgLen,String version) throws IOException;

    public void sendOtaData(long bsid,int tagid,byte[] firmwareData,long pkgid,long pkgsize);

    public void processTagUpdateFinish(long bsid,int tagid,byte res) throws IOException;

    public long tagUpdateCheck(String fileName,long oldVersion);

    byte[] tagUpdateData(byte[] data,long pkgid,long pkgsize) ;

    public long  getBsid(Long bsid,long target);


}
