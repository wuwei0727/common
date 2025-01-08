package com.tgy.rtls.data.entity.routing;

import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.user.PersonVO;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/23
 * 巡检路线记录
 */
@Data
@ToString
public class RouteVO implements Serializable {
    private Integer id;//任务id
    private String month;//年月
    private Integer day;//天
    private Integer personid;
    private PersonVO person;//人员信息
    private List<RoutedotVO> routedotVOs;//巡检点记录

    private Integer status;//是否正常 0异常 1正常
}
