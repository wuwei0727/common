package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.equip.TagScan;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.equip.TagScanMapper;
import com.tgy.rtls.data.service.equip.TagScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2021/1/19
 */
@Service
public class TagScanServiceImpl implements TagScanService {
    @Autowired(required = false)
    private TagScanMapper tagScanMapper;
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired
    LocalUtil localUtil;
    @Override
    public List<TagScan> findByAll(Integer status, Integer type, String code1, String code2) {
        List<TagScan> tagScanList=tagScanMapper.findByAll(status,type,code1,code2,localUtil.getLocale());
        for (TagScan tagScan:tagScanList){
            Tag tag = tagMapper.findByNum(tagScan.getNum());
            if (NullUtils.isEmpty(tag)) {
                tagScan.setStatus(0);
            } else {
                tagScan.setStatus(1);
            }
        }
        return tagScanList;
    }

    @Override
    @Cacheable(value = "tagScanNum",key = "#num")
    public TagScan findByNum(String num) {
        return tagScanMapper.findByNum(num);
    }

    @Override
    @CacheEvict(value = "tagScanNum",key = "#tagScan.num")
    public boolean addTagScan(TagScan tagScan) {
        return tagScanMapper.addTagScan(tagScan)>0;
    }

    @Override
    public boolean updateStatus(String num, Integer status) {
        return tagScanMapper.updateStatus(num,status)>0;
    }


    @Override
    public boolean delTagScan() {
        return tagScanMapper.delTagScan()>0;
    }

    @Override
    public boolean addInfrared(Infrared infrared) {
        return tagScanMapper.addInfrared(infrared)>0;
    }
    @Override
    public boolean updateInfrared(DeviceInfoVO infrared) {
        return tagScanMapper.updateInfrared(infrared)>0;
    }
}
