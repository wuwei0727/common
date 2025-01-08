package com.tgy.rtls.data.service.routing.impl;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.routing.Route;
import com.tgy.rtls.data.entity.routing.Routedot;
import com.tgy.rtls.data.mapper.routing.RouteMapper;
import com.tgy.rtls.data.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.routing.impl
 * @date 2020/11/23
 */
@Service
@Transactional
public class RouteServiceImpl implements RouteService{
    @Autowired(required = false)
    private RouteMapper routeMapper;


    @Override
    public List<Route> findByAll(Integer map,Integer instanceid) {
        return routeMapper.findByAll(map,instanceid);
    }

    @Override
    public boolean addRoute(Route route) {
        //先新增巡检路线 在新增巡检点
        if (routeMapper.addRoute(route)>0){
            if (!NullUtils.isEmpty(route.getRoutedots())){
                for (Routedot routedot:route.getRoutedots()){
                    routedot.setRid(route.getId());
                    routeMapper.addRoutedot(routedot);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateRoute(Route route) {
        return routeMapper.updateRoute(route)>0;
    }

    @Override
    public List<Route> findByRouteNameAndMap(Integer map, String name) {
        return routeMapper.findByRouteNameAndMap(map, name);
    }

    @Override
    public boolean updateRoutedot(Routedot routedot) {
        return routeMapper.updateRoutedot(routedot)>0;
    }

    @Override
    public boolean delRoute(Integer id) {
        //删除巡检路线前先删除巡检点
        routeMapper.delRoutedotRid(id);
        //删除该路线的巡检任务
        routeMapper.delRoutetask(id);
        return routeMapper.delRoute(id)>0;
    }

    @Override
    public boolean delRoutedot(Integer id) {
        return routeMapper.delRoutedot(id)>0;
    }

    @Override
    public void delRouteMap(String[] maps) {
        String rids=routeMapper.findByMapRouteid(maps);
        if (!NullUtils.isEmpty(rids)){
            String[] split=rids.split(",");
            for (String s:split){
                delRoute(Integer.valueOf(s));
            }
        }
    }


}
