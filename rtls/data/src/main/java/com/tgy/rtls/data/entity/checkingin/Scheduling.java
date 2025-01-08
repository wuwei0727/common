package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/13
 * 排班类
 */
@Data
@ToString
public class Scheduling implements Serializable {
    private Integer id;//排班自增id
    private String month;//年月
    private Integer day;//日
    private Integer woid;//班次id
    private Integer type;//制度类型 三八制 1早 2中 3晚  四六制 1一 2二 3三 4四
    private Integer personid;//人员id
    private Integer instanceid;

}
