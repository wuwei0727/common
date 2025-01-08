package com.tgy.rtls.data.entity.checkingin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.user.Person;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/16
 * 班组
 */
@Data
@ToString
public class Classgroup implements Serializable {
    private Integer id;//
    private String name;//班组名
    private Integer count;//成员数量
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date creatTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间
    private String instanceid;//实例id
    private String sName;

    private List<Person> personList;
}
