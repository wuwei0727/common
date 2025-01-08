package com.tgy.rtls.data.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUserMap;

/**
* @author Administrator
* @description 针对表【sys_person_map(人员和地图关联表)】的数据库操作Service
* @createDate 2022-07-13 19:54:35
*/
public interface SysUserMapService extends IService<SysUserMap> {

    Integer updateByUserId(String userId);
}
