package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/20
 * 区域类
 */
@Data
@ToString
public class Area implements Serializable {
    private Integer id;
    private String name;
    private Integer type;
    private Integer enable;
    private String color;
    private Integer map;
    private Short floor;

    //区域类型名
    private String typeName;
    //区域点集合
    private List<AreaDot> dots;
}
