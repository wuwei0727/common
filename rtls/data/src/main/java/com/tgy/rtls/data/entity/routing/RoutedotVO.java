package com.tgy.rtls.data.entity.routing;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/23
 * 巡检点信息
 */
@Data
@ToString
public class RoutedotVO implements Serializable {
    private Integer id;
    private String name;//巡检点
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String arriveTime;//实际到达时间
    private Integer status;//状态 0异常 1正常
}
