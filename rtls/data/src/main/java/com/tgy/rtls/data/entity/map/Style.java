package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/10/19
 * 样式信息类
 */
@Data
@ToString
public class Style implements Serializable {
    private Integer id;
    private String type;
    private String url;
}
