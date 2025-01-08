package com.tgy.rtls.data.service.promoter.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.promoter.PromoterQrCode;
import com.tgy.rtls.data.mapper.promoter.PromoterQrCodeMapper;
import com.tgy.rtls.data.service.promoter.PromoterQrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/3/20 - 15:22
 */
@Service
public class PromoterQrCodeServiceImpl extends ServiceImpl<PromoterQrCodeMapper, PromoterQrCode> implements PromoterQrCodeService {

    @Autowired
    private PromoterQrCodeMapper promoterQrCodeMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return promoterQrCodeMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(PromoterQrCode record) {
        return promoterQrCodeMapper.insert(record);
    }

    @Override
    public int insertSelective(PromoterQrCode record) {
        return promoterQrCodeMapper.insertSelective(record);
    }

    @Override
    public PromoterQrCode selectByPrimaryKey(Integer id) {
        return promoterQrCodeMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PromoterQrCode> getById(Integer id) {
        return promoterQrCodeMapper.getById(id);
    }

    @Override
    public int updateByPrimaryKeySelective(PromoterQrCode record) {
        return promoterQrCodeMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(PromoterQrCode record) {
        return promoterQrCodeMapper.updateByPrimaryKey(record);
    }

    @Override
    public int updateQrCodeById(PromoterQrCode record) {
        return promoterQrCodeMapper.updateQrCodeById(record);
    }

    @Override
    public List<PromoterQrCode> getPromoterQrCodeInfo(String name,String shangJiaName, Integer map, String desc, String[] mapids) {
        return promoterQrCodeMapper.getPromoterQrCodeInfo(name,shangJiaName, map,desc,mapids);
    }

    @Override
    public void deleteByIdIn(String[] split) {
        promoterQrCodeMapper.deleteByIdIn(split);
    }
}

