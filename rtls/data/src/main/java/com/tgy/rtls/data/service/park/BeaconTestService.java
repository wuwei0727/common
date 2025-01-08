package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.BeaconTest;

import java.util.List;
public interface BeaconTestService extends IService<BeaconTest>{


    int updateBatch(List<BeaconTest> list);

    int batchInsert(List<BeaconTest> list);

}
