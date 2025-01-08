package com.tgy.rtls.data.mapper.view;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.view.LocationSharingLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;

/**
 *@author wuwei
 *@date 2024/3/12 - 22:50
 */
public interface LocationSharingLogMapper extends BaseMapper<LocationSharingLog> {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(LocationSharingLog record);

    LocationSharingLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(LocationSharingLog record);

    int updateByPrimaryKey(LocationSharingLog record);
    int insertLocationLog(userSearchLogVo record);
    int insertUserSearchLog(userSearchLogVo record);
}