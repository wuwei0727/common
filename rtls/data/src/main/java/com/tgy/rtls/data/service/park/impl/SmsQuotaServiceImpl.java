package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.entity.pay.SmsQuota;
import com.tgy.rtls.data.mapper.park.SmsQuotaMapper;
import com.tgy.rtls.data.service.park.SmsQuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2023-11-13 14:52
*@Description: TODO
*@Version: 1.0
*/
@Service
public class SmsQuotaServiceImpl implements SmsQuotaService{

    @Resource
    private SmsQuotaMapper smsQuotaMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return smsQuotaMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(SmsQuota record) {
        return smsQuotaMapper.insert(record);
    }

    @Override
    public int insertSelective(SmsQuota record) {
        return smsQuotaMapper.insertSelective(record);
    }

    @Override
    public SmsQuota getSmsQuotaById(Integer id) {
        return smsQuotaMapper.selectByPrimaryKey(id);
    }
    @Override
    public SmsQuota getSmsQuotaByMap(Integer map,Integer id) {
        return smsQuotaMapper.getSmsQuotaByMap(map,id);
    }

    @Override
    public int updateByPrimaryKeySelective(SmsQuota record) {
        return smsQuotaMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(SmsQuota record) {
        return smsQuotaMapper.updateByPrimaryKey(record);
    }

	@Override
	public List<SmsQuota> getSmsQuotaOrCondition(String map, String desc, String[] mapids){
		 return smsQuotaMapper.getSmsQuotaOrCondition(map,desc,mapids);
	}

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductQuota(Integer mapId, Integer count) {
        try {
            // 获取短信配额记录
            SmsQuota smsQuota = smsQuotaMapper.getSmsQuotaByMap(mapId,null);

            if (smsQuota == null || smsQuota.getCount() < count) {
                return false;
            }

            // 扣减配额
            smsQuota.setCount(smsQuota.getCount() - count);
            smsQuota.setUpdateTime(new Date());

            // 更新配额
            return smsQuotaMapper.updateByPrimaryKey(smsQuota) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int deleteByIdIn(String[] ids){
        return smsQuotaMapper.deleteByIdIn(ids);
    }


}
