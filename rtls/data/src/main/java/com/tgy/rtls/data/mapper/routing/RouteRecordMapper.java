package com.tgy.rtls.data.mapper.routing;

import com.tgy.rtls.data.entity.routing.Route;
import com.tgy.rtls.data.entity.routing.RouteData;
import com.tgy.rtls.data.entity.routing.RouteVO;
import com.tgy.rtls.data.entity.routing.Routedot;
import com.tgy.rtls.data.entity.routing.Routerecord;
import com.tgy.rtls.data.entity.routing.Routetask;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.Routing
 * @date 2020/11/23
 * 巡检任务和巡检记录管理
 */
public interface RouteRecordMapper {
    /*
    * 查询该月的巡检任务 month-->年月
    * */
    List<Routetask> findByTask(@Param("month")String month,@Param("instanceid")Integer instanceid);

    List<Routetask> findByTaskRid(@Param("month")String month,@Param("rid")Integer rid);
    /*
    * 查询该月使用过的巡检路线
    * */
    List<Route> findByRoute(@Param("month")String month,@Param("instanceid")Integer instanceid);

    /*
    * 查询人员当天要去的巡检点 month-->年月  day-->天  personid-->人员id
     * */
    List<Routedot> findByRoutedotId(@Param("month")String month, @Param("day")Integer day, @Param("personid")Integer personid);

    /*
    * 查询人员当天是否有正常的巡检记录 month-->年月  day-->天  personid-->人员id  rdid-->巡检点id
    * */
    Routerecord findByRouteRecordId(@Param("month")String month, @Param("day")Integer day, @Param("personid")Integer personid,@Param("rdid")Integer rdid);

   /*
   * 新增巡检任务
   * */
   int addRoutetask(@Param("task")Routetask task);

   /*
   *修改巡检任务
   * */
   int updateRoutetask(@Param("task")Routetask task);

    /*
    * 删除巡检任务 id-->巡检路线id  month-->年月
    * */
    int delRoutetask(@Param("id")Integer id,@Param("month")String month);

    int delRoutetaskByid(@Param("id")Integer id);

    /*
    * 巡检记录查询 month-->年月  name-->路线名  keyword-->人员姓名/工号
    * */
    List<RouteVO> findByRouteRecord(@Param("month")String month,@Param("rid")Integer rid,@Param("keyword")String keyword,@Param("map")Integer map);

    List<RouteData> findByRouteData(@Param("name")String name,@Param("map")Integer map,@Param("instanceid")Integer instanceid);

    /*
    * 新增巡检记录
    * */
    int addRouteRecord(@Param("record")Routerecord record);
}
