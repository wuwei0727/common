package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/20
 * 区域类型管理
 */
@Data
@ToString
public class Areatype implements Serializable {
    private Integer id;
    private String name;
    private String color;
    private Integer instanceid;
}
