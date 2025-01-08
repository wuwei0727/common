package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;

/**
 * 标签配置数据
 */
@Data
public class TagPara {
    private String keyOrder;
    private long instanceid;//实例id
    private String type;// 0 读 1写
    private short powerLevel;// 功率等级
    private int beepInterval;// 蜂鸣器鸣叫间隔，单位：ms 0: 一直鸣叫
    private int messageid;//redis生成的唯一id
    private String tagid;
    private long bsid;
    private short beepState;//蜂鸣器状态
    private long newId;//标签新ID
    private short lowPowerMode;//低功耗模式 0：关闭 1：开启
    private int sensorInterval;//传感器上传周期 单位：ms
    private short moveLevel;//运动传感器阈值 单位：mg
    private int heartInterval;//心跳周期 单位：ms
    private short pa;// 0:关闭  1:打开
    private short reboot;
    private int loc_inval;  //定位周期 单位：ms
    private int rx_inval;//接收窗口时间 单位：ms
    private String firmwareUrl;
    private String firmwareVersion;
    private Integer pkglen;
    private short mode;
    private short period;
    private String hardware;
    private String bootloader;
    private String firmware;
    private Integer updatestate;
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    private long time;
    private String groupbslist;//组测距地址
    private int grouprangetime ;//组测距周期
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
