package com.tgy.rtls.data.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.message
 * @date 2020/10/29
 * 报警记录
 */
@Data
@ToString
public class WarnRecord implements Serializable {
    private Integer id;
    private String personid;//触发报警的人/物
    private Integer map;//地图id
    private Integer area;//区域id
    private Integer type;//报警类型
    private String describe;//描述
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;//触发时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;//结束时间
    private int warnstate;//报警状态 0报警中 1报警结束
    private String floor;//报警楼层
    private String position;//报警位置

    //报警类型名
    private String typeName;
    //地图名
    private String mapName;

    private String duration;//持续时长

    private String tagid;//标签id

    private String areaType;//区域类型


    private String areaName;//区域名称
}
