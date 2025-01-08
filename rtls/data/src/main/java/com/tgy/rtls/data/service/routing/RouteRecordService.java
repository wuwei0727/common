package com.tgy.rtls.data.service.routing;

import com.tgy.rtls.data.entity.routing.*;

import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing
 * @date 2020/11/24
 */
public interface RouteRecordService {
    /*
     * 查询该月的巡检任务 month-->年月
     * */
    List<Routetask> findByTask(String month,Integer instanceid);

    /*
     * 查询该月使用过的巡检路线
     * */
    List<Route> findByRoute(String month,Integer instanceid);

    /*
     * 新增巡检任务
     * */
    boolean addRoutetask(Routetask task);

    /*
     *修改巡检任务
     * */
    boolean updateRoutetask(Routetask task);

    /*
     * 删除巡检任务 id-->巡检路线id  month-->年月
     * */
    boolean delRoutetask(Integer id,String month);

    /*
     * 巡检记录查询 month-->年月  name-->路线名  keyword-->人员姓名/工号
     * */
    List<RouteData> findByRouteRecord(String month, String name, String keyword,Integer map, Integer instanceid);

    /*
     * 查询人员当天要去的巡检点 month-->年月  day-->天  personid-->人员id
     * */
    List<Routedot> findByRoutedotId(String month,Integer day,Integer personid);

    /*
     * 新增巡检记录
     * */
    boolean addRouteRecord(Routerecord record);

    /*
     * 查询人员当天是否有正常的巡检记录 month-->年月  day-->天  personid-->人员id  rdid-->巡检点id
     * */
    Routerecord findByRouteRecordId(String month,Integer day,Integer personid,Integer rdid);

    /*
    * 巡检报表导出
    * */
    void exportRouteRecord(ServletOutputStream out, String month, String name, String keyword,Integer map, Integer instanceid, String title)throws Exception;

    /*
    * 巡检任务导出
    * */
    void exportRouteTask(ServletOutputStream out, String month, Integer instanceid, String title)throws Exception;
}
