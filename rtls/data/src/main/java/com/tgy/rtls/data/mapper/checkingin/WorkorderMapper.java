package com.tgy.rtls.data.mapper.checkingin;

import com.tgy.rtls.data.entity.checkingin.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.checkingin
 * @date 2020/11/13
 * 班次管理
 */
public interface WorkorderMapper {
    /*
    * 实例下查询班次
    * */
    List<WorkorderVO> findByAll(@Param("instanceid")Integer instanceid,String name);

    /*
    * 查看班次详情
    * */
    WorkorderVO findById(@Param("id")Integer id,String name);

    Workorder findByWorkorderId(@Param("id")Integer id);

    /*
    * 班次编号重名判断
    * */
    Workorder findByNum(@Param("num")String num);

    /*
    *新增班次
    * */
    int addWorkorder(@Param("workorder")WorkorderVO workorder);

    int addWorksystem(@Param("worksystem")Worksystem worksystem);

    /*
    * 修改班次
    * */
    int updateWorkorder(@Param("workorder")WorkorderVO workorder);

    int updateWorksystem(@Param("worksystem")Worksystem worksystem);

    /*
    * 删除班次
    * */
    int delWorkorder(@Param("ids")String[] ids);

    int delWorksystem(@Param("ids")String[] ids);

    /*
    * 新增排班列表
    * */
    int addScheduling(@Param("scheduling")Scheduling scheduling);

    /*
    * 删除排班
    * */
    int delScheduling(@Param("scheduling")Scheduling scheduling);

    /*
    * 删除该月班次下的所有排班
    * */
    int delSchedulingWoid(@Param("woid")Integer woid,@Param("month")String month);

    /*
    * 查询一个月的排班信息 month-->年月
    * */
    List<SchedulingVO> findByScheduling(@Param("month")String month,@Param("instanceid")Integer instanceid,String name);

    /*
    * 查看该月使用的班次
    * */
    List<WorkorderVO> findBySchedulingWorkorder(@Param("month")String month,@Param("instanceid")Integer instanceid,String name);

    /*
    * 查询一个月的有排班信息的人员
    * */
    List<PersonSchedulingVO> findBySchedulingPerson(@Param("month")String month, @Param("instanceid")Integer instanceid);

    /*
    * 查询该月使用的班次制度信息
    * */

    List<WorksystemVO> findByWorksystemVO(@Param("month")String month, @Param("instanceid")Integer instanceid);

    /**
     * 获取工作制度详情
     * @param personid
     * @return
     */
    List<WorkInf> findWorkInfNameByPersonId(Integer personid,String name);

    /**
     * 获取工作制度详情
     * @param personid
     * @return
     */
    List<WorkInf> findWorkInfNameByPersonIdAsso(Integer personid,String name);

    /**
     * 获取工作制度名称
     * @param personid
     * @return
     */
    List<WorkInf> findWorkInfByPersonId(Integer personid,String name);

    boolean delWorkorderByInstance(Integer instanceid);
}
