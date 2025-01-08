package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/10/15
 * 人员信息的简介
 */
@Data
@ToString
public class PersonVO implements Serializable {
    private Integer id;
    private String name;//人员名称
    private String num;//工号


    //部门名称
    private String departmentName;
    //职务
    private String jobName;
    //等级
    private String levelName;
    //工种
    private String worktypeName;
    //标签
    private String tagName;
    //状态
    private String statusName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;//应检时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;//检查结束时间
    private String identity;

    private int sex;//性别 0女1男
}
