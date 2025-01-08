package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.CrossLevelCorridor;
import com.tgy.rtls.data.mapper.park.CrossLevelCorridorMapper;
import com.tgy.rtls.data.service.park.impl.CrossLevelCorridorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2024-01-04 14:44
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class CrossLevelCorridorServiceImpl extends ServiceImpl<CrossLevelCorridorMapper, CrossLevelCorridor> implements CrossLevelCorridorService {
    @Autowired
    private CrossLevelCorridorMapper corridorMapper;
    @Override
    public int insertSelective(CrossLevelCorridor record) {
        return baseMapper.insertSelective(record);
    }

    @Override
    public int updateByPrimaryKeySelective(CrossLevelCorridor record) {
        return baseMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<CrossLevelCorridor> getAllOrConditionalQuery(String name, Integer map, Integer type, String floorName, String desc, String[] mapIds) {
        return corridorMapper.getAllOrConditionalQuery(name,map,type,floorName,desc,mapIds);
    }

    @Override
    public void addCrossLevelCorridor(CrossLevelCorridor crossLevelCorridor) {

    }

    @Override
    public CrossLevelCorridor getConditionalById(Integer id) {
        return baseMapper.getConditionalById(id);
    }

    @Override
    public void editCrossLevelCorridor(CrossLevelCorridor crossLevelCorridor) {
    }

    @Override
    public Integer delConditionalById(String[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

}
