package com.tgy.rtls.data.service.park.floorLock.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.floorLock.UserCompanyMap;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park.floorLock.impl
 * @Author: wuwei
 * @CreateTime: 2024-07-18 14:29
 * @Description: TODO
 * @Version: 1.0
 */
public interface UserCompanyMapService extends IService<UserCompanyMap> {


    List<UserCompanyMap> getUserCompanyMap(Integer map, Integer companyId, String userName,String phone,String licensePlate, String desc, String[] mapids);
}
