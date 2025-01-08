package com.tgy.rtls.data.mapper.park;
import org.apache.ibatis.annotations.Param;

import com.tgy.rtls.data.entity.pay.SmsQuota;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park
*@Author: wuwei
*@CreateTime: 2023-11-13 14:52
*@Description: TODO
*@Version: 1.0
*/
public interface SmsQuotaMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(SmsQuota record);

    int insertSelective(SmsQuota record);

    SmsQuota selectByPrimaryKey(Integer id);

    SmsQuota getSmsQuotaByMap(@Param("map") Integer map, @Param("id") Integer id);

    int updateByPrimaryKeySelective(SmsQuota record);

    int updateByPrimaryKey(SmsQuota record);

    List<SmsQuota> getSmsQuotaOrCondition(@Param("map") String map, @Param("desc") String desc, @Param("mapids") String[] mapids);

    int deleteByIdIn(@Param("ids")String[] ids);
}