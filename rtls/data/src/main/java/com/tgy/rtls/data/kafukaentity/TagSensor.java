package com.tgy.rtls.data.kafukaentity;

import com.tgy.rtls.data.common.Descrip;
import lombok.Data;
import net.sf.json.JSONObject;
@Data
public class TagSensor {
    @Descrip(value ="标签编号")
    private String tagid;
    @Descrip(value ="时间")
    private Long time;//UTC时间
    @Descrip(value ="电压")
    private	Float   power ;//电压值
    private  float temper;
    private  short	broken;
    @Descrip(value ="运动状态")
    private	short   moveState ;
    @Descrip(value ="sos")
    private	short   sos ;
    private short heart;
    @Descrip(value ="bsid")
    private String bsid;
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    @Descrip(value ="type")
    short type;// 0: 煤矿  1：平面定位  2：蓝牙
    @Descrip(value ="rssi")
    private short sub1g_rssi;
    private long steps;
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
