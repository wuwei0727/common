package com.tgy.rtls.data.mapper.check;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.check.TagchecklocationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author rtls
 * @since 2020-11-05
 */
@Mapper
public interface TagchecklocationDao extends BaseMapper<TagchecklocationEntity> {
    List<TagchecklocationEntity> getByTagcheckbsid_Recent(@Param("checkbsid") Integer checkbsid);
    List<TagchecklocationEntity> getByTagcheckbsid_All(@Param("checkbsid") Integer checkbsid);
}
