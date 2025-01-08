package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/16
 * 报表详情
 */
@Data
@ToString
public class StatementVO implements Serializable {
    private Integer id;//人员id
    private String name;//人员名
    private String departmentName;//部门
    private String month;//年月
    private Integer addendanceday;//出勤天数
    private Integer belateday;//迟到天数
    private Integer leaveday;//早退天数
    private Integer absenteeismday;//旷工天数
    private Integer vacationday;//休假天数
    private List<Statement> statements;
}
