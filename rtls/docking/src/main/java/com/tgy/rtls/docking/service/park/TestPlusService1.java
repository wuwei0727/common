package com.tgy.rtls.docking.service.park;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.docking.dao.PlaceTest;
import com.tgy.rtls.docking.mapper.TestPlusMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.park
 * @Author: wuwei
 * @CreateTime: 2024-08-09 21:28
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class TestPlusService1 extends ServiceImpl<TestPlusMapper, PlaceTest> {
    @Transactional
    public void updateBatch(List<PlaceTest> places) {
        this.updateBatchById(places);
    }
}
