package com.tgy.rtls.data.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @description 针对表【sys_user(SmallApp用户信息表)】的数据库操作Mapper
 * @createDate 2022-07-21 20:58:41
 * @Entity com.tgy.rtls.data.entity.user.SysUserDemo
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    /**
     * @param keyword 关键字
     * @param desc    排序
     * @return 查询所有用户
     */
    List<SysUser> getAllUsers(@Param("keyword") String keyword, @Param("enable") Integer enable, @Param("desc") String desc);

    List<SysUser> getAllUsers2(@Param("keyword") String keyword, @Param("enable") Integer enable, @Param("createuId") Integer createuId, @Param("desc") String desc);

    SysUser findUserName(String userName);

    SysUser getUserName(String userName);

    int removeByIds(@Param("ids") String[] ids);

    /*
     * 新增smallApp登录记录
     * */
    int addLonginRecord(@Param("login") LoginRecord login);

    /**
     * 根据用户id获取当前用户的权限列表
     *
     * @param userId
     * @return
     */
    Set<String> getPermByUserId(Integer userId);

    /**
     * 根据用户Id获取地图
     *
     * @param userId 用户Id
     * @return
     */
    List<Map_2d> getUserByIdMap(Integer userId);

    List<Map_2d> getUserByIdMap1(Integer map);

    //删除smallAPP用户权限
    int delUserPermiss(@Param("userId") Integer userId);

    //插入权限
    int insertUserPermiss(@Param("permissIds") List<Integer> permissIds,
                          @Param("userId") Integer userId);

    Set<String> findByUserId(@Param("userId") Integer userId, String name);

    /*
     * 修改SMAllapp用户登录时间
     * */
    int updateAddTime(@Param("userId") Integer userId, @Param("loginTime") String loginTime);

    int insertUserMap(@Param("userId") Integer userId, @Param("mapid") String mapId);

    int addUserMap(@Param("mapid") List<String> mapIds, @Param("userId") Integer userId);

    SysUser getByUserId(@Param("userId") Integer userId);

    List<Map_2d> getMapName(@Param("userId") Integer userId);

    int saveAppUserMap(@Param("sysUser") SysUser sysUser);

    int updateByUserId(@Param("sysUser") SysUser sysUser);

    int delUserMap(Integer userId);

    List<SysUser> getCreateByuid(@Param("userId") Integer userId);
}




