package com.tgy.rtls.data.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUserMap;
import org.apache.ibatis.annotations.Param;

/**
* @author Administrator
* @description 针对表【sys_person_map(人员和地图关联表)】的数据库操作Mapper
* @createDate 2022-07-13 19:54:35
* @Entity com.tgy.rtls.data.entity.role.SysPersonMap
*/
public interface SysUserMapMapper extends BaseMapper<SysUserMap> {
    Integer updateByUserId(String userId);

    boolean deleteByUserId(@Param("userId") String[] userId);
}




