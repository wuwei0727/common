package com.tgy.rtls.data.mapper.check;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.check.BserrorcodetestEntity;
import com.tgy.rtls.data.entity.check.BserrorcodetestrecordEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-09
 */
@Mapper
public interface BserrorcodetestDao extends BaseMapper<BserrorcodetestEntity> {

      BserrorcodetestEntity getByCode(@Param("send") Long send);
      BserrorcodetestrecordEntity getByTagCheckId(@Param("tagcheckid") Long tagcheckid);
      BserrorcodetestEntity getRecentSend(@Param("tagcheckid") Long tagcheckid);
      void truncateTest();

}
