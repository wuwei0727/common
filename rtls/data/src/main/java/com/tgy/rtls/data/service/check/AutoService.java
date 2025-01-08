package com.tgy.rtls.data.service.check;

import com.tgy.rtls.data.entity.check.AutoidEntity;

public interface AutoService {

    AutoidEntity getAutoId(String key);

    void updateById(AutoidEntity autoid);
}
