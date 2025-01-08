package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2021/1/26
 * 报警统计-报警数量的变化
 */
@Data
@ToString
public class WarnFlow implements Serializable {
    private int count;//报警数
    private int type;
    private String typeName;//报警类型名
    private String color;//颜色
}
