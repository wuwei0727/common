package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/13
 * 班次视图显示
 */
@Data
@ToString
public class WorkorderVO implements Serializable {
    private Integer id;//班次id
    private String num;//编号
    private String style;//样式
    private String system;//工作制度 0三八制 1四六制
    private Integer instanceid;//实例id
    private String name;//status name

    private List<Worksystem> worksystemList;//班次制度信息

    private String systemName;//工作制度名
}
