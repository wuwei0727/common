package com.tgy.rtls.data.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/9
 * 事件日志
 */
@Data
@ToString
public class Eventlog implements Serializable {
    private Integer id;
    private Integer personid; //人员id
    private Integer map;//地图id
    /*private Integer type;//类型id*/
    private String event;//事件内容
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;//触发时间
    private String mapName;//地图名

    private String personName;//人员名
    private String tagName;//标签编号
    private String departmentName;//部门名称
    private Integer typeSimple;
    private String typeSimpleName;
    private String bsid;
}
