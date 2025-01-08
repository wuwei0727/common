package com.tgy.rtls.data.mapper.update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.update.TagfirmwareEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-18
 */
@Mapper
public interface TagfirmwareDao extends BaseMapper<TagfirmwareEntity> {

    TagfirmwareEntity findByTagid(@Param("tagid") Long tagid);

    List<TagfirmwareEntity> getAll();
}
