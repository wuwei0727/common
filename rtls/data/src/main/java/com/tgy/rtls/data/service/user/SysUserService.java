package com.tgy.rtls.data.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.SysUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * @author Administrator
 * @description 针对表【sys_user(SmallApp用户信息表)】的数据库操作Service
 * @createDate 2022-07-21 20:58:41
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * @param keyword 关键字
     * @param desc    排序
     * @return 查询所有用户
     */
    List<SysUser> getAllUsers(String keyword, Integer enabled, String desc);
    List<SysUser> getAllUsers2(String keyword, Integer enable, Integer createuId, String desc);

    /**
     * 判断用户名重复
     *
     * @param userName 用户名
     * @return 判断用户名重复
     */
    SysUser findUserName(String userName);

    /**
     * <p>
     * 删除（根据ID 批量删除）
     * </p>
     *
     * @param ids 主键ID列表
     */
    int removeByIds(@Param("ids") String[] ids);

    /**
     * 根据登录名查询用户
     *
     * @param userName
     * @return
     */
    SysUser queryUser(String userName);

    boolean addLonginRecord(String userName, String ip);

    /**
     * 根据用户Id获取地图
     *
     * @param userId 用户Id
     * @return
     */
    List<Map_2d> getUserByIdMap(Integer userId);

    List<Map_2d> getUserByIdMap1(Integer userId);

    /**
     * 设置smallAPP用户权限
     *
     * @param permissionIds
     * @param userId
     * @return
     */
    int setUserPermiss(List<Integer> permissionIds, Integer userId);

    /**
     * 根据id查用户权限
     *
     * @param userId
     * @return
     */
    Set<String> findByUserId(Integer userId);

    /*
     * 修改成员登录时间
     * */
    boolean updateAddTime(Integer userId, String loginTime);

    int insertUserMap(Integer userId, String mapid);

    SysUser getByUserId(Integer userId);

    boolean saveAppUserMap(SysUser sysUser);

    boolean updateByUserId(SysUser sysUser);

    int delUserMap(List<String> mapIds, Integer userId);

    List<SysUser> getCreateByuid(Integer userId);
}
