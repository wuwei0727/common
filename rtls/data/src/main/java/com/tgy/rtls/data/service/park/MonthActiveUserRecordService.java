package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.MonthActiveUserRecord;

import java.util.List;
public interface MonthActiveUserRecordService extends IService<MonthActiveUserRecord>{


    int updateBatch(List<MonthActiveUserRecord> list);

    int batchInsert(List<MonthActiveUserRecord> list);

    Integer add(MonthActiveUserRecord monthUser);

    Integer insertmap(MonthActiveUserRecord monthUser);
}
