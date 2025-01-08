package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.checkingin
 * @date 2020/11/13
 * 班次制度类
 */
@Data
@ToString
public class Worksystem implements Serializable {
    private Integer id;
    private Integer woid;//班次id
    private Integer type;//类型  三八制 1早 2中 3晚 四六制 1一 2二 3三 4四
    private String startTime;//开始时间
    private String endTime;//结束时间

    private String typeName;//类型名
}
