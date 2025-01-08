package com.tgy.rtls.data.entity.routing;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/27
 */
@Data
@ToString
public class RouteData implements Serializable {
    private Integer id;
    private String name;//巡检路线名
    private List<RouteVO> routeVOS;
}
