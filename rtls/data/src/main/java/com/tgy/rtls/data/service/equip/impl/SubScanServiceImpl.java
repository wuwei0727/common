package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.SubScan;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.equip.SubScanMapper;
import com.tgy.rtls.data.service.equip.SubScanService;
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
public class SubScanServiceImpl implements SubScanService {
    @Autowired(required = false)
    private SubScanMapper subScanMapper;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private LocalUtil localUtil;
    @Override
    public List<SubScan> findByAll(Integer status,String code1,String code2){
       List<SubScan> subScanList=subScanMapper.findByAll(status,code1,code2,localUtil.getLocale());
       for (SubScan subScan:subScanList){
           Substation sub = subMapper.findByNum(subScan.getNum(),localUtil.getLocale());
           if (NullUtils.isEmpty(sub)){
               subScan.setStatus(0);
           }else {
               subScan.setStatus(1);
           }
       }
        return subScanList;
    }

    @Override
    @Cacheable(value = "subScanNum",key = "#num")
    public SubScan findByNum(String num) {
        return subScanMapper.findByNum(num);
    }

    @Override
    @CacheEvict(value = "subScanNum",key = "#subScan.num")
    public boolean addSubScan(SubScan subScan) {
        return subScanMapper.addSubScan(subScan)>0;
    }

    @Override
    public boolean updateStatus(String num, Integer status) {
        return subScanMapper.updateStatus(num,status)>0;
    }

    @Override
    public boolean delSubScan() {
        return subScanMapper.delSubScan()>0;
    }
}
