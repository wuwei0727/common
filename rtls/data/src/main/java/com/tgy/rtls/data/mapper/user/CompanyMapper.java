package com.tgy.rtls.data.mapper.user;

import com.tgy.rtls.data.entity.user.Company;
import com.tgy.rtls.data.entity.user.Member;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.dao.user
 * @date 2019/10/22
 */
public interface CompanyMapper {
    /*
    * 权限管理-->部门信息查询
    * */
    List<Company> findByAll(@Param("keyword") String keyword, @Param("enabled") Integer enabled, @Param("cid") String cid, @Param("desc") String desc);

    List<Company> findByAll2(@Param("keyword") String keyword, @Param("enabled") Integer enabled, @Param("cid") String cid, @Param("desc") String desc,@Param("createuId") Integer createuId,@Param("cname") String cname);

    List<Company> getCreateByuid(@Param("uid")Integer uid);

    List<Member> findMemberByCid(@Param("cid")Integer cid);
    /*
    * 通过部门id查询部门名
    * */
    String findByNameId(@Param("ids")String[] ids);
    /*
    * 权限管理-->部门详情
    * */
    Company findById(@Param("id") Integer id);
    /*
     * 权限管理-->部门重名判断
     * */
    Company findByName(@Param("cname") String cname);
    /*
     * 权限管理-->新增部门
     * */
    int insertCompany(@Param("company") Company company);
    /*
     * 权限管理-->修改部门
     * */
    int updateCompany(@Param("company") Company company);
    /*
     * 权限管理-->删除部门
     * */
    int delCompany(@Param("id") String id);

    int delCompany_project(@Param("cid") Integer cid, @Param("project_ids") String[] project_ids);

    int delCompany_projectId(@Param("project_id") String project_id);

    int findByCid(@Param("cid") String cid, @Param("pid") String pid);

    int insertCompany_project(@Param("cid") String cid, @Param("pid") String pid);

    List<Member> findMember(@Param("cid") Integer cid);

    Integer updateMember(@Param("uid") Integer uid,@Param("enabled")Integer enabled);

    List<Member> getMemberByCid(@Param("cid") String[] cid);

    void updateDisabledMemberByCid(Integer cid);
}
