package com.tgy.rtls.data.service.view.impl;

import com.tgy.rtls.data.entity.view.EachMapSearchBusiness;
import com.tgy.rtls.data.mapper.view.EachMapSearchBusinessMapper;
import com.tgy.rtls.data.service.view.EachMapSearchBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 *@author wuwei
 *@date 2024/3/7 - 14:52
 */
@Service
public class EachMapSearchBusinessServiceImpl implements EachMapSearchBusinessService{

    @Autowired
    private EachMapSearchBusinessMapper eachMapSearchBusinessMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return eachMapSearchBusinessMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(EachMapSearchBusiness record) {
        return eachMapSearchBusinessMapper.insert(record);
    }


    @Override
    public int insertSelective(EachMapSearchBusiness record) {
        return eachMapSearchBusinessMapper.insertSelective(record);
    }

    @Override
    public EachMapSearchBusiness selectByPrimaryKey(Integer id) {
        return eachMapSearchBusinessMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(EachMapSearchBusiness record) {
        return eachMapSearchBusinessMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(EachMapSearchBusiness record) {
        return eachMapSearchBusinessMapper.updateByPrimaryKey(record);
    }

}
