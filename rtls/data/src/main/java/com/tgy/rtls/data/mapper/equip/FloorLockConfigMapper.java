package com.tgy.rtls.data.mapper.equip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.equip.FloorLockConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2024-06-06 14:24
*@Description: TODO
*@Version: 1.0
*/
public interface FloorLockConfigMapper extends BaseMapper<FloorLockConfig> {
    int insertSelective(FloorLockConfig record);

    int updateByPrimaryKeySelective(FloorLockConfig record);

    List<FloorLockConfig> getFloorLockConfigInfo(@Param("map") String map, @Param("id") Integer id, @Param("desc") String desc, @Param("mapids") String[] mapids);

    void addFloorLockConfigInfo(FloorLockConfig config);

    void delFloorLockConfigInfo(@Param("ids") String[] ids);
}