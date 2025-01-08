package com.tgy.rtls.data.entity.map;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.map
 * @date 2020/11/16
 */
@Data
@ToString
public class AreaVO implements Serializable {
    private Integer id;
    private String name;//区域名
    private String typeName;//区域类型名
    private Integer enable;
    private Integer count;//检测人数
    private Integer maxnum;//人数上限
    private String status;//正常 超员
    private Integer map;
}
