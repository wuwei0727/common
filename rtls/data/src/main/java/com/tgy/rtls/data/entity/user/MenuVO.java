package com.tgy.rtls.data.entity.user;

import lombok.Data;

import java.util.List;

@Data
public class MenuVO {
    private Long id;
    private String menuCode;
    private String menuName;
    private String permissions;  // 主菜单权限
    private List<SubMenuVO> subPermissions;  // 子菜单权限列表


    @Data
    public static class SubMenuVO {
        private String menuCode;
        private String menuName;
        private String permissions;  // 子菜单权限
    }
}