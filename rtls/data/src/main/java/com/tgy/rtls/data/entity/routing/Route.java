package com.tgy.rtls.data.entity.routing;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/23
 * 巡检路线
 */
@Data
@ToString
public class Route implements Serializable {
    private Integer id;
    private Integer map;//地图id
    private String name;//巡检路线名
    private String style;//样式
    private String mapName;//
    private Short floor;

    private List<Routedot> routedots;//巡检点集合
}
