package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.BeaconTest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BeaconTestMapper extends BaseMapper<BeaconTest> {
    int updateBatch(List<BeaconTest> list);

    int batchInsert(@Param("list") List<BeaconTest> list);
}