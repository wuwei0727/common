package com.tgy.rtls.data.entity.location;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.location
 * @date 2020/10/22
 * 原始数据
 */
@Data
@ToString
public class Originaldata implements Serializable {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;
    private String rangid;//测距id
    private String tagid;//标签id
    private String upbsid;//上传基站id
    private String rangebsid;//测距基站id
    private String rx3;//本基站收到tag的第二个帧（结果帧）的时间戳
    private String ft1c;//标签发出的时间戳
    private String ft2c;//基站收到的时间戳
    private String ft3c;//基站发送的时间戳
    private String ft4c;//标签接收基站的时间戳
    private String rssi;//信号强度
    private String rssifp;//首达径信号强度
    private String original_dis;//原始距离
    private String filter_dis;//修正后距离
    private String lr;//左右模块  0左 1右

    private float x,y;
}
