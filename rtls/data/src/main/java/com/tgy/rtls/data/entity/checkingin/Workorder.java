package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.checkingin
 * @date 2020/11/13
 * 班次类
 */
@Data
@ToString
public class Workorder implements Serializable {
    private Integer id;//班次id
    private String num;//编号
    private String style;//样式
    private String system;//工作制度 0三八制 1四六制
    private Integer instanceid;//实例id
}
