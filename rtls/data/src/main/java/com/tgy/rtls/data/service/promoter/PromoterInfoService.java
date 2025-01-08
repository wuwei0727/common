package com.tgy.rtls.data.service.promoter;

import com.tgy.rtls.data.entity.promoter.PromoterInfo;

import java.util.List;

/**
 *@author wuwei
 *@date 2024/3/20 - 10:07
 */
public interface PromoterInfoService{

    int deleteByPrimaryKey(Integer id);

    int insert(PromoterInfo record);

    int insertSelective(PromoterInfo record);

    PromoterInfo selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PromoterInfo record);

    int updateByPrimaryKey(PromoterInfo record);



    List<PromoterInfo> getPromoterInfo(String name, Integer map, String province, String city, String area,String desc);

    void deleteByIdIn(String[] split);
}
