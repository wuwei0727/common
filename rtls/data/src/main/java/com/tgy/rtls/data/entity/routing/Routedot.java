package com.tgy.rtls.data.entity.routing;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/23
 * 巡检点
 */
@Data
@ToString
public class Routedot implements Serializable {
    private Integer id;
    private String name;//巡检点
    private Double x;
    private Double y;
    private Double z;
    private Double range;//范围（米)
    private String startTime;//开始时间
    private String endTime;//结束时间
    private Integer rid;//巡检路线id
    private Short floor;//楼层
}
