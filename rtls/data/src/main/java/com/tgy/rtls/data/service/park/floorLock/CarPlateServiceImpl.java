package com.tgy.rtls.data.service.park.floorLock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.floorLock.CarPlate;
import com.tgy.rtls.data.mapper.park.floorLock.CarPlateMapper;
import com.tgy.rtls.data.service.park.floorLock.impl.CarPlateService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-17 15:44
*@Description: TODO
*@Version: 1.0
*/
@Service
public class CarPlateServiceImpl extends ServiceImpl<CarPlateMapper, CarPlate> implements CarPlateService{

    @Override
    public List<CarPlate> getCarPlate(Integer map, String companyId, String plateNumber, String phone, String desc, String[] mapids) {
        return baseMapper.getCarPlate(map, companyId, plateNumber, phone, desc, mapids);
    }

    @Override
    public CarPlate getCarPlateById(Integer id) {
        return baseMapper.getCarPlateById(id);
    }
}
