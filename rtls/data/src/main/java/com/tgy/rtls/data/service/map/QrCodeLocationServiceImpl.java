package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.excel.QrCodeLocationDTO;
import com.tgy.rtls.data.entity.map.QrCodeLocation;
import com.tgy.rtls.data.mapper.map.QrCodeLocationMapper;
import com.tgy.rtls.data.service.map.impl.QrCodeLocationService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.map
*@Author: wuwei
*@CreateTime: 2024-12-16 10:03
*@Description: TODO
*@Version: 1.0
*/
@Service
public class QrCodeLocationServiceImpl extends ServiceImpl<QrCodeLocationMapper, QrCodeLocation> implements QrCodeLocationService{

    @Override
    public List<QrCodeLocation> getAllQrCodeLocationOrConditionQuery(String map, String areaName, String floorName, String desc, String[] mapids) {
        return baseMapper.getAllQrCodeLocationOrConditionQuery(map, areaName, floorName, desc,mapids);
    }

    @Override
    public QrCodeLocation getQrCodeLocationById(Integer id) {
        return baseMapper.getQrCodeLocationById(id);
    }

    @Override
    public List<QrCodeLocationDTO> getByMapId(String mapId) {
        return baseMapper.getByMapId(mapId);
    }
}
