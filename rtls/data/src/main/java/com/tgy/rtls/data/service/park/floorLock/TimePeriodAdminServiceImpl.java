package com.tgy.rtls.data.service.park.floorLock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import com.tgy.rtls.data.mapper.park.floorLock.TimePeriodAdminMapper;
import com.tgy.rtls.data.service.park.floorLock.impl.TimePeriodAdminService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 11:27
*@Description: TODO
*@Version: 1.0
*/
@Service
public class TimePeriodAdminServiceImpl extends ServiceImpl<TimePeriodAdminMapper, TimePeriodAdmin> implements TimePeriodAdminService{

    @Override
    public int insertSelective(TimePeriodAdmin record) {
        return baseMapper.insertSelective(record);
    }
    @Override
    public int updateByPrimaryKeySelective(TimePeriodAdmin record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<TimePeriodAdmin> getTimePeriodAdminInfo(Integer map, String companyId, String dayOfWeek, String desc,Integer num,String[] mapids) {
        return baseMapper.getTimePeriodAdminInfo(map, companyId, dayOfWeek, desc,num,mapids);
    }

    @Override
    public TimePeriodAdmin getTimePeriodAdminInfoById(Integer id) {
        return baseMapper.getTimePeriodAdminInfoById(id);
    }
}
