package com.tgy.rtls.data.service.update.imp;

import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import com.tgy.rtls.data.mapper.update.TagfirmwareDao;
import com.tgy.rtls.data.service.update.TagFirmwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagFirmwareServiceImp implements TagFirmwareService {
   @Autowired(required = false)
    TagfirmwareDao tagfirmwareDao;

    @Override
   // @CachePut(cacheNames = "tagfirmware")
    public TagfirmwareEntity findByTagid(Long tagid) {
        return tagfirmwareDao.findByTagid(tagid);
    }

    @Override
   // @CacheEvict(cacheNames = "tagfirmware")
    public void updateById(TagfirmwareEntity tagfirmwareEntity) {
              tagfirmwareDao.updateById(tagfirmwareEntity);
    }

    @Override
    public List<TagfirmwareEntity> getAll() {
        return tagfirmwareDao.getAll();
    }

    @Override
    public void insert(TagfirmwareEntity tagfirmwareEntity) {
           tagfirmwareDao.insert(tagfirmwareEntity);
    }


}
