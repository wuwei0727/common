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
public class Beacondata implements Serializable {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;
    private String bsid;//上传基站id
    private String beacon_src;//同步源基站
    private String beacon_id;//同步序号
    private String beacon_txTs;//源基站发送时间戳
    private String beacon_rxTs;//基站接收的时间戳
    private String beacon_rssi;//rssi
    private String beacon_fp;//信号强度
    private float x,y;


}
