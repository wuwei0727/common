package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/13
 * 部门
 */
@Data
@ToString
public class Department implements Serializable {
    private Integer id;
    private String name;//部门名称
    private Integer worksystem;//部门工作制度
    private Integer instanceid;//实例编号
}
