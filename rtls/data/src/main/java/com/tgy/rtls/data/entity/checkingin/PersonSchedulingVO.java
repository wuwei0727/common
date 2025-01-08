package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/12/7
 * 用于考勤排班导出
 */
@Data
@ToString
public class PersonSchedulingVO {
    private Integer id;
    private String name;//人员名称
    private String departmentName;//部门名
    private List<Scheduling> schedulings;//人员的排班信息
}
