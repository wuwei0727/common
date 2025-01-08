package com.tgy.rtls.data.service.view.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.view.LocationSharingLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;
import com.tgy.rtls.data.mapper.view.LocationSharingLogMapper;
import com.tgy.rtls.data.service.view.LocationSharingLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 *@author wuwei
 *@date 2024/3/12 - 22:50
 */
@Service
public class LocationSharingLogServiceImpl extends ServiceImpl<LocationSharingLogMapper,LocationSharingLog> implements LocationSharingLogService{

    @Autowired
    private LocationSharingLogMapper locationSharingLogMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return locationSharingLogMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(LocationSharingLog record) {
        return locationSharingLogMapper.insert(record);
    }

    @Override
    public int insertLocationLog(userSearchLogVo record) {
        return locationSharingLogMapper.insertLocationLog(record);
    }

    @Override
    public int insertSelective(LocationSharingLog record) {
        return locationSharingLogMapper.insertSelective(record);
    }

    @Override
    public LocationSharingLog selectByPrimaryKey(Integer id) {
        return locationSharingLogMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(LocationSharingLog record) {
        return locationSharingLogMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(LocationSharingLog record) {
        return locationSharingLogMapper.updateByPrimaryKey(record);
    }

}
