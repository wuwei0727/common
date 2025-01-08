package com.tgy.rtls.data.mapper.view;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.view.UserSearchLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;

/**
 *@author wuwei
 *@date 2024/3/18 - 14:34
 */
public interface UserSearchLogMapper extends BaseMapper<UserSearchLog> {
    int deleteByPrimaryKey(Integer id);

    int insertSelective(UserSearchLog record);
    int insertUserSearchLog(userSearchLogVo record);

    UserSearchLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserSearchLog record);

    int updateByPrimaryKey(UserSearchLog record);
}