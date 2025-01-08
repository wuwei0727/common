package com.tgy.rtls.data.entity.location;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.location
 * @date 2020/10/23
 * 定位数据
 */
@Data
@ToString
public class TdoaData implements Serializable {
    private Integer id;
    private String tagid;//区域
    private Date time;
    private String src;
    private String target;
    private String src_tx;
    private String target_rx;
    private String src_t;
    private String target_t;
    private Float diff;
    private float x,y;
}
