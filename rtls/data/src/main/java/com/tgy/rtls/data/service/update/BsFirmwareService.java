package com.tgy.rtls.data.service.update;

import com.tgy.rtls.data.entity.update.BsfirmwareEntity;
import org.apache.ibatis.annotations.Param;

public interface BsFirmwareService {
    BsfirmwareEntity findByBsid(@Param("bsid") long bsid);
    void   insertBsfirmwareEntity(@Param("bsfirmwareEntity") BsfirmwareEntity bsfirmwareEntity);
    int update(@Param("bsfirmwareEntity") BsfirmwareEntity entity);
}
