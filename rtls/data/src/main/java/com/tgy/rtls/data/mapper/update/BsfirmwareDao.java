package com.tgy.rtls.data.mapper.update;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.update.BsfirmwareEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-18
 */
@Mapper
public interface BsfirmwareDao extends BaseMapper<BsfirmwareEntity> {

   BsfirmwareEntity  findByBsid(@Param("bsid") long bsid);
   void   insertBsfirmwareEntity(@Param("bsfirmwareEntity") BsfirmwareEntity bsfirmwareEntity);
    int update(@Param("bsfirmwareEntity") BsfirmwareEntity entity);
}
