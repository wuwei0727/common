package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/21
 * 白名单
 */
@Data
@ToString
public class whitelist implements Serializable {
    private Integer turnoverid;//进出报警规则id
    private Integer personid;
}
