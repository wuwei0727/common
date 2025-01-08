package com.tgy.rtls.data.mapper.common;

import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.common.EventlogType;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.common
 * @date 2020/11/9
 */
public interface EventlogMapper {
    /*
    *事件日志查询
    **/
    List<Eventlog> findByAll(@Param("instanceid")Integer instanceid,@Param("map")Integer map,@Param("type")Integer type,Integer typeSimple,
                             @Param("startTime")String startTime,@Param("endTime")String endTime,Integer departmentId,String personName,String name);

    /*
    * 新增事件日志
    * */

    int addEventlog(@Param("eventlog")Eventlog eventlog);



    /*
    * 事件类型查询
    * */
    List<EventlogType> findByType(@Param("instanceid")Integer instanceid);

    /*
     * 事件类型查询simple
     * */
    List<EventlogType> findByTypeSimple(String name);

    /*
     * 事件类型查询simple，查询最近的进入分站的数据信息
     * */
    EventlogType findByTypeSimpleBsid(String bsid,@Param("personid")Integer personid);



    /*
    * 事件类型id获取
    * */
    int findByEventlogType(@Param("name")String name,@Param("instanceid")Integer instanceid);
}
