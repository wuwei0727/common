package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.equip.FloorLockConfig;
import com.tgy.rtls.data.mapper.equip.FloorLockConfigMapper;
import com.tgy.rtls.data.service.park.impl.FloorLockConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2024-06-06 14:24
*@Description: TODO
*@Version: 1.0
*/
@Service
public class FloorLockConfigServiceImpl extends ServiceImpl<FloorLockConfigMapper, FloorLockConfig> implements FloorLockConfigService{

    @Override
    public int insertSelective(FloorLockConfig record) {
        return baseMapper.insertSelective(record);
    }
    @Override
    public int updateByPrimaryKeySelective(FloorLockConfig record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<FloorLockConfig> getFloorLockConfigInfo(String map, Integer id, String desc, String[] mapids) {
        return baseMapper.getFloorLockConfigInfo(map,id,desc,mapids);
    }

    @Override
    public void addFloorLockConfigInfo(FloorLockConfig config) {
        baseMapper.addFloorLockConfigInfo(config);
    }

    @Override
    public void delFloorLockConfigInfo(String[] split) {
        baseMapper.delFloorLockConfigInfo(split);
    }
}
