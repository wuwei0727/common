package com.tgy.rtls.data.service.view;

import com.tgy.rtls.data.entity.view.EachMapSearchPlace;

/**
*@author wuwei
*@date 2024/3/7 - 14:52
*/
public interface EachMapSearchPlaceService{

int deleteByPrimaryKey(Integer id);

int insert(EachMapSearchPlace record);

int insertSelective(EachMapSearchPlace record);

EachMapSearchPlace selectByPrimaryKey(Integer id);

int updateByPrimaryKeySelective(EachMapSearchPlace record);

int updateByPrimaryKey(EachMapSearchPlace record);

}
