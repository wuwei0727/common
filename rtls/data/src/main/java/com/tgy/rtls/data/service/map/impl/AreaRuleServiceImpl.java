package com.tgy.rtls.data.service.map.impl;

import com.tgy.rtls.data.entity.map.AreaDetection;
import com.tgy.rtls.data.entity.map.AreaOverload;
import com.tgy.rtls.data.entity.map.AreaTurnover;
import com.tgy.rtls.data.mapper.map.AreaRuleMapper;
import com.tgy.rtls.data.service.map.AreaRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map.impl
 * @date 2020/11/25
 */
@Service
public class AreaRuleServiceImpl implements AreaRuleService {
    @Autowired(required = false)
    private AreaRuleMapper areaRuleMapper;

    @Override
    public List<AreaTurnover> findByTurnoverEnable(Integer area, Integer type) {
        return areaRuleMapper.findByTurnoverEnable(area,type);
    }

    @Override
    public List<AreaOverload> findByOverloadEnable(Integer area) {
        return areaRuleMapper.findByOverloadEnable(area);
    }

    @Override
    public List<AreaDetection> findByDetectionEnable(Integer area, Integer type) {
        return areaRuleMapper.findByDetectionEnable(area,type);
    }

    @Override
    public int findByWhitelist(Integer turnoverid, Integer personid) {
        return areaRuleMapper.findByWhitelist(turnoverid,personid);
    }
}
