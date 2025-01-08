package com.tgy.rtls.data.service.map;

import com.tgy.rtls.data.entity.map.MapBuildCommon;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:57
 */
public interface MapBuildCommonService {
    List<MapBuildCommon> getByConditions(String name, Integer map, Integer floor, String desc, String floorName, String objectType, String[] mapids, String fid);

    boolean addMapBuild(MapBuildCommon mapBuildCommon);

    boolean updateMapBuild(MapBuildCommon mapBuildCommon);

    List<MapBuildCommon> getByConditions2(String name, Integer map, Integer floor, String desc, String floorName, String objectType, String[] mapids, String fid);

    boolean addMapBuild2(MapBuildCommon mapBuildCommon);

    boolean updateMapBuild2(MapBuildCommon mapBuildCommon);

    MapBuildCommon getMapBuild2ById(Integer id);

    MapBuildCommon getMapBuild2ById2(Integer id);

    boolean delMapBuild(String[] ids);

    boolean delMapBuild2(String[] ids);
}
