package com.tgy.rtls.data.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.user.MaintenanceStaff;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.user
*@Author: wuwei
*@CreateTime: 2024-10-29 10:04
*@Description: TODO
*@Version: 1.0
*/
public interface MaintenanceStaffMapper extends BaseMapper<MaintenanceStaff> {
    List<MaintenanceStaff> getAllOrFilteredMaintenanceStaff(@Param("map") String map, @Param("name") String name, @Param("phone") String phone, @Param("status") String status, @Param("map1") String map1, @Param("desc") String desc, @Param("mapids") String[] mapids);

    MaintenanceStaff getMaintenanceStaffById(int id);
}