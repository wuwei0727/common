package com.tgy.rtls.data.entity.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/10/28
 * 入井记录表
 */
@Data
@ToString
public class IncoalRecord implements Serializable {
    private Integer id;
    private Integer personid;//入井人员id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inTime;//入井时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date outTime;//出井时间
}
