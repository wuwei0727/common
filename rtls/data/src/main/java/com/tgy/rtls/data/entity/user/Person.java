package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/10/13
 * 人员
 */
@Data
@ToString
public class Person implements Serializable {
    private Integer id;
    private String name;//人员名称
    private int sex;//性别 0女1男
    private Integer tagid;//定位卡编号
    private String birthday;//出生日期
    private String num;//工号
    private Integer department;//部门id
    private Integer job;//职务id
    private Integer level;//等级id
    private Integer worktype;  //工种id
    private String phone;//电话
    private String identity;//身份证
    private Integer worklocation;//工作地点 与区域关联
    private String photo;//照片
    private String photolocal;//照片
    private Integer worksystem;//工作制度
    private Integer workorder;//班次
    private Integer instanceid;//实例id
    private String orderName;//工作班组名称
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;//下井时间
    private Integer map;//所在地图id
    private Integer status;//在线状态
    private String sub;//所在分站
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date subTime;//进入分站的时间



    private String checkTime;//应检时间
    private String finishTime;//检查结束时间
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
    //在线状态
    private String statusName;
    //井下状态 0井下 1井外
    private Integer minestate;

    private String offTime;

    private String duration;//井下停留时间
    private Integer areaType;//工作地点类型id
    private String worklocationName;//工作地点信息
    private String areaTypeName;//工作地点类型


}
