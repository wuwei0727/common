package com.tgy.rtls.data.entity.location;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class Recovery_data implements Serializable {
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String timestamp;
    private Long tagid;//标签ID
    private Long rangid;//测距id
    private Long bsid;//基站ID
    private BigDecimal rx3;//本基站收到tag的第二个帧（结果帧）的时间戳
    private BigDecimal ft1;//标签发出的时间戳
    private BigDecimal ft2;//基站收到的数据戳
    private BigDecimal ft3;//发送的时间戳
    private BigDecimal ft4;//基站的时间戳
    private Float rssi;
    private Float fp;
    private Long lr;
}
