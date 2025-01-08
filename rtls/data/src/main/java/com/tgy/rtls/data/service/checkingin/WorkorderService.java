package com.tgy.rtls.data.service.checkingin;

import com.tgy.rtls.data.entity.checkingin.*;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.checkingin
 * @date 2020/11/13
 */
public interface WorkorderService {
    /*
     * 实例下查询班次
     * */
    List<WorkorderVO> findByAll(Integer instanceid);

    /*
     * 查看班次详情
     * */
    WorkorderVO findById(Integer id);

    /*
     * 班次编号重名判断
     * */
    Workorder findByNum(String num);

    /*
     *新增班次
     * */
    boolean addWorkorder(WorkorderVO workorderVO);

    /*
     * 修改班次
     * */
    boolean updateWorkorder(WorkorderVO workorderVO);

    /*
     * 删除班次
     * */
    boolean delWorkorder(String ids);

    /*
     * 根据实例删除班次
     * */
    boolean delWorkorderByInstance(Integer instanceid);
    /*
     * 新增排班列表
     * */
    boolean addScheduling(Scheduling scheduling,String personids);

    /*
     * 修改排班
     * */
    boolean updateScheduling(Scheduling scheduling,String personids);


    /*
     * 删除该月班次下的所有排班
     * */
    boolean delSchedulingWoid(Integer woid,String month);

    /*
     * 查询一个月的排班信息 month-->年月
     * */
    List<SchedulingVO> findByScheduling(String month,Integer instanceid);

    /*
     * 查看该月使用的班次
     * */
    List<WorkorderVO> findBySchedulingWorkorder(String month,Integer instanceid);

    /*
    * 考勤排班导出
    * */
    void exportRouteTask(ServletOutputStream out, String month, Integer instanceid, String title)throws Exception;

    List<PersonSchedulingVO> exportSchedulingSel(String month,Integer instanceid);


    /**
     * 获取工作制度详情
     * @param personid
     * @return
     */
    List<WorkInf> findWorkInfNameByPersonIdAsso(Integer personid);


    /**
     * 获取工作制度名称
     * @param personid
     * @return
     */
    List<WorkInf> findWorkInfByPersonId(Integer personid);
}
