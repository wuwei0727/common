package com.tgy.rtls.data.service.view;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.view.UserSearchLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;

/**
 *@author wuwei
 *@date 2024/3/18 - 14:34
 */
public interface UserSearchLogService extends IService<UserSearchLog> {

    int deleteByPrimaryKey(Integer id);

    int insertSelective(UserSearchLog record);
    int insertUserSearchLog(userSearchLogVo record);

    UserSearchLog selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(UserSearchLog record);

    int updateByPrimaryKey(UserSearchLog record);

}
