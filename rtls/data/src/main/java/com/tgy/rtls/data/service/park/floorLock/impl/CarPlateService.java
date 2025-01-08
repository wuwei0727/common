package com.tgy.rtls.data.service.park.floorLock.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.floorLock.CarPlate;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock.impl
*@Author: wuwei
*@CreateTime: 2024-07-17 15:44
*@Description: TODO
*@Version: 1.0
*/
public interface CarPlateService extends IService<CarPlate>{


    List<CarPlate> getCarPlate(Integer map, String companyId, String plateNumber, String phone, String desc, String[] mapids);

    CarPlate getCarPlateById(Integer id);
}
