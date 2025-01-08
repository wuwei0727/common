package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/13
 * 工种
 */
@Data
@ToString
public class Worktype implements Serializable {
    private Integer id;
    private String name;//工种名
    private Integer instanceid;
}
