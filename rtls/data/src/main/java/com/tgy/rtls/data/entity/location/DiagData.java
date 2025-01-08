package com.tgy.rtls.data.entity.location;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@ToString
public class DiagData implements Serializable {
    private Long id;
    private String upbsid;
    private Short isTx;//标签ID
    private int frametype ;//测距id
    private String uwbdata;//基站ID
    private Short hasdiag;//本基站收到tag的第二个帧（结果帧）的时间戳
    private String  diag;//标签发出的时间戳
    private BigDecimal timestamp;//发送的时间戳
    private long uwb_error;
    private float x;
    private float y;
}
