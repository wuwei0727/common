package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.map.MapPathLabel;
import com.tgy.rtls.data.mapper.map.MapPathLabelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapPathLabelServiceImpl extends ServiceImpl<MapPathLabelMapper, MapPathLabel> implements MapPathLabelService{

    @Autowired
    private MapPathLabelMapper mapPathLabelMapper;

    @Override
    public int updateBatch(List<MapPathLabel> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int batchInsert(List<MapPathLabel> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public List<MapPathLabel> getMapPathLabels(String name, Integer map, String floorName, String[] mapIds) {
        return mapPathLabelMapper.getMapPathLabels(name,map,floorName,mapIds);
    }

    @Override
    public void addMapPathLabel(MapPathLabel mapPathLabel) {
        mapPathLabelMapper.addMapPathLabel(mapPathLabel);
    }

    @Override
    public void delMapPathLabel(String[] ids) {
        for (String id : ids) {
            mapPathLabelMapper.delMapPathLabel(id);
        }
    }

    @Override
    public MapPathLabel getMapPathLabelById(Integer id) {
        return mapPathLabelMapper.getMapPathLabelById(id);
    }

    @Override
    public void editById(MapPathLabel mapPathLabel) {
        mapPathLabelMapper.editById(mapPathLabel);
    }
}
