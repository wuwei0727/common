package com.tgy.rtls.data.service.map.impl;

import com.tgy.rtls.data.entity.excel.QrCodeLocationDTO;
import com.tgy.rtls.data.entity.map.QrCodeLocation;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.map.impl
*@Author: wuwei
*@CreateTime: 2024-12-16 10:03
*@Description: TODO
*@Version: 1.0
*/
public interface QrCodeLocationService extends IService<QrCodeLocation>{
    List<QrCodeLocation>  getAllQrCodeLocationOrConditionQuery(String map, String areaName, String floorName, String desc, String[] mapids);
    QrCodeLocation getQrCodeLocationById(Integer id);

    List<QrCodeLocationDTO> getByMapId(String mapId);
}
