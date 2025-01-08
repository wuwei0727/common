package com.tgy.rtls.data.entity.routing;

import com.tgy.rtls.data.entity.user.PersonVO;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.routing
 * @date 2020/11/27
 */
@Data
@ToString
public class RoutetaskVO {
    private Integer id;
    private String month;//年月
    private Integer day;//天
    private Integer personid;
    private PersonVO person;//人员信息
    private List<RoutedotVO> routedotVOs;//巡检点记录

    private Integer status;//是否正常 0异常 1正常
}
