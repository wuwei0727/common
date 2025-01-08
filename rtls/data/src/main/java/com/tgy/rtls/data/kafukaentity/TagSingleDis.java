package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;

/**
 * 标签定位数据
 */
@Data
public class TagSingleDis {

    long tagid;//标签编号
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    long  bsid;//区域信息，或者定位基站
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    short type;// 0: 煤矿  1：平面定位  2：蓝牙
    long time;//utc t 定位时间
    short lr ;//lr 0 左 1 右
    float dis;//距离

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
