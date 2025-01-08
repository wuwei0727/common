package com.tgy.rtls.data.service.user;

import com.tgy.rtls.data.entity.user.MenuVO;
import com.tgy.rtls.data.entity.user.Permission;

import java.util.List;
import java.util.Set;

public interface PermissionServices {
    /**
     * 根据用户id获取当前用户的权限列表
     * @param uid
     * @return
     */
    Set<String> getPermByUserId(Integer uid);
    List<Permission> getPermisByUser(Integer uid);

    List<MenuVO> getAllMenuPermissions();

    String getMainMenuPermissions(Long id);

    List<MenuVO> getAllMenusWithPermissions();
}
