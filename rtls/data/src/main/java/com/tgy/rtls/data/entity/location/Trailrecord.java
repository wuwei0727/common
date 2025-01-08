package com.tgy.rtls.data.entity.location;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Trailrecord implements Serializable {
    private Integer id;
    private String area;//区域
    private String name;//名称
    private String num;//工号
    private Integer personid;//人员id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private Double x;
    private Double y;
    private Double z;
    private Integer type;//类型 0煤炭定位 1GPS定位 2其他
    private Integer mapid;//地图id
    private Double r;//精度值
    private String debugData;
    private Short floor;

}
