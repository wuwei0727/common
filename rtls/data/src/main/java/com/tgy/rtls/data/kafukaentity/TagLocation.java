package com.tgy.rtls.data.kafukaentity;

import com.tgy.rtls.data.common.Descrip;
import lombok.Data;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 标签定位数据
 */
@Data
public class TagLocation {
    @Descrip(value ="标签编号")
    String tagid;//标签编号
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    @Descrip(value ="x")
    float x;
    @Descrip(value ="y")
    float y;
    @Descrip(value ="z")
    float z;
    @Descrip(value ="区域")
    String area="";//区域信息，或者定位基站
    @Descrip(value ="类型")
    short type;// 0: 煤矿  1：平面定位  2：蓝牙
    @Descrip(value ="时间")
    long time;//utc t 定位时间
    @Descrip(value ="基站编号")
    long bsid;//bsid
    float r;//精度因子
    @Descrip(value ="楼层")
    String floor;
    public JSONArray debugData;//调试信息

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
