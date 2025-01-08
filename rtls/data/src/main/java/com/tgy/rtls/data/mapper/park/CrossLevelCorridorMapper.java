package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.CrossLevelCorridor;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.park
 * @Author: wuwei
 * @CreateTime: 2024-01-04 14:48
 * @Description: TODO
 * @Version: 1.0
 */
public interface CrossLevelCorridorMapper extends BaseMapper<CrossLevelCorridor> {
    int insertSelective(CrossLevelCorridor record);

    int updateByPrimaryKeySelective(CrossLevelCorridor record);

    List<CrossLevelCorridor> getAllOrConditionalQuery(@Param("name") String name, @Param("map") Integer map, @Param("type") Integer type, @Param("floorName") String floorName, @Param("desc") String desc, @Param("mapIds") String[] mapIds);

    CrossLevelCorridor getConditionalById(Integer id);
}