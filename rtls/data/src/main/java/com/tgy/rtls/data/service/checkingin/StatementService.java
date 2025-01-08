package com.tgy.rtls.data.service.checkingin;

import com.tgy.rtls.data.entity.checkingin.Attendancerule;
import com.tgy.rtls.data.entity.checkingin.StatementVO;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.checkingin
 * @date 2020/11/16
 */
public interface StatementService {
    /*
     * 实例下的考勤规则信息
     * */
    List<Attendancerule> findByRule(Integer instanceid);

    /*
     * 修改考勤规则
     * */
    boolean updateRule(List<Attendancerule> rules);

    /*
     * 实例下查询该月的考勤报表
     * */
    List<StatementVO> findByAll(Integer instanceid,String month,Integer departmentid,Integer worktypeid,Integer jobid,Integer classid);

    void exportStatementVO(ServletOutputStream out, Integer instanceid,String month,Integer departmentid,Integer worktypeid,Integer jobid,Integer classid, String title)throws Exception;
}
