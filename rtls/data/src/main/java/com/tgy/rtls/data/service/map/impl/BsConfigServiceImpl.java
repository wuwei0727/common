package com.tgy.rtls.data.service.map.impl;

import com.alibaba.fastjson.JSONObject;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.map.BsConfigMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.BsConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map.impl
 * @date 2020/10/20
 */
@Service
public class BsConfigServiceImpl implements BsConfigService {
    @Autowired(required = false)
    private BsConfigMapper bsConfigMapper;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LocalUtil localUtil;

    @Override
    @Cacheable(cacheNames = "bsConfigs")
    public List<BsConfig> findByAll(Integer map) {
        return bsConfigMapper.findByAll (map, localUtil.getLocale ());
    }

    @Override
    @Cacheable(cacheNames = "bsConfigs")
    public String findByAll1(Integer map) {
        List<BsConfig> byAll = bsConfigMapper.findByAll(map, localUtil.getLocale());
        return JSONObject.toJSONString(byAll);
    }

    @Override
    public BsConfig findById(Integer id) {
        return bsConfigMapper.findById(id, localUtil.getLocale());
    }

    @Override
    @Cacheable(value = "bsconfignum", key = "#num" ,unless="#result == null")
    public BsConfig findByNum(String num) {
        return bsConfigMapper.findByNum(num, localUtil.getLocale());
    }

    @Override
    @Transactional
    @CacheEvict(value = "bsConfigs*",allEntries = true)
    public Boolean updateBsConfig(BsConfig bsConfig) {
        if (bsConfigMapper.updateBsConfig(bsConfig) > 0) {
            //修改substation表
            //subMapper.updateAntennadelay(bsConfig.getId(),bsConfig.getAntennadelay(),bsConfig.getDisfix());
            //清除缓存
            Substation substation = subMapper.findByBsid(bsConfig.getId(), localUtil.getLocale());
            redisService.remove("bsconfignum::" + substation.getNum());
            return true;
        }
        return false;
    }

    @Override
    @CacheEvict(value = "bsconfignum", key = "#num")
    @Transactional
    public Boolean updateBsConfig(BsConfig bsConfig, String num) {
        if (bsConfigMapper.updateBsConfig(bsConfig) > 0) {
            Substation substation = new Substation();
            substation.setNum(num);
            substation.setId(bsConfig.getBsid());
            substation.setUpdateTime(new Date());
            subMapper.updateSub(substation);
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public Boolean delBsConfig(String ids) {
        String[] split = ids.split(",");
        HashSet bsids = new HashSet();
        for (String id : split) {
            BsConfig bsConfig = findById(Integer.valueOf(id));
            bsids.add(bsConfig.getNum());
            String sub = bsConfigMapper.getSub(Integer.valueOf(id));

            if(!NullUtils.isEmpty(sub)){
                bsConfigMapper.delSub(sub.split(","));
            }
        }


        if (bsConfigMapper.delBsConfig(split) > 0) {
            try {
                for (Object num : bsids) {
                    String numString = (String) num;
                    redisService.remove("bsconfignum::" + numString);
                }
            } catch (Exception e) {

            }

            return true;
        } else {
            return false;
        }
    }
    @Override
    @Transactional
    @CacheEvict(value = "bsConfigs",key = "#map")
    public Boolean delBsConfig1(String ids, Integer map) {
        String[] split = ids.split(",");
        HashSet bsids = new HashSet();
        for (String id : split) {
            BsConfig bsConfig = findById(Integer.valueOf(id));
            bsids.add(bsConfig.getNum());
            String sub = bsConfigMapper.getSub(Integer.valueOf(id));

            if(!NullUtils.isEmpty(sub)){
                bsConfigMapper.delSub(sub.split(","));
            }
        }


        if (bsConfigMapper.delBsConfig(split) > 0) {
            try {
                for (Object num : bsids) {
                    String numString = (String) num;
                    redisService.remove("bsconfignum::" + numString);
                }
            } catch (Exception e) {

            }

            return true;
        } else {
            return false;
        }
    }
    @Override
    @CacheEvict(value = "bsConfigs", key = "#bsConfig.map")
    public void addDisparkBsConfig(BsConfig bsConfig) {
        bsConfigMapper.addDisparkBsConfig(bsConfig);
    }
}
