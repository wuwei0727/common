package com.tgy.rtls.data.service.user.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.user.Company;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.mapper.user.CompanyMapper;
import com.tgy.rtls.data.mapper.user.MemberMapper;
import com.tgy.rtls.data.mapper.user.PermissionMapper;
import com.tgy.rtls.data.service.common.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.service.user
 * @date 2019/10/23
 */
@Service
@Transactional
public class CompanyService {
    @Autowired(required = false)
    private CompanyMapper companyMapper;
    @Autowired(required = false)
    private MemberMapper memberMapper;
    @Autowired(required = false)
    private PermissionMapper permissionMapper;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LocalUtil localUtil;
    @Autowired
    private RedisService redisService;

    public List<Member> getMemberByCid(String[] cids){
        List<Member> memberByCid = companyMapper.getMemberByCid(cids);
        for (Member member:memberByCid) {
            companyMapper.updateDisabledMemberByCid(member.getCid());
        }
        return memberByCid;
    }


    /*
     * 权限管理部门信息查询
     * */
    public List<Company> findByAll(String keywrod, Integer enabled, String cid, String desc,Integer createuId) {
        return companyMapper.findByAll(keywrod, enabled, cid, desc);
    }

    public List<Company> getCreateByuid(Integer uid) {
        return companyMapper.getCreateByuid(uid);
    }

    public List<Company> findByAll2(String keywrod, Integer enabled, String cid, String desc,Integer createuId,String cname) {
        return companyMapper.findByAll2(keywrod, enabled, cid, desc,createuId,cname);
    }

    public List<Member> findMemberByCid(Integer cid){
        return companyMapper.findMemberByCid(cid);
    }
    
    /*
     * 通过部门id查询部门名
     * */
    public String findByNameId(String ids){
        String[] split=ids.split(",");
        return companyMapper.findByNameId(split);
    }

    /*
     * 权限管理部门详情
     * */
    public Company findById(Integer id) {
        Company company = companyMapper.findById(id);
        if (company != null) {
            List<Permission> permissions = permissionMapper.findByCid(id,localUtil.getLocale());
            if (permissions != null) {
                company.setPermissions(permissions);
            }
        }
        return company;
    }

    public Company findByName(String cname) {
        return companyMapper.findByName(cname);
    }

    public Boolean insertCompany(Company company) {
        return companyMapper.insertCompany(company) > 0;
    }

    public Boolean updateCompany(Company company) {
        return companyMapper.updateCompany(company) > 0;
    }

    public List<Member> findMember(Integer cid) {
        return companyMapper.findMember(cid);
    }

    public Integer UpdateMember(Integer uid,Integer enabled) {
        return companyMapper.updateMember(uid,enabled);
    }

    public Integer delCompany(String id) {
        String[] split = id.split(",");
        int num = 0;
        for (String s : split) {
            //解除和部门绑定的权限 实例关系
            permissionMapper.delCompanyPermission(s, null);
            companyMapper.delCompany_project(Integer.valueOf(s), null);
            if (companyMapper.delCompany(s) > 0) {
                //查询该权限组下有哪些人
                String ids=memberMapper.findByMemberCid(s);
                if (!NullUtils.isEmpty(ids)) {
                    memberService.delMember(ids);
                }
                num++;
            }
        }
        return num;
    }
    public Integer delCompany1(String id) {
        String[] split = id.split(",");
        int num = 0;
        for (String s : split) {
            //解除和部门绑定的权限 实例关系
            permissionMapper.delCompanyPermission(s, null);
            //解除和部门绑定的权限 实例关系
            if (companyMapper.delCompany(s) > 0) {
                num++;
            }
        }
        return num;
    }
    public void updateCid(Integer cid, String project_ids) {
        try {
            List<Member> members = memberMapper.findByCid(cid);
            if (NullUtils.isEmpty(project_ids)) {
                companyMapper.delCompany_project(cid, null);
                for (Member member : members) {
                    memberMapper.delMember_project(String.valueOf(member.getUid()), null);
                }
                return;
            }
            String[] split = project_ids.split(",");
            companyMapper.delCompany_project(cid, split);
            for (Member member : members) {
                memberMapper.delMember_project(String.valueOf(member.getUid()), split);
                try {
                    redisService.remove("instance::" + member.getUid());
                }catch (Exception e){

                }
            }
            for (String s : split) {
                if (companyMapper.findByCid(String.valueOf(cid), s) == 0) {
                    companyMapper.insertCompany_project(String.valueOf(cid), s);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
