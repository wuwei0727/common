package com.tgy.rtls.data.entity.routing;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.Routing
 * @date 2020/11/23
 * 巡检记录
 */
@Data
@ToString
public class Routerecord implements Serializable {
    private Integer id;
    private String month;//年月
    private Integer day;//天
    private Integer personid;//人员id
    private Integer rdid;//巡检点id
    private String arriveTime;//实际到达时间
    private Integer status;//状态 0异常 1正常
}
