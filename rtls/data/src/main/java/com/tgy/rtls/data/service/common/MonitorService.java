package com.tgy.rtls.data.service.common;

import com.tgy.rtls.data.entity.common.*;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common
 * @date 2020/11/5
 */
public interface MonitorService {
    /*
     * 项目检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    Monitor findByProject(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    /*
     * 地图检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<Monitor> findByMap(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    Monitor findByMapId(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);
    /*
     * 区域检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<MonitorArea> findByArea(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    MonitorArea findByAreaId(Integer instanceid,String map,String area,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    /*
     * 分站检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<MonitorSub> findBySub(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    MonitorSub findBySubId(Integer instanceid,String map,String sub,String departmentid,String worktypeid,String jobid,String startTime,String endTime);


    /*
     * 检测信息的区域下拉框
     * */
    List<MonitorAreatype> findByAreatype(String map);


    List<MonitorSubtype> findBySubtype(String map);
    /*
     * 检测信息-->项目信息统计
     * */
    List<MonitorPerson> findByProjectPerson(Integer instanceid,String departmentid,String worktypeid,String jobid,String startTime,String endTime);

    /*
     * 检测信息-->地图信息统计
     * */
    List<MonitorPerson> findByMapPerson(Integer instanceid,String map,String departmentid,String worktypeid,String jobid,String startTime,String endTime);
    /*
     * 检测信息-->区域信息统计 area-->区域自增id集
     * */
    List<MonitorPerson> findByAreaPerson(Integer instanceid,String map,String area,String departmentid,String worktypeid,String jobid,String startTime,String endTime);
    /*
     * 检测信息-->分站信息统计 sub-->分站自增id集
     * */
    List<MonitorPerson> findBySubPerson(Integer instanceid,String map,String sub,String departmentid,String worktypeid,String jobid,String startTime,String endTime);
}