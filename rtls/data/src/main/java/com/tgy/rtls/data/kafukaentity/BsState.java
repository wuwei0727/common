package com.tgy.rtls.data.kafukaentity;

import com.tgy.rtls.data.common.Descrip;
import lombok.Data;
import net.sf.json.JSONObject;
@Data
public class BsState {
    @Descrip(value ="基站类型")
    public long type=1;//1 煤炭基站  2 uwb基站   3.uwb网关  4.蓝牙网关
    @Descrip(value ="设备编号")
    private String bsid;//设备编号
    @Descrip(value ="错误码")
    public short errorCode;  // 1：供电状态   2：网络状态  3：CAN口状态  4：UWB状态
    @Descrip(value ="状态")
    public short state; // 0: 正常  1：异常
    public float chargeVolt;//充电电压
    public float batteryVolt;//电池电压
    @Descrip(value ="ip")
    public String ip="";
    @Descrip(value ="时间")
    public long time;
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
