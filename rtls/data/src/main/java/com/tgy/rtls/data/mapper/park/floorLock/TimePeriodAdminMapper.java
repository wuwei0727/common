package com.tgy.rtls.data.mapper.park.floorLock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.floorLock.TimePeriodAdmin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park.floorLock
*@Author: wuwei
*@CreateTime: 2024-07-16 11:31
*@Description: TODO
*@Version: 1.0
*/
public interface TimePeriodAdminMapper extends BaseMapper<TimePeriodAdmin> {
    int insertSelective(TimePeriodAdmin record);

    int updateByPrimaryKeySelective(TimePeriodAdmin record);

    List<TimePeriodAdmin> getTimePeriodAdminInfo(@Param("map") Integer map, @Param("companyId") String companyId, @Param("dayOfWeek") String dayOfWeek, @Param("desc") String desc, @Param("num") Integer num,@Param("mapids") String[]mapids);

    TimePeriodAdmin getTimePeriodAdminInfoById(@Param("id") Integer id);
}