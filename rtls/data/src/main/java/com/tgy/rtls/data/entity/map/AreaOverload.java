package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/21
 * 区域超员报警规则
 */
@Data
@ToString
public class AreaOverload implements Serializable {
    private Integer id;
    private Integer maxnum;//人数上限
    private Integer enable;
    private String startTime;
    private String endTime;
    private Integer instanceid;
    private Short floor;
    private Integer area;//规则所绑定的区域id
}
