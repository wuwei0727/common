package com.tgy.rtls.data.service.type.impl;

import com.tgy.rtls.data.entity.type.Level;
import com.tgy.rtls.data.mapper.type.LevelMapper;
import com.tgy.rtls.data.service.type.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type.impl
 * @date 2020/10/15
 */
@Service
@Transactional
public class LevelServiceImpl implements LevelService {

    @Autowired(required = false)
    private LevelMapper levelMapper;

    @Override
    public List<Level> findByAll(Integer instanceid) {
        return levelMapper.findByAll(instanceid);
    }

    @Override
    public Level findById(Integer id) {
        return levelMapper.findById(id);
    }

    @Override
    public Boolean addLevel(Level level) {
        return levelMapper.addLevel(level)>0;
    }

    @Override
    public Boolean updateLevel(Level level) {
        return levelMapper.updateLevel(level)>0;
    }

    @Override
    public Boolean delLevel(String ids) {
        String[] split=ids.split(",");
        return levelMapper.delLevel(split)>0;
    }

    @Override
    public List<Level> findLevelByName(Integer instance, String name) {
        return levelMapper.findByName(instance,name);
    }
}
