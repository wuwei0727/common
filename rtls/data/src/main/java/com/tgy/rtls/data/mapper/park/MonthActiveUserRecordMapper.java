package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.MonthActiveUserRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MonthActiveUserRecordMapper extends BaseMapper<MonthActiveUserRecord> {
    int updateBatch(List<MonthActiveUserRecord> list);

    int batchInsert(@Param("list") List<MonthActiveUserRecord> list);

    Integer add(MonthActiveUserRecord monthUser);
    Integer insertmap(MonthActiveUserRecord monthUser);
}