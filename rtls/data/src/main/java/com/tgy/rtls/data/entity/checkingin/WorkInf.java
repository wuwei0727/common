package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/12/22
 * 用于考勤排班导出
 */
@Data
@ToString
public class WorkInf implements Serializable {
    private Integer id;
    private Integer woid;//班次id
    private Integer type;//类型  三八制 1早 2中 3晚 四六制 1一 2二 3三 4四
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String month;
    private String day;


    private String typeName;//样式
    private String system;//制度类型 0三八制 1四六制
    private String systemName;//制度名称
    private String systemName1;//制度名称
}
