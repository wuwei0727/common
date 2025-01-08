package com.tgy.rtls.data.service.map.impl;

import com.tgy.rtls.data.entity.map.MapBuildCommon;
import com.tgy.rtls.data.mapper.map.MapBuildCommonMapper;
import com.tgy.rtls.data.service.map.MapBuildCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:57
 */
@Service
public class MapBuildCommonServiceImpl implements MapBuildCommonService {
    @Autowired
    private MapBuildCommonMapper mapBuildCommonMapper;
    @Override
    public List<MapBuildCommon> getByConditions(String name, Integer map, Integer floor, String desc, String floorName, String objectType, String[] mapids, String fid) {
        return mapBuildCommonMapper.getByConditions(name,map,floor,desc,floorName,objectType,mapids, fid);
    }

    @Override
    public boolean addMapBuild(MapBuildCommon mapBuildCommon) {
        return mapBuildCommonMapper.addMapBuild(mapBuildCommon);
    }

    @Override
    public boolean updateMapBuild(MapBuildCommon mapBuildCommon) {
        return mapBuildCommonMapper.updateMapBuild(mapBuildCommon);
    }


    @Override
    public List<MapBuildCommon> getByConditions2(String name, Integer map, Integer floor, String desc, String floorName, String objectType, String[] mapids, String fid) {
        return mapBuildCommonMapper.getByConditions2(name,map,floor,desc,floorName,objectType,mapids,fid);
    }

    @Override
    public boolean addMapBuild2(MapBuildCommon mapBuildCommon) {
        return mapBuildCommonMapper.addMapBuild2(mapBuildCommon);
    }

    @Override
    public boolean updateMapBuild2(MapBuildCommon mapBuildCommon) {
        return mapBuildCommonMapper.updateMapBuild2(mapBuildCommon);
    }

    @Override
    public MapBuildCommon getMapBuild2ById(Integer id) {
        return mapBuildCommonMapper.getMapBuild2ById(id);
    }

    @Override
    public MapBuildCommon getMapBuild2ById2(Integer id) {
        return mapBuildCommonMapper.getMapBuild2ById2(id);
    }

    @Override
    public boolean delMapBuild(String[] ids) {
        return mapBuildCommonMapper.delMapBuild(ids);
    }

    @Override
    public boolean delMapBuild2(String[] ids) {
        return mapBuildCommonMapper.delMapBuild2(ids);
    }
}
