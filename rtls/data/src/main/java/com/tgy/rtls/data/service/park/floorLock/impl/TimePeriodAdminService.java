package com.tgy.rtls.data.service.park.floorLock.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock.impl
*@Author: wuwei
*@CreateTime: 2024-07-16 11:27
*@Description: TODO
*@Version: 1.0
*/
public interface TimePeriodAdminService extends IService<TimePeriodAdmin>{
    int insertSelective(TimePeriodAdmin record);

    int updateByPrimaryKeySelective(TimePeriodAdmin record);

    List<TimePeriodAdmin> getTimePeriodAdminInfo(Integer map, String companyId, String dayOfWeek, String desc,Integer num, String[] mapids);

    TimePeriodAdmin getTimePeriodAdminInfoById(Integer id);
}
