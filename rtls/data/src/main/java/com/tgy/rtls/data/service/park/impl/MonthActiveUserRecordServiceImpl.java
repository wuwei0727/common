package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.MonthActiveUserRecord;
import com.tgy.rtls.data.mapper.park.MonthActiveUserRecordMapper;
import com.tgy.rtls.data.service.park.MonthActiveUserRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonthActiveUserRecordServiceImpl extends ServiceImpl<MonthActiveUserRecordMapper, MonthActiveUserRecord> implements MonthActiveUserRecordService {
        @Autowired
        private MonthActiveUserRecordMapper monthActiveUserRecordMapper;
    @Override
    public int updateBatch(List<MonthActiveUserRecord> list) {
        return baseMapper.updateBatch(list);
    }
    @Override
    public int batchInsert(List<MonthActiveUserRecord> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public Integer add(MonthActiveUserRecord monthUser) {
        return monthActiveUserRecordMapper.add(monthUser);
    }

    @Override
    public Integer insertmap(MonthActiveUserRecord monthUser) {
        return monthActiveUserRecordMapper.insertmap(monthUser);
    }
}
