package com.tgy.rtls.data.service.view;

import com.tgy.rtls.data.entity.view.EachMapSearchBusiness;

/**
*@author wuwei
*@date 2024/3/7 - 14:52
*/
public interface EachMapSearchBusinessService{

int deleteByPrimaryKey(Integer id);

int insert(EachMapSearchBusiness record);


int insertSelective(EachMapSearchBusiness record);

EachMapSearchBusiness selectByPrimaryKey(Integer id);

int updateByPrimaryKeySelective(EachMapSearchBusiness record);

int updateByPrimaryKey(EachMapSearchBusiness record);

}
