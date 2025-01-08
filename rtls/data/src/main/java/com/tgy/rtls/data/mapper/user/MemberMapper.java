package com.tgy.rtls.data.mapper.user;

import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.dao.user
 * @date 2019/10/21
 */
public interface MemberMapper {
    /*
     * 权限管理-->成员信息查询
     * */
    List<Member> findByAll(@Param("keyword") String keyword, @Param("enabled") Integer enabled, @Param("cid") String cid, @Param("desc") String desc);

    List<Member> findByAll2(@Param("keyword") String keyword, @Param("enabled") Integer enabled, @Param("cid") String cid, @Param("desc") String desc, @Param("cname") String cname, @Param("maps") String[] maps);

    /*
     * 登录记录查询 phone-->登录手机号  startTime，endTime登录时间区间
     * */
    List<LoginRecord> findByLonginRecord(@Param("phone") String phone, @Param("startTime") String startTime, @Param("endTime") String endTime);
    List<LoginRecord> findByLonginRecord2(@Param("phone") String phone, @Param("startTime") String startTime, @Param("endTime") String endTime,@Param("uid")Integer uid);

    /*
     * 新增登录记录
     * */
    int addLonginRecord(@Param("login") LoginRecord login);

    /*
     * 根据成员id查询成员名
     * */
    String findByNameId(@Param("ids") String[] ids);

    /*
     * 权限管理-->部门下成员信息查询
     * */
    List<Member> findByCid(@Param("cid") Integer cid);

    /*
     * 权限管理-->成员信息详情
     * */
    Member findById(@Param("id") Integer id);

    /*
     * 权限管理-->成员重名判断
     * */
    Member findByName(@Param("membername") String membername);

    /*
     * 权限管理-->成员手机号重复判断
     * */
    Member findByPhone(@Param("phone") String phone);

    /*
     * 权限管理-->新增成员
     * */
    int insertMember(@Param("member") Member member);

    /**
     * 添加用户
     * @param mapids 地图id
     * @param uid    用户id
     * @return
     */

    int insertMemberMap(@Param("uid") Integer uid, @Param("mapids") String mapids);

    //修改人员 先清空 --->然后插入
    int delMemberMap(Integer uid);

    int addMemberMap(@Param("mapIds") List<String> mapIds, @Param("uid") Integer uid);

    List<Map_2d> getMapName(@Param("uid") Integer uid);

    List<Map_2d> getMapId(@Param("uid") Integer uid);

    List<Map_2d> getMapIdAll(@Param("uid") Integer uid);

    List<Map_2d> getMapId2(@Param("userId") Integer userId);

    /*
     * 权限管理-->修改成员
     * */
    int updateMember(@Param("member") Member member);

    /*
     * 修改成员密码
     * */
    int updatePassword(@Param("uid") Integer uid, @Param("password") String password);

    /*
     * 修改成员登录时间
     * */
    int updateAddTime(@Param("uid") Integer uid, @Param("loginTime") String loginTime);

    /*
     * 权限管理-->删除成员
     * */
    int delMember(@Param("id") String id);

    /*
     * 查询权限组下的人员id
     * */
    String findByMemberCid(@Param("cid") String cid);

    int delMember_project(@Param("uid") String uid, @Param("project_ids") String[] project_ids);

    int delMember_projectId(@Param("project_id") Integer project_id);

    int findByUid(@Param("uid") Integer uid, @Param("pid") Integer pid);

    int insertMember_project(@Param("uid") Integer uid, @Param("pid") Integer pid);

    //查询人员有哪些地图
    List<Map_2d> getMemberMap(Integer uid);

    /**
     * 获取人员有哪些权限
     */
    List<Permission> getMemberPermissons(Integer uId);
}
