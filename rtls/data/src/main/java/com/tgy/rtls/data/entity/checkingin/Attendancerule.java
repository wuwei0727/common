package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/16
 * 考勤规则
 */
@Data
@ToString
public class Attendancerule implements Serializable {
    private Integer id;
    private Integer type;
    private String typeName;//类型名
    private String rule;//规则
    private Integer enable;//是否启用0否 1是
    private Integer instanceid;//实例id
}
