package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.pay.SmsQuota;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park.impl
*@Author: wuwei
*@CreateTime: 2023-11-13 14:52
*@Description: TODO
*@Version: 1.0
*/
public interface SmsQuotaService{


    int deleteByPrimaryKey(Integer id);

    int insert(SmsQuota record);

    int insertSelective(SmsQuota record);

    SmsQuota getSmsQuotaById(Integer id);
    SmsQuota getSmsQuotaByMap(Integer map,Integer id);

    int updateByPrimaryKeySelective(SmsQuota record);

    int updateByPrimaryKey(SmsQuota record);


    int deleteByIdIn(String[] ids);
	List<SmsQuota> getSmsQuotaOrCondition(String map, String desc, String[] mapids);


    boolean deductQuota(Integer mapId, Integer count);
}
