package com.tgy.rtls.data.service.user.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUserMap;
import com.tgy.rtls.data.mapper.user.SysUserMapMapper;
import com.tgy.rtls.data.service.user.SysUserMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* @author Administrator
* @description 针对表【sys_person_map(人员和地图关联表)】的数据库操作Service实现
* @createDate 2022-07-13 19:54:35
*/
@Service
@Transactional
public class SysUserMapServiceImpl extends ServiceImpl<SysUserMapMapper, SysUserMap> implements SysUserMapService {
    @Autowired
    private SysUserMapMapper sysUserMapMapper;

    @Override
    public Integer updateByUserId(String userId) {
        return sysUserMapMapper.updateByUserId(userId);
    }
}




