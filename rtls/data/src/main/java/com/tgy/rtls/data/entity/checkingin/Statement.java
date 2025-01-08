package com.tgy.rtls.data.entity.checkingin;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.checkingin
 * @date 2020/11/16
 * 考勤报表信息
 */
@Data
@ToString
public class Statement implements Serializable {
    private Integer day;//日
    private String typeName;//制度类型名
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String inTime;//上班时间
    private String outTime;//下班时间
    private String duration;//工作时长
    private Integer status;//状态 1出勤 2休假 3迟到 4早退 5旷工 6迟到+早退

}
