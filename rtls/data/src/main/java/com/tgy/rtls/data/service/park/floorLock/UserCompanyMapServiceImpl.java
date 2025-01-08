package com.tgy.rtls.data.service.park.floorLock;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;
import com.tgy.rtls.data.mapper.park.floorLock.UserCompanyMapMapper;
import com.tgy.rtls.data.service.park.floorLock.impl.UserCompanyMapService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-18 14:29
*@Description: TODO
*@Version: 1.0
*/
@Service
public class UserCompanyMapServiceImpl extends ServiceImpl<UserCompanyMapMapper, UserCompanyMap> implements UserCompanyMapService{

    @Override
    public List<UserCompanyMap> getUserCompanyMap(Integer map, Integer companyId, String userName,String phone,String licensePlate, String desc, String[] mapids) {
        return baseMapper.getUserCompanyMap(map, companyId, userName,phone,licensePlate,desc, mapids);
    }
}
