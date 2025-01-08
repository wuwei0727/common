package com.tgy.rtls.data.service.view.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.view.UserSearchLog;
import com.tgy.rtls.data.entity.view.userSearchLogVo;
import com.tgy.rtls.data.mapper.view.UserSearchLogMapper;
import com.tgy.rtls.data.service.view.UserSearchLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 *@author wuwei
 *@date 2024/3/18 - 14:34
 */
@Service
public class UserSearchLogServiceImpl extends ServiceImpl<UserSearchLogMapper,UserSearchLog> implements UserSearchLogService{

    @Autowired
    private UserSearchLogMapper userSearchLogMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return userSearchLogMapper.deleteByPrimaryKey(id);
    }


    @Override
    public int insertSelective(UserSearchLog record) {
        return userSearchLogMapper.insertSelective(record);
    }
    @Override
    public int insertUserSearchLog(userSearchLogVo record) {
        return userSearchLogMapper.insertUserSearchLog(record);
    }

    @Override
    public UserSearchLog selectByPrimaryKey(Integer id) {
        return userSearchLogMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(UserSearchLog record) {
        return userSearchLogMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(UserSearchLog record) {
        return userSearchLogMapper.updateByPrimaryKey(record);
    }

}
