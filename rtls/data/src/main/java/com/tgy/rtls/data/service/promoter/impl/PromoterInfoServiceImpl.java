package com.tgy.rtls.data.service.promoter.impl;

import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Autowired;

import com.tgy.rtls.data.mapper.promoter.PromoterInfoMapper;
import com.tgy.rtls.data.entity.promoter.PromoterInfo;
import com.tgy.rtls.data.service.promoter.PromoterInfoService;

import java.util.List;

/**
 *@author wuwei
 *@date 2024/3/20 - 10:07
 */
@Service
public class PromoterInfoServiceImpl implements PromoterInfoService{

    @Autowired
    private PromoterInfoMapper promoterInfoMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return promoterInfoMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(PromoterInfo record) {
        return promoterInfoMapper.insert(record);
    }

    @Override
    public int insertSelective(PromoterInfo record) {
        return promoterInfoMapper.insertSelective(record);
    }

    @Override
    public PromoterInfo selectByPrimaryKey(Integer id) {
        return promoterInfoMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(PromoterInfo record) {
        return promoterInfoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(PromoterInfo record) {
        return promoterInfoMapper.updateByPrimaryKey(record);
    }

    @Override
    public List<PromoterInfo> getPromoterInfo(String name, Integer map, String province, String city, String area, String desc) {
        return promoterInfoMapper.getPromoterInfo(name, map, province, city, area,desc);
    }

    @Override
    public void deleteByIdIn(String[] split) {
        promoterInfoMapper.deleteByIdIn(split);
    }

}
