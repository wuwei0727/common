package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.equip.FloorLockConfig;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.impl
*@Author: wuwei
*@CreateTime: 2024-06-06 14:24
*@Description: TODO
*@Version: 1.0
*/
public interface FloorLockConfigService extends IService<FloorLockConfig>{


    int insertSelective(FloorLockConfig record);

    int updateByPrimaryKeySelective(FloorLockConfig record);

    List<FloorLockConfig> getFloorLockConfigInfo(String map, Integer id, String desc, String[] mapids);

    void addFloorLockConfigInfo(FloorLockConfig config);

    void delFloorLockConfigInfo(String[] split);
}
