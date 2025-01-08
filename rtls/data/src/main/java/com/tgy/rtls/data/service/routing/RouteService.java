package com.tgy.rtls.data.service.routing;

import com.tgy.rtls.data.entity.routing.Route;
import com.tgy.rtls.data.entity.routing.Routedot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing
 * @date 2020/11/23
 */
public interface RouteService {
    /*
     * 查询地图上的巡检路线
     * */
    List<Route> findByAll(Integer map,Integer instanceid);

    /*
     * 新增巡检路线
     * */
    boolean addRoute(Route route);

    /*
     *修改巡检路线
     * */
    boolean updateRoute(Route route);

    List<Route> findByRouteNameAndMap(@Param("map")Integer map,@Param("name")String name);

    /*
     * 修改巡检点
     * */
    boolean updateRoutedot(Routedot routedot);

    /*
     * 删除巡检路线
     * */
    boolean delRoute(Integer id);

    /*
     * 删除巡检点 id-->巡检点id  rid-->巡检路线id
     * */
    boolean delRoutedot(Integer id);

    /*
    * 删除地图时 将地图上的巡检路线删除
    * */
    void delRouteMap(@Param("maps")String[] maps);
}
