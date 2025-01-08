package com.tgy.rtls.data.service.update.imp;

import com.tgy.rtls.data.entity.update.BsfirmwareEntity;
import com.tgy.rtls.data.mapper.update.BsfirmwareDao;
import com.tgy.rtls.data.service.update.BsFirmwareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BsFirmwareServiceImp implements BsFirmwareService {
@Autowired(required = false)
    BsfirmwareDao bsfirmwareDao;
    @Override
    public BsfirmwareEntity findByBsid(long bsid) {
        return bsfirmwareDao.findByBsid(bsid);
    }

    @Override
    public void insertBsfirmwareEntity(BsfirmwareEntity bsfirmwareEntity) {
        bsfirmwareDao.insertBsfirmwareEntity(bsfirmwareEntity);

    }

    @Override
    public int update(BsfirmwareEntity entity) {
        return update(entity);
    }
}
