package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
public class TextPara {
    private String text;//下发内容
    private long instanceid;//实例id
    private int messageid;//当前消息id，需要由redis或者雪花算法生成唯一id
    private Short state; //-1: 发送/接收中  0：发送/接收成功  1：消息已读  2：发送/接收失败
    private Short type;  // 1:普通文本     2.标签位置信息 3:告警通知
    private Short level;// 当type为2时，0：井上  1：井下 2:其他 当type为3时 0:一般紧急消息    1：特殊紧急消息；
    private Long bsid;// 基站id，当基站-1时，由location模块根据当前标签所在区域灵活选择基站；否则根据输入bsid进行下发
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    private byte tagsid_h;//识别码高位
    private byte tagsid_m;//识别码低位
    private Long target;//
    private Long time;

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
