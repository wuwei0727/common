package com.tgy.rtls.data.entity.message;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.message
 * @date 2020/10/30
 * 报警规则
 */
@Data
@ToString
public class WarnRule implements Serializable {
    private Integer id;
    private Integer type;//报警类型
    private String rule;//报警规则
    private Integer enable;//是否启用 0否 1是
    private Integer map;//地图id

    private String typeName;//类型名
    private String mapName;//地图名

}
