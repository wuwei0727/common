package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/29
 * 系统类型相关信息
 */
@Data
@ToString
public class Status implements Serializable {
    private Integer typeid; //类型id
    private String name;//类型名
}
