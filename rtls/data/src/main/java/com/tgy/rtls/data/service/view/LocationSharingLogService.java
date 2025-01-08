package com.tgy.rtls.data.service.view;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.view.LocationSharingLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;

/**
*@author wuwei
*@date 2024/3/12 - 22:50
*/
public interface LocationSharingLogService extends IService<LocationSharingLog> {

int deleteByPrimaryKey(Integer id);

int insert(LocationSharingLog record);

int insertLocationLog(userSearchLogVo record);

int insertSelective(LocationSharingLog record);

LocationSharingLog selectByPrimaryKey(Integer id);

int updateByPrimaryKeySelective(LocationSharingLog record);

int updateByPrimaryKey(LocationSharingLog record);

}
