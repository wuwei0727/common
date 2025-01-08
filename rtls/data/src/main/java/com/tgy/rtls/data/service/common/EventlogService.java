package com.tgy.rtls.data.service.common;

import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.common.EventlogType;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common
 * @date 2020/11/9
 * 事件日志管理
 */
public interface EventlogService {
    /*
     *事件日志查询
     **/
    List<Eventlog> findByAll(Integer instanceid,Integer map,Integer type,Integer typeSimple, String startTime, String endTime,Integer departmentId,String personName);

    /*
     * 事件类型查询
     * */
    List<EventlogType> findByType(Integer instanceid);


    List<EventlogType> findByTypeSimple();

    void exportEventlog(ServletOutputStream out, Integer instanceid,Integer map,Integer type,Integer typeSimple, String startTime, String endTime,String title,Integer departmentId,String personName)throws Exception;
}
