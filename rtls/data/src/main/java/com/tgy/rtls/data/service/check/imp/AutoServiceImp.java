package com.tgy.rtls.data.service.check.imp;


import com.tgy.rtls.data.entity.check.AutoidEntity;
import com.tgy.rtls.data.mapper.check.AutoidDao;
import com.tgy.rtls.data.service.check.AutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class AutoServiceImp implements AutoService {
    @Autowired(required = false)
    AutoidDao autoidDao;
    @Override
    @CachePut(cacheNames = "autoid")
    public AutoidEntity getAutoId(String key) {
        return autoidDao.getIdByRedisKey(key);
    }
    @CacheEvict(cacheNames = "autoid")
  public   void updateById(AutoidEntity autoid){
        autoidDao.updateById(autoid);
    }
}
