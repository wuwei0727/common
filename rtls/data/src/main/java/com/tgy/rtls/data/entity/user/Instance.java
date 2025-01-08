package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/10/13
 * 实例表
 */
@Data
@ToString
public class Instance implements Serializable {
    private Integer id;
    private String name;//名称
    private String num;//ID编号
    private String code1;//识别码1
    private String code2;//识别码2
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;//创建时间
}
