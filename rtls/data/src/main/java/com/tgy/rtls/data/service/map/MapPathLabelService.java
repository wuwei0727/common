package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.map.MapPathLabel;

import java.util.List;
public interface MapPathLabelService extends IService<MapPathLabel>{


    int updateBatch(List<MapPathLabel> list);

    int batchInsert(List<MapPathLabel> list);

    List<MapPathLabel> getMapPathLabels(String name, Integer map, String floorName, String[] mapIds);

    void addMapPathLabel(MapPathLabel mapPathLabel);

    void delMapPathLabel(String[] ids);

    MapPathLabel getMapPathLabelById(Integer id);

    void editById(MapPathLabel mapPathLabel);
}
