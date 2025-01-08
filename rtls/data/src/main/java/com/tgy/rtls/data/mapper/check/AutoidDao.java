package com.tgy.rtls.data.mapper.check;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.check.AutoidEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-13
 */
@Mapper
public interface AutoidDao extends BaseMapper<AutoidEntity> {

   AutoidEntity getIdByRedisKey(@Param("rediskey")String rediskey);

}
