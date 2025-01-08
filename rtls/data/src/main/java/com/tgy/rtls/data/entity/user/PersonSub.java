package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/11/2
 * 人员分站信息
 */
@Data
@ToString
public class PersonSub implements Serializable {
    private Integer id;
    private String name;//人员名称
    private String num;//工号
    private String tagName;//卡号

    //部门名称
    private String departmentName;
    //职务
    private String jobName;
    //等级
    private String levelName;
    //工种
    private String worktypeName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date insubTime;//进入分站时间

    private String duration;//分站停留时间
}
