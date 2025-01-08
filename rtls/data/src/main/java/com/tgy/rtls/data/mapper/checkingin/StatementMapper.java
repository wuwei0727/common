package com.tgy.rtls.data.mapper.checkingin;

import com.tgy.rtls.data.entity.checkingin.Attendancerule;
import com.tgy.rtls.data.entity.checkingin.Statement;
import com.tgy.rtls.data.entity.checkingin.StatementVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.checkingin
 * @date 2020/11/16
 * 考勤报表
 */
public interface StatementMapper {
    /*
    * 实例下的考勤规则信息
    * */
    List<Attendancerule> findByRule(@Param("instanceid")Integer instanceid,String name);

    Attendancerule findByRuleId(@Param("instanceid")Integer instanceid,@Param("type")Integer type);

    /*
    * 新增实例时添加考勤规则
    * */
    int addRule(@Param("instanceid")Integer instanceid);

    /*
    * 修改考勤规则
    * */
    int updateRule(@Param("rule")Attendancerule rule);

    /*
    * 删除实例时  考勤规则删除
    * */
    int delRule(@Param("instanceid")Integer instanceid);

    /*
     * 实例下查询该月的考勤报表
     * */
    List<StatementVO> findByAll(@Param("instanceid")Integer instanceid,@Param("departmentid")Integer departmentid,
                                @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,@Param("classid")Integer classid);

    List<Statement> findByStatement(@Param("month")String month,@Param("personid")Integer personid,String name);
}
