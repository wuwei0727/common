package com.tgy.rtls.data.entity.type;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.type
 * @date 2020/10/13
 * 标签类型
 */
@Data
@ToString
public class Tagtype implements Serializable {
    private Integer id;
    private String name;
}
