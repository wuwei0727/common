package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/10/13
 * 公告
 */
@Data
@ToString
public class Notice implements Serializable {
    private Integer id;
    private int level;//公告等级 0低 1中 2高
    private String content;//公告内容
    private Integer instanceid;
}
