package com.tgy.rtls.data.entity.message;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.message
 * @date 2020/11/6
 */
@Data
@ToString
public class WarnMap implements Serializable {
    private Integer id;
    private String mapName;
    private List<WarnRule> warnRules;
}
