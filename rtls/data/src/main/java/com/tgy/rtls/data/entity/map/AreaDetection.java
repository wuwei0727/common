package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/21
 * 区域出入口检测规则
 */
@Data
@ToString
public class AreaDetection implements Serializable {
    private Integer id;
    private Integer type;//类型 1出口 0入口
    private Integer enable;//是否启用 0否 1是

    private String startTime;
    private String endTime;
    private Short floor;
    private Integer instanceid;    private Integer area;//规则所绑定的区域id
}
