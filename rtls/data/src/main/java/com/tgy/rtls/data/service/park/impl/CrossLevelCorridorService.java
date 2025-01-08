package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.CrossLevelCorridor;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.impl
*@Author: wuwei
*@CreateTime: 2024-01-04 14:44
*@Description: TODO
*@Version: 1.0
*/
public interface CrossLevelCorridorService extends IService<CrossLevelCorridor>{


    int insertSelective(CrossLevelCorridor record);

    int updateByPrimaryKeySelective(CrossLevelCorridor record);

    List<CrossLevelCorridor> getAllOrConditionalQuery(String name, Integer map, Integer type, String floorName, String desc, String[] mapIds);

    void addCrossLevelCorridor(CrossLevelCorridor crossLevelCorridor);

    CrossLevelCorridor getConditionalById(Integer id);

    void editCrossLevelCorridor(CrossLevelCorridor crossLevelCorridor);

    Integer delConditionalById(String[] ids);
}
