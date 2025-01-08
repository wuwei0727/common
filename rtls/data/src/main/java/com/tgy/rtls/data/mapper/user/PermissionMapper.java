package com.tgy.rtls.data.mapper.user;

import com.tgy.rtls.data.config.SubMenuTypeHandler;
import com.tgy.rtls.data.entity.user.MenuVO;
import com.tgy.rtls.data.entity.user.Permission;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

/**
 * @author 许强
 * @Package com.tuguiyao.dao
 * @date 2019/9/27
 */
public interface PermissionMapper {

    List<Permission> findByAll(String name);
    List<Permission> findByCidAll(@Param("cid") Integer cid,@Param("name")String name);

    List<Permission> findByCid(@Param("cid") Integer cid,String name);

    List<Permission> findByUid(@Param("uid") Integer uid,String name);

    int findByMemberPermission(@Param("uid") Integer uid, @Param("permission_id") String permission_id);

    int findByCompanyPermission(@Param("cid") Integer cid, @Param("permission_id") Integer permission_id);

    /*
    * 新增登录人员权限
    * */
    int insertMemberPermission(@Param("uid") Integer uid, @Param("permission_id") String permission_id);

    int insertCompanyPermission(@Param("cid") Integer cid, @Param("permission_id") Integer permission_id);
    /*
    * 新增登录人员项目权限
    * */
    int insertMemberProject(@Param("uid") Integer uid, @Param("pid") Integer pid);

    int insertCompanyProject(@Param("cid") Integer cid, @Param("pid") Integer pid);


    int delCompanyPermission(@Param("cid") String cid, @Param("permission_ids") String[] permission_ids);

    /*
    * 删除登录人员权限
    * */
    int delMemberPermissionId(@Param("uid") Integer uid, @Param("permission_id") String permission_id);
    int delMemberPermissionIds(@Param("uid") Integer uid);

    int delMemberPermission(@Param("uid") String uid, @Param("permission_ids") String[] permission_ids);

    //删除权限
    int delMemberPermiss(@Param("uid") Integer uid);

    int delCompanyPermiss(@Param("companyId") Integer companyId);;

    //插入权限
    int insertMemberPermiss(@Param("permissionIds") List<Integer> permissionIds,
                            @Param("uid") Integer uid);

    //插入角色权限
    int insertMemberPermiss1( @Param("uid") Integer uid,@Param("permissionIds") List<Integer> permissionIds);

    List findRolePermissions(@Param("company") Integer company);

    /**
     * 根据用户id获取当前用户的权限列表
     * @param uid
     * @return
     */
    Set<String> getPermByUId(@Param("uid") Integer uid);

    List<Permission> getPermisByUser(@Param("uid") Integer uid);

    @Select("select id,menu_code menuCode,menu_name menuName,permission_codes permissions from sys_main_menu ")
    List<MenuVO> getAllMenuPermissions();

    @Select("SELECT GROUP_CONCAT(DISTINCT CONCAT(sub_menu_name, ':', permission_codes)) as sub_permissions FROM sys_menu_permission_group WHERE status = 1 and main_menu_id = #{id}")
    String getMainMenuPermissions(Long id);

    @Select("SELECT " +
            "m.id, m.menu_code, m.menu_name, m.sort_order, " +
            "GROUP_CONCAT(DISTINCT CONCAT(g.sub_menu_name, ':', g.permission_codes)) as sub_permissions, " +
            "GROUP_CONCAT(DISTINCT g.permission_codes) as permissions " +
            "FROM sys_main_menu m " +
            "LEFT JOIN sys_menu_permission_group g ON m.id = g.main_menu_id " +
            "WHERE m.status = 1 " +
            "GROUP BY m.id, m.menu_code, m.menu_name, m.sort_order ")
    List<MenuVO> getAllMenusWithPermissions1();

    @Select("SET SESSION group_concat_max_len = 102400;SELECT m.id, m.menu_code, m.menu_name, m.sort_order,\n" +
            "    CONCAT('[',GROUP_CONCAT(CONCAT('{\"menuCode\":\"', g.menu_code,'\",\"menuName\":\"',g.sub_menu_name,'\",\"permissions\":\"', g.permission_codes,'\"}')),']') as subPermissions," +
            "    GROUP_CONCAT(DISTINCT g.permission_codes) as permissions\n" +
            "FROM sys_main_menu m \n" +
            "    LEFT JOIN sys_menu_permission_group g ON m.id = g.main_menu_id\n" +
            "WHERE m.status = 1\n" +
            "GROUP BY m.id, m.menu_code, m.menu_name, m.sort_order")
    @Results(id = "menuResultMap", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "menuCode", column = "menu_code"),
            @Result(property = "menuName", column = "menu_name"),
            @Result(property = "permissions", column = "permissions"),
            @Result(property = "subPermissions", column = "subPermissions",
                    typeHandler = SubMenuTypeHandler.class) // 使用自定义类型处理器
    })
    List<MenuVO> getAllMenusWithPermissions();


    @Select("SET SESSION group_concat_max_len = 102400; " +"SELECT " +
            "    m.id, " +
            "    m.menu_code, " +
            "    m.menu_name, " +
            "    m.sort_order, " +
            "    CONCAT('[', " +
            "        GROUP_CONCAT( " +
            "            CONCAT( " +
            "                '{\"menuCode\":\"', g.menu_code, " +
            "                '\",\"menuName\":\"', g.sub_menu_name, " +
            "                '\",\"permissions\":\"', g.permission_codes, " +
            "                '\"}' " +
            "            ) " +
            "        ), " +
            "    ']') as subPermissions, " +
            "    GROUP_CONCAT(DISTINCT g.permission_codes) as permissions " +
            "FROM " +
            "    sys_main_menu m " +
            "    LEFT JOIN sys_menu_permission_group g ON m.id = g.main_menu_id " +
            "WHERE " +
            "    m.status = 1 " +
            "GROUP BY " +
            "    m.id, " +
            "    m.menu_code, " +
            "    m.menu_name, " +
            "    m.sort_order")
    List<MenuVO> getMenuList();
}
