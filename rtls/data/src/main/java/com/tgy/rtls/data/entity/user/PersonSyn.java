package com.tgy.rtls.data.entity.user;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/10/15
 * 人员信息的简介
 */
@Data
@ToString
public class PersonSyn implements Serializable {
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
}
