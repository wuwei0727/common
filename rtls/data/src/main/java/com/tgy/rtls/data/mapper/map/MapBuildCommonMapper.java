package com.tgy.rtls.data.mapper.map;

import com.tgy.rtls.data.entity.map.MapBuildCommon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:59
 */
public interface MapBuildCommonMapper {
    List<MapBuildCommon> getByConditions(@Param("name") String name, @Param("map") Integer map, @Param("floor") Integer floor, @Param("desc") String desc, @Param("floorName") String floorName, @Param("objectType") String objectType, @Param("mapids") String[] mapids, @Param("fid") String fid);

    boolean addMapBuild(MapBuildCommon mapBuildCommon);

    boolean updateMapBuild(MapBuildCommon mapBuildCommon);

    List<MapBuildCommon> getByConditions2(@Param("name") String name, @Param("map") Integer map, @Param("floor") Integer floor, @Param("desc") String desc, @Param("floorName") String floorName, @Param("objectType") String objectType, @Param("mapids") String[] mapids, @Param("fid") String fid);

    boolean addMapBuild2(MapBuildCommon mapBuildCommon);

    boolean updateMapBuild2(MapBuildCommon mapBuildCommon);

    MapBuildCommon getMapBuild2ById(Integer id);

    MapBuildCommon getMapBuild2ById2(Integer id);

    boolean delMapBuild(@Param("ids") String[] ids);

    boolean delMapBuild2(@Param("ids") String[] ids);
}
