package com.tgy.rtls.data.mapper.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.MapPathLabel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MapPathLabelMapper extends BaseMapper<MapPathLabel> {
    int updateBatch(List<MapPathLabel> list);

    int batchInsert(@Param("list") List<MapPathLabel> list);

    List<MapPathLabel> getMapPathLabels(@Param("name") String name, @Param("map") Integer map,@Param("floorName") String floorName, @Param("mapIds")String[] mapIds);

    void addMapPathLabel(MapPathLabel mapPathLabel);

    void delMapPathLabel(@Param("ids") String ids);

    MapPathLabel getMapPathLabelById(@Param("id") Integer id);

    void editById(MapPathLabel mapPathLabel);
}