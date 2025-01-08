package com.tgy.rtls.data.entity.checkingin;

import com.tgy.rtls.data.entity.user.Person;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/13
 * 用于查询这一天排班的人员
 */
@Data
@ToString
public class SchedulingVO implements Serializable {
    private Integer id;//排班自增id
    private String month;//年月
    private Integer day;//日
    private Integer woid;//班次id
    private Integer type;//制度类型 三八制 1早 2中 3晚  四六制 1一 2二 3三 4四
    private String name;
    //人员列表
    private List<Person> personList;
}
