package com.tgy.rtls.data.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/10/31
 * 进出分站记录
 */
public class InsubRecord implements Serializable {
    private Integer id;
    private Integer personid;//人员id
    private String num;//分站编号
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inTime;//进入分站时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date outTime;//离开分站时间
}
