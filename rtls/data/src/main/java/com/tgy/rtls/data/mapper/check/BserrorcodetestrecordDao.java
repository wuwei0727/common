package com.tgy.rtls.data.mapper.check;

import com.tgy.rtls.data.entity.check.BserrorcodetestrecordEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-10
 */
@Mapper
public interface BserrorcodetestrecordDao extends BaseMapper<BserrorcodetestrecordEntity> {

    List<BserrorcodetestrecordEntity> getByTagCheckid(@Param("tagcheckid") Long tagcheckid);
    void deleteByTagCheckid(@Param("tagcheckid") Long tagcheckid);

}
