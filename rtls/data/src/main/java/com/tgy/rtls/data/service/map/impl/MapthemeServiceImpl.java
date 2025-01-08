package com.tgy.rtls.data.service.map.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.map.Maptheme;
import com.tgy.rtls.data.mapper.map.MapthemeMapper;
import com.tgy.rtls.data.service.map.MapthemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapthemeServiceImpl extends ServiceImpl<MapthemeMapper, Maptheme> implements MapthemeService {

    @Autowired
    private MapthemeMapper mapthemeMapper;
    @Override
    public int updateBatch(List<Maptheme> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int batchInsert(List<Maptheme> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int addMaptheme(Maptheme maptheme) {
        return mapthemeMapper.addMaptheme(maptheme);
    }

    @Override
    public int editMaptheme(Maptheme maptheme) {
        return mapthemeMapper.editMaptheme(maptheme);

    }
}
