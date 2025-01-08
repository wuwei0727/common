package com.tgy.rtls.data.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/10
 * 操作日志
 */
@Data
@ToString
public class Operationlog implements Serializable {
    private Integer id;
    private Integer uid;//登录成员id
    private String incident;//事件
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;
    private Integer instanceid;
    private String ip;

    private String name;//操作人员
}
