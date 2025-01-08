package com.tgy.rtls.data.service.user;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.user.MaintenanceStaff;
import com.tgy.rtls.data.mapper.user.MaintenanceStaffMapper;
import com.tgy.rtls.data.service.user.impl.MaintenanceStaffService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.user
*@Author: wuwei
*@CreateTime: 2024-10-29 10:04
*@Description: TODO
*@Version: 1.0
*/
@Service
public class MaintenanceStaffServiceImpl extends ServiceImpl<MaintenanceStaffMapper, MaintenanceStaff> implements MaintenanceStaffService{

    @Override
    public List<MaintenanceStaff> getAllOrFilteredMaintenanceStaff(String map, String name, String phone, String status, String map1, String desc, String[] mapids) {
        return baseMapper.getAllOrFilteredMaintenanceStaff(map, name, phone, status, map1, desc, mapids);
    }

    @Override
    public MaintenanceStaff getMaintenanceStaffById(int id) {
        return baseMapper.getMaintenanceStaffById(id);
    }
}
