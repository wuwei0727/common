package com.tgy.rtls.data.service.map.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.map.Maptheme;
import com.tgy.rtls.data.entity.map.Style;
import com.tgy.rtls.data.mapper.common.RecordMapper;
import com.tgy.rtls.data.mapper.map.Map2dMapper;
import com.tgy.rtls.data.mapper.message.WarnRuleMapper;
import com.tgy.rtls.data.service.common.StatisticsService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.AreaService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.map.MapthemeService;
import com.tgy.rtls.data.service.routing.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map.impl
 * @date 2020/10/19
 */
@Service
@Transactional
public class Map2dServiceImpl extends ServiceImpl<Map2dMapper, Map_2d> implements Map2dService {
    @Autowired(required = false)
    private Map2dMapper map2dMapper;
    @Autowired(required = false)
    private WarnRuleMapper warnRuleMapper;
    @Autowired(required = false)
    private RecordMapper recordMapper;
    @Autowired(required = false)
    private AreaService areaService;
    @Autowired(required = false)
    private SubService subService;
    @Autowired
    private RouteService routeService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private LocalUtil localUtil;
    @Autowired
    private MapthemeService mapthemeService;
    @Override
    public List<Map_2d> findByAll(String name,Integer enable, String instanceid) {
        List<Map_2d> map2ds=map2dMapper.findByAll(name,enable,instanceid,localUtil.getLocale());

        return map2ds;
    }
    @Override
    public List<Map_2d> findByAll2(String name, Integer enable, String instanceid, String comId, String floorName, String[] maps) {
        List<Map_2d> map2ds=map2dMapper.findByAll2(name,enable,instanceid,localUtil.getLocale(),comId,floorName,maps);

        return map2ds;
    }

    @Override
    public List<Map_2d> findByAll3(String name, Integer enable, String instanceid, String floorName, String[] mapidAlls) {
        List<Map_2d> map2ds=map2dMapper.findByAll3(name,enable,instanceid,localUtil.getLocale(),floorName,mapidAlls);
        return map2ds;
    }

    @Override
    public List<Map_2d> findByAll4(String name,Integer enable, String instanceid,String[] mapidAlls) {
        List<Map_2d> map2ds=map2dMapper.findByAll4(name,enable,instanceid,localUtil.getLocale(),mapidAlls);

        return map2ds;
    }

    @Override
    public List<Map_2d> findByAllSame(String name) {
        return map2dMapper.findByAllSame(name);
    }

    @Override
    public String findByNameId(String ids) {
        String[] split=ids.split(",");
        return map2dMapper.findByNameId(split);
    }

    @Override
    public Map_2d findById(Integer id) {
        return map2dMapper.findById(id,localUtil.getLocale());
    }

    @Override
    public Boolean addMap2d(Map_2d map2d) {
        //新增地图成功后 添加默认的报警规则
        map2d.setAddTime(new Date());
        if (map2dMapper.addMap2d(map2d)>0){
            Maptheme maptheme = new Maptheme();
            if (!"".equals(map2d.getThemeName())) {
                map2d.setThemeName(map2d.getThemeName());
            }else {
                map2d.setThemeName("tgymap");
            }
            maptheme.setMapId(String.valueOf(map2d.getId()));
//            mapthemeService.addMaptheme(maptheme);
            warnRuleMapper.addWarnRule(map2d.getId());
            return true;
        }
        return false;
    }

    @Override
    public Boolean deleteByMapIdMap(String mapId,String userId) {
        String[] split = mapId.split(",");
        for (String s : split) {
            map2dMapper.deleteByUserIdMap(s,userId);
        }
        return true;
    }

    @Override
    public int addUsermap(Integer uid,String mapId) {
        try {
            return map2dMapper.addUserMap(uid,mapId);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }

    @Override
    public Boolean updateMap2d(Map_2d map2d) {
        Maptheme maptheme = new Maptheme();
        if (!"".equals(map2d.getThemeName())) {
            map2d.setThemeName(map2d.getThemeName());
        }else {
            map2d.setThemeName("tgymap");
        }
        maptheme.setMapId(String.valueOf(map2d.getId()));
        mapthemeService.editMaptheme(maptheme);
        map2d.setUpdateTime(new Date());
        return map2dMapper.updateMap2d(map2d)>0;
    }

    @Override
    public Boolean delMap2dInstanceid(Integer instanceid) {
        //获取实例下地图id集
        String ids=map2dMapper.findByMapInstance(instanceid);
        //获取实例下分站id集
        String subids=map2dMapper.findBySubInstance(instanceid);
        if (!NullUtils.isEmpty(subids)){
            subService.delSub(subids);
        }
        if (!NullUtils.isEmpty(ids)){
            delMap2d(ids);
        }
        return true;
    }

    @Override
    public Boolean delMap2d(String ids){
        String[] split = ids.split(",");
        //删除关联地图下的车位
        map2dMapper.delPlace(split);
        //删除地图下的区域
        areaService.delAreaMap(split);
        //删除报警记录
        warnRuleMapper.delWarnRecord(split);
        //删除报警规则
        warnRuleMapper.delWarnRule(split);
        //删除下井记录数据
        recordMapper.delIncoal(split);
        //删除地图上巡检路线信息
        routeService.delRouteMap(split);
        //删除地图的人流量信息
        statisticsService.delManFlow(split);
        //删除地图信息
        map2dMapper.delMap2d(split);
        return true;
    }

    //删除关联地图下的车位
    @Override
    public Boolean delPlace(String ids) {
        String[] split = ids.split(",");
        return map2dMapper.delPlace(split)>0;
    }

    @Override
    public List<Style> findByStyle(String type) {
        return map2dMapper.findByStyle(type);
    }

    @Override
    public Map_2d findByfmapID(String mapId) {
        return map2dMapper.findByMapId(mapId);
    }

    /**
     * 根据ID修改地图二维码地址
     * @param map2d
     * @return
     */
    @Override
    public int updateByIDCode(Map_2d map2d) {
        return map2dMapper.updateByIds(map2d);
    }

    @Override
    public List<Map_2d> findByfmapId(Integer mapId,String fmapId) {
        return map2dMapper.findByfmapId(mapId,fmapId);
    }
    @Override
    public List<Map_2d> getMapName(String[] mapIds) {
        return map2dMapper.getMapName(mapIds);
    }

}
