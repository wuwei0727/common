package com.tgy.rtls.data.service.view.impl;

import com.tgy.rtls.data.entity.view.EachMapSearchPlace;
import com.tgy.rtls.data.mapper.view.EachMapSearchPlaceMapper;
import com.tgy.rtls.data.service.view.EachMapSearchPlaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 *@author wuwei
 *@date 2024/3/7 - 14:52
 */
@Service
public class EachMapSearchPlaceServiceImpl implements EachMapSearchPlaceService {

    @Autowired
    private EachMapSearchPlaceMapper eachMapSearchPlaceMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return eachMapSearchPlaceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(EachMapSearchPlace record) {
        return eachMapSearchPlaceMapper.insert(record);
    }

    @Override
    public int insertSelective(EachMapSearchPlace record) {
        return eachMapSearchPlaceMapper.insertSelective(record);
    }

    @Override
    public EachMapSearchPlace selectByPrimaryKey(Integer id) {
        return eachMapSearchPlaceMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(EachMapSearchPlace record) {
        return eachMapSearchPlaceMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(EachMapSearchPlace record) {
        return eachMapSearchPlaceMapper.updateByPrimaryKey(record);
    }

}
