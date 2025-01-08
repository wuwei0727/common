package com.tgy.rtls.data.service.type.impl;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.type.Areatype;
import com.tgy.rtls.data.mapper.type.AreatypeMapper;
import com.tgy.rtls.data.service.type.AreatypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type.impl
 * @date 2020/10/20
 */
@Service
@Transactional
public class AreatypeServiceImpl implements AreatypeService{
    @Autowired(required = false)
    private AreatypeMapper areatypeMapper;

    @Override
    public List<Areatype> findByAll(Integer instanceid) {
        return areatypeMapper.findByAll(instanceid);
    }

    @Override
    public Areatype findById(Integer id) {
        return areatypeMapper.findById(id);
    }

    @Override
    public Boolean addAreatype(Areatype areatype) {
        return areatypeMapper.addAreatype(areatype)>0;
    }

    @Override
    public Boolean updateAreatype(Areatype areatype) {
        return areatypeMapper.updateAreatype(areatype)>0;
    }

    @Override
    public Boolean delAreatype(String ids) {
        String[] split=ids.split(",");
        return areatypeMapper.delAreatype(split)>0;
    }

    @Override
    public List<Area> findByAreatypeId(Integer id) {
        return areatypeMapper.findByAreatypeId(id);
    }
}
