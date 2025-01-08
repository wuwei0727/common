package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.BeaconTest;
import com.tgy.rtls.data.mapper.park.BeaconTestMapper;
import com.tgy.rtls.data.service.park.BeaconTestService;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BeaconTestServiceImpl extends ServiceImpl<BeaconTestMapper, BeaconTest> implements BeaconTestService {

    @Override
    public int updateBatch(List<BeaconTest> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int batchInsert(List<BeaconTest> list) {
        return baseMapper.batchInsert(list);
    }
}
