package com.tgy.rtls.data.mapper.common;

import com.tgy.rtls.data.entity.common.Monitor;
import com.tgy.rtls.data.entity.common.MonitorArea;
import com.tgy.rtls.data.entity.common.MonitorAreatype;
import com.tgy.rtls.data.entity.common.MonitorPerson;
import com.tgy.rtls.data.entity.common.MonitorSub;
import com.tgy.rtls.data.entity.common.MonitorSubtype;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.Area;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.common
 * @date 2020/11/3
 * 监测信息管理
 */
public interface MonitorMapper {
    /*
    * 项目检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
    * */
    Monitor findByProject(@Param("instanceid")Integer instanceid,@Param("map")String map,
                          @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                          @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);

    /*
     * 地图检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<Monitor> findByMap(@Param("instanceid")Integer instanceid,@Param("map")String map,
                      @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                      @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);

    Monitor findByMapId(@Param("instanceid")Integer instanceid,@Param("map")String map,
                            @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                            @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);

    /*
     * 区域检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<MonitorArea> findByArea(@Param("instanceid")Integer instanceid, @Param("map")String map,
                                 @Param("departmentid")String departmentid, @Param("worktypeid")String worktypeid,
                                 @Param("jobid")String jobid, @Param("startTime")String startTime, @Param("endTime")String endTime,String timeformat);

    MonitorArea findByAreaId(@Param("instanceid")Integer instanceid, @Param("map")String map,@Param("area") String area,
                                 @Param("departmentid")String departmentid, @Param("worktypeid")String worktypeid,
                                 @Param("jobid")String jobid, @Param("startTime")String startTime, @Param("endTime")String endTime,String timeformat);
    /*
     * 分站检测信息统计 map-->地图id集 departmentid-->部门id集 worktypeid-->工种id集 jobid-->职务id集
     * */
    List<MonitorSub> findBySub(@Param("instanceid")Integer instanceid, @Param("map")String map,
                               @Param("departmentid")String departmentid, @Param("worktypeid")String worktypeid,
                               @Param("jobid")String jobid, @Param("startTime")String startTime, @Param("endTime")String endTime,@Param("name")String name,String timeformat);

    MonitorSub findBySubId(@Param("instanceid")Integer instanceid, @Param("map")String map,@Param("sub") String sub,
                               @Param("departmentid")String departmentid, @Param("worktypeid")String worktypeid,
                               @Param("jobid")String jobid, @Param("startTime")String startTime, @Param("endTime")String endTime,@Param("name")String name,String timeformat);
    /*
    * 检测信息的区域下拉框
    * */
    List<MonitorAreatype> findByAreatype(@Param("map")String[] map);

    List<Area> findByAreatypeName(@Param("typeName")String typeName,@Param("map")String[] map);

    List<MonitorSubtype> findBySubtype(@Param("map")String[] map,@Param("name")String name);

    List<Substation> findBySubtypeName(@Param("typeName")String typeName, @Param("map")String[] map,@Param("name")String name);
    /*
    * 检测信息-->项目信息统计
    * */
    List<MonitorPerson> findByProjectPerson(@Param("instanceid")Integer instanceid,@Param("departmentid")String departmentid, @Param("worktypeid")String worktypeid,
                                @Param("jobid")String jobid, @Param("startTime")String startTime, @Param("endTime")String endTime,String timeformat);

    /*
     * 检测信息-->地图信息统计
     * */
    List<MonitorPerson> findByMapPerson(@Param("instanceid")Integer instanceid,@Param("map")String map,
                                        @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                                        @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);
    /*
     * 检测信息-->区域信息统计 area-->区域自增id集
     * */
    List<MonitorPerson> findByAreaPerson(@Param("instanceid")Integer instanceid,@Param("map")String map,@Param("area") String area,
                                         @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                                         @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);
    /*
     * 检测信息-->分站信息统计 sub-->分站自增id集
     * */
    List<MonitorPerson> findBySubPerson(@Param("instanceid")Integer instanceid,@Param("map")String map,@Param("sub")String sub,
                                        @Param("departmentid")String departmentid,@Param("worktypeid")String worktypeid,
                                        @Param("jobid")String jobid,@Param("startTime")String startTime,@Param("endTime")String endTime,String timeformat);
}
