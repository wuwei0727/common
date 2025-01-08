package com.tgy.rtls.data.mapper.promoter;
import com.tgy.rtls.data.entity.promoter.PromoterInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *@author wuwei
 *@date 2024/3/20 - 10:07
 */
public interface PromoterInfoMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(PromoterInfo record);

    int insertSelective(PromoterInfo record);

    PromoterInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PromoterInfo record);

    int updateByPrimaryKey(PromoterInfo record);

    List<PromoterInfo> getPromoterInfo(@Param("name") String name, @Param("map") Integer map, @Param("province") String province, @Param("city") String city, @Param("area") String area, @Param("desc") String desc);

    int deleteByIdIn(@Param("ids")String[] ids);

}