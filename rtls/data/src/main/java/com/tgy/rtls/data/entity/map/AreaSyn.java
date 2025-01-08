package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/22
 * 区域信息新增修改时传输的参数
 */
@Data
@ToString
public class AreaSyn implements Serializable {
    private Integer id;
    private String name;
    private Integer type;
    private Integer enable;
    private String color;
    private Integer map;
    private Short floor;
    //区域点集合
    private List<AreaDot> dots;

    private List<AreaTurnover> turnovers;

    private List<AreaOverload> overloads;

    private List<AreaDetection> detections;
}
