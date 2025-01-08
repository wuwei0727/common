package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/20
 * 区域点
 */
@Data
@ToString
public class AreaDot implements Serializable {
    private Integer id;
    private Double x;
    private Double y;
    private Double z;
    private Integer area;//区域id
}
