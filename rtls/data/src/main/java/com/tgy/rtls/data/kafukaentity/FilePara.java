package com.tgy.rtls.data.kafukaentity;

import lombok.Data;
import net.sf.json.JSONObject;

/**
 * 音频数据,固件数据
 */
@Data
public class FilePara {

    private short fileType; // 0 固件   1 音频
    private long time;//utc 时间
    private long instanceid;//实例id
    private int messageid;//当前消息id，需要由redis或者雪花算法生成唯一id
    private Long bsid;// 基站id，当基站-1时，由location模块根据当前标签所在区域灵活选择基站；否则根据输入bsid进行下发
    private Long target;// -1 代表广播所有标签（标签固件升级时使用）；0代表该文件发送给当前基站（基站固件升级时使用）；其他值时则下发给特定标签 （注意：当target为0或者-1时bsid不能为空)
    private short state; //-1: 发送/接收中  0：发送/接收成功  1：消息已读  2：发送/接收失败
    private String fileName;//文件名
    private short direction;//方向0： 上行  1：下行
    private String url;//完整的文件url
    private byte bssid_h;//识别码高位
    private byte bssid_m;//识别码低位
    private byte tagsid_h;//识别码高位
    private byte tagid_m;//识别码低位



    private short process; //进度


    /**
     * 注意在下发文件时，
     * bsid 和target组合有几种使用场景
     *    bsid ：-1   target  为特定标签值时 ，适合定向下发，优先找标签所在区域基站发送，当标签位置未知时，则调用所有基站下发
     *    bsid ：-1   target  为0时 ，适合定向下发基站升级固件
     *    bsid ：特定基站编号时   target  为-1时 ，适合向某个基站附近的标签下发广播信息
     *    bsid ：-1   target  为-1时 ，适合向当前区域，全部标签发送广播信息，例如紧急撤退语音等
     */

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
