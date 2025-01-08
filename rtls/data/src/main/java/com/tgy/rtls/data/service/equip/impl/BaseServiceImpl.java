package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.equip.Basestation;
import com.tgy.rtls.data.mapper.equip.BaseMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2021/1/5
 * 微基站管理
 */
@Service
public class BaseServiceImpl implements BaseService {
    @Autowired(required = false)
    private BaseMapper baseMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LocalUtil localUtil;
    @Autowired
    Executor executor;

    @Override
    public List<Basestation> findByAll(String num, Integer networkstate, String desc, Integer map,Integer relevance, Integer instanceid) {
        return baseMapper.findByAll(num,networkstate,desc,map,relevance,instanceid,localUtil.getLocale());
    }

    @Override
    public Basestation findById(Integer id) {
        return baseMapper.findById(id);
    }

    @Override
 @Cacheable(value = "bsstationId",key = "#num", unless="#result == null")
    public Basestation findByNum(String num) {
        return baseMapper.findByNum(num);
    }

    @Override
    public boolean addBasestation(Basestation base) {
        return baseMapper.addBasestation(base)>0;
    }

    @Override
  @CacheEvict(value = "bsstationId",key = "#base.num")
    public boolean updateBasestation(Basestation base) {
        //下发指令-->用于修改微基站的坐标
        return baseMapper.updateBasestation(base)>0;
    }

    @Override
  @CacheEvict(value = "bsstationId",key = "#num")
    public void updateBaseNetworkstate(String num, Integer networkstate) {
        baseMapper.updateBaseNetworkstate(num,networkstate);
    }

    @Override
    public boolean delBasestation(String ids) {

        String[] split = ids.split(",");
        HashSet names=new HashSet();
        for (String s:split
             ) {
            Basestation base = findById(Integer.valueOf(s));
            if(base!=null){
                names.add(base.getNum());
            }
        }

        if (baseMapper.delBasestation(split)>0) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (Object id : names
                    ) {
                        if (id != null) {
                            String num=(String) id;
                            redisService.remove("bsstationId::" + num);
                        }
                    }
                }
            });

            return true;
        }else{
            return false;
        }
    }

/*    @Override
    @CacheEvict(value = "bsstationId",key = "#num")
    public boolean delBasestationBynum(String num) {

        return false;
    }*/

    @Override
    public int delBasestationByInstance(Integer instanceid) {
        List<Basestation> all = findByAll(null, null, null, null, null, instanceid);
        if(   baseMapper.delBasestationByInstance(instanceid)>0){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    for (Basestation bs : all
                    ) {
                        if (bs != null) {
                            redisService.remove("bsstationId::" + bs.getNum());
                        }
                    }
                }

            });
            return 1;
        }else {
            return 0;
        }

    }


}
