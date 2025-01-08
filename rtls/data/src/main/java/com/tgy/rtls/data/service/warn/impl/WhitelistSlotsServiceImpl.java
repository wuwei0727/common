package com.tgy.rtls.data.service.warn.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.warn.WhitelistSlots;
import com.tgy.rtls.data.mapper.warn.WhitelistSlotsMapper;
import com.tgy.rtls.data.service.warn.WhitelistSlotsService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.warn
*@Author: wuwei
*@CreateTime: 2024-10-25 09:23
*@Description: TODO
*@Version: 1.0
*/
@Service
public class WhitelistSlotsServiceImpl extends ServiceImpl<WhitelistSlotsMapper, WhitelistSlots> implements WhitelistSlotsService{

    @Override
    public List<WhitelistSlots> getAllOrFilteredWhitelist(String configName, String status, String map,String mapId, String desc, String[] mapids) {
        return baseMapper.getAllOrFilteredWhitelist(configName, status, map, mapId,desc,mapids);
    }

    @Override
    public WhitelistSlots getWhitelistById(int id) {
        return baseMapper.getWhitelistById(id);
    }
}
