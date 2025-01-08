package com.tgy.rtls.data.service.update;

import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TagFirmwareService {

    TagfirmwareEntity findByTagid(@Param("tagid") Long tagid);

    void updateById(TagfirmwareEntity tagfirmwareEntity);

   public List<TagfirmwareEntity> getAll();

    void insert(TagfirmwareEntity tagfirmwareEntity);

}
