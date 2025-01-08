package com.tgy.rtls.data.mapper.routing;

import com.tgy.rtls.data.entity.routing.Route;
import com.tgy.rtls.data.entity.routing.Routedot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.Routing
 * @date 2020/11/23
 * 巡检路线--路线和巡检点管理
 */
public interface RouteMapper {
    /*
    * 查询地图上的巡检路线
    * */
    List<Route> findByAll(@Param("map") Integer map,@Param("instanceid")Integer instanceid);

    /*
    * 查询地图上巡检路线id
    * */
    String findByMapRouteid(@Param("maps")String[] maps);

    /*
    * 新增巡检路线
    * */
    int addRoute(@Param("route")Route route);

    /*
    * 新增巡检点
    * */
    int addRoutedot(@Param("routedot")Routedot routedot);

    /**
     * 根据名字和map查找巡检线路
     * @param map
     * @param name
     * @return
     */
   List<Route> findByRouteNameAndMap(@Param("map")Integer map,@Param("name")String name);

    /*
    *修改巡检路线
    * */
    int updateRoute(@Param("route")Route route);

    /*
    * 修改巡检点
    * */
    int updateRoutedot(@Param("routedot")Routedot routedot);

    /*
    * 删除巡检路线
    * */
    int delRoute(@Param("id")Integer id);

    /*
    * 删除巡检点 id-->巡检点id  rid-->巡检路线id
    * */
    int delRoutedot(@Param("id")Integer id);

    int delRoutedotRid(@Param("rid")Integer rid);

    /*
    * 删除巡检任务 id-->巡检路线id
    * */
    int delRoutetask(@Param("id")Integer id);
}
