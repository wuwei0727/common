package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/9
 * 事件日志类型
 */
@Data
@ToString
public class EventlogType implements Serializable {
    private Integer id;
    private String name;
    private Integer instanceid;
}
