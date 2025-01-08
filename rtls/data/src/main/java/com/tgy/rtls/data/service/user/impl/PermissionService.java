package com.tgy.rtls.data.service.user.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.MenuVO;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.mapper.user.MemberMapper;
import com.tgy.rtls.data.mapper.user.PermissionMapper;
import com.tgy.rtls.data.service.user.PermissionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 许强
 * @Package com.tuguiyao.service.user
 * @date 2019/10/31
 */
@Service
@Transactional
public class PermissionService implements PermissionServices {
    @Autowired(required = false)
    private PermissionMapper permissionMapper;
    @Autowired(required = false)
    private MemberMapper memberMapper;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private LocalUtil localUtil;

    public List<Permission> findByAll() {
        return permissionMapper.findByAll(localUtil.getLocale());
    }

    public List<Permission> findByCidAll(Integer cid) {
        return permissionMapper.findByCidAll(cid, localUtil.getLocale());
    }

    public List<Permission> findByCidAll2(Integer cid, Integer uid) {
        List<Permission> permissions = permissionMapper.findByCidAll(cid, localUtil.getLocale());
        if (!CollectionUtils.isEmpty(permissions)) {
            permissionService.setMemberPermiss1(permissions, uid);
        }
        return permissionMapper.findByCidAll(cid, localUtil.getLocale());
    }

    public List<Permission> findByCid(Integer cid) {
        return permissionMapper.findByCid(cid, localUtil.getLocale());
    }

    public List<Permission> findByUid(Integer uid) {
        return permissionMapper.findByUid(uid, localUtil.getLocale());
    }

    public Boolean memberPermission(Integer uid, String permission_ids) {
        try {
            if (NullUtils.isEmpty(permission_ids)) {
                permissionMapper.delMemberPermission(String.valueOf(uid), null);
                return true;
            }
            String[] split = permission_ids.split(",");
            for (int i = 0; i < split.length; i++) {
                if (!NullUtils.isEmpty(split[i])) {
                    if (permissionMapper.findByMemberPermission(uid, split[i]) == 0) {
                        permissionMapper.insertMemberPermission(uid, split[i]);
                    }
                }
            }
            permissionMapper.delMemberPermission(String.valueOf(uid), split);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int setMemberPermiss(List<Integer> permissionIds, Integer uid) {
        try {
            permissionMapper.delMemberPermiss(uid);

            return permissionMapper.insertMemberPermiss(permissionIds, uid);

        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }

    public Boolean companyPermission(Integer cid, String permission_ids) {
        try {
            List<Member> members = memberMapper.findByCid(cid);
            if (NullUtils.isEmpty(permission_ids)) {
                permissionMapper.delCompanyPermission(String.valueOf(cid), null);
                for (Member member : members) {
                    permissionMapper.delMemberPermission(String.valueOf(member.getUid()), null);
                }
                return true;
            }
            String[] split = permission_ids.split(",");
            permissionMapper.delCompanyPermission(String.valueOf(cid), split);
            for (Member member : members) {
                permissionMapper.delMemberPermission(String.valueOf(member.getUid()), split);
            }
            for (String s : split) {
                if (!NullUtils.isEmpty(s)) {
                    if (permissionMapper.findByCompanyPermission(cid, Integer.valueOf(s)) == 0) {
                        permissionMapper.insertCompanyPermission(cid, Integer.valueOf(s));

                    }
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int setMemberPermiss1(List<Permission> permissionIds, Integer uid) {
        try {
            List<Integer> permissions = permissionIds.stream().map(Permission::getId).collect(Collectors.toList());
            permissionMapper.delMemberPermiss(uid);
            if(NullUtils.isEmpty(permissions)){
                return 0;
            }else{
                return permissionMapper.insertMemberPermiss1(uid, permissions);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }
    }
    /*
     * 新增单一权限
     * */

    public Boolean insertMemberPermission(Integer uid, String permissionid) {
        return permissionMapper.insertMemberPermission(uid, permissionid) > 0;
    }

    /*
     * 新增登录人员项目权限
     * */
    public void insertMemberProject(Integer uid, Integer cid, Integer pid) {
        permissionMapper.insertMemberProject(uid, pid);
        permissionMapper.insertCompanyProject(cid, pid);
    }

    /*
     * 删除单一权限
     * */
    public Boolean delMemberPermission(Integer uid, String permissionid) {
        return permissionMapper.delMemberPermissionId(uid, permissionid) > 0;
    }

    public Boolean delMemberPermissions(Integer uid) {
        return permissionMapper.delMemberPermissionIds(uid) > 0;
    }

    List findRolePermissions(Integer company) {
        return permissionMapper.findRolePermissions(company);
    }

    @Override
    public Set<String> getPermByUserId(Integer uid) {
        return permissionMapper.getPermByUId(uid);
    }

    @Override
    public List<Permission> getPermisByUser(Integer uid) {
        return permissionMapper.getPermisByUser(uid);
    }

    @Override
    public List<MenuVO> getAllMenuPermissions() {
        return permissionMapper.getAllMenuPermissions();
    }

    @Override
    public String getMainMenuPermissions(Long id) {
        return permissionMapper.getMainMenuPermissions(id);
    }

    @Override
    public List<MenuVO> getAllMenusWithPermissions() {
        return permissionMapper.getAllMenusWithPermissions();
    }
}
