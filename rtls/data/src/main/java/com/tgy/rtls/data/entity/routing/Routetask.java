package com.tgy.rtls.data.entity.routing;

import com.tgy.rtls.data.entity.user.PersonVO;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.Routing
 * @date 2020/11/23
 * 巡检任务
 */
@Data
@ToString
public class Routetask implements Serializable {
    private Integer id;
    private String month;//年月
    private Integer day;//天
    private Integer rid;//巡检路线id
    private Integer personid;//人员id
    private Integer instanceid;//实例id

    private PersonVO person;//人员
}
