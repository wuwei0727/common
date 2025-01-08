package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/13
 * 等级
 */
@Data
@ToString
public class Level implements Serializable {
    private Integer id;
    private String name;//级别名称
    private Integer instanceid;//实例编号
}
