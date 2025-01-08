package com.tgy.rtls.data.service.user.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.user.MaintenanceStaff;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.user.impl
*@Author: wuwei
*@CreateTime: 2024-10-29 10:04
*@Description: TODO
*@Version: 1.0
*/
public interface MaintenanceStaffService extends IService<MaintenanceStaff>{

    List<MaintenanceStaff> getAllOrFilteredMaintenanceStaff(String map, String name, String phone, String status, String map1, String desc, String[] mapids);

    MaintenanceStaff getMaintenanceStaffById(int id);
}
