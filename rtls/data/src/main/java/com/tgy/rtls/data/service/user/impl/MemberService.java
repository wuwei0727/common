package com.tgy.rtls.data.service.user.impl;

import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.Company;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.mapper.user.CompanyMapper;
import com.tgy.rtls.data.mapper.user.MemberMapper;
import com.tgy.rtls.data.mapper.user.PermissionMapper;
import com.tgy.rtls.data.tool.IpInf;
import net.sf.json.JSONObject;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.CollectionUtils;

import java.net.InetAddress;
import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.service.user
 * @date 2019/10/23
 */
@Service
@Transactional
public class MemberService {
    @Autowired(required = false)
    private MemberMapper memberMapper;
    @Autowired(required = false)
    private PermissionMapper permissionMapper;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private LocalUtil localUtil;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * 加密类型
     */
    @Value("${rtls.shiro.algorithmName}")
    private String algorithmName;

    /**
     * 加密次数
     */
    @Value("${rtls.shiro.hashIterations}")
    private Integer hashIterations;
    @Value("${rtls.shiro.salt}")
    private String salt;

    /*
     *成员信息查询
     * */
    public List<Member> findByAll(String keyword, Integer enabled, String cid, String desc) {
        return memberMapper.findByAll(keyword, enabled, cid, desc);
    }

    public List<Member> findByAll2(String keyword, Integer enabled, String cid, String desc, String cname, String[] maps) {
        return memberMapper.findByAll2(keyword, enabled, cid, desc, cname, maps);
    }

    /*
     * 根据成员id查询成员名
     * */
    public String findByNameId(String ids) {
        String[] split = ids.split(",");
        return memberMapper.findByNameId(split);
    }

    /*
     * 成员信息详情
     * */
    public Member findById(Integer id) {
        Member member = memberMapper.findById(id);
        if (member != null) {
            List<Permission> permissions = permissionMapper.findByUid(id, localUtil.getLocale());
            if (permissions != null) {
                member.setPermissions(permissions);
            }
            List<Map_2d> mapName = memberMapper.getMapName(id);
            member.setMapList(mapName);

        }
        return member;
    }

    /**
     * 根据人员id查找
     * @param cid
     * @param uid
     * @return
     */
    public Member findById(Integer cid, Integer uid) {
        Member member = memberMapper.findById(uid);
        if (member != null) {
            List<Permission> permissions = permissionMapper.findByCidAll(cid, localUtil.getLocale());
            if (!CollectionUtils.isEmpty(permissions)) {
                member.setPermissions(permissions);
            }

            List<Map_2d> mapName = memberMapper.getMapName(uid);
            member.setMapList(mapName);

        }
        return member;
    }

    /**
     * 根据人员id查找权限
     * @param cid
     * @param uid
     * @return
     */
    public Member findPermissionById(Integer cid, Integer uid) {
        Member member = memberMapper.findById(uid);
        List<Permission> memberPermissons = memberMapper.getMemberPermissons(member.getUid());
        if (member != null) {
            List<Permission> permissions = permissionMapper.findByCidAll(cid, localUtil.getLocale());
            List<Permission> permissions2 = permissionMapper.findByUid(cid,localUtil.getLocale());
            if(CollectionUtils.isEmpty(memberPermissons)){
                member.setPermissions(permissions);
                if(!CollectionUtils.isEmpty(memberPermissons)){
                    permissionService.setMemberPermiss1(permissions2, uid);
                }else {
                    if(CollectionUtils.isEmpty(permissions2)){
                        member.setPermissions(permissions2);
                        permissionService.setMemberPermiss1(permissions2, uid);
                    }else {
                        permissionService.setMemberPermiss1(permissions, uid);
                    }
                }
            }else{
                member.setPermissions(memberPermissons);
                permissionService.setMemberPermiss1(memberPermissons, uid);
            }

            List<Map_2d> mapName = memberMapper.getMapName(uid);
            member.setMapList(mapName);

        }
        return member;
    }

    public List<Map_2d> getMapName(Integer uid) {
        return memberMapper.getMapName(uid);
    }

    //获取地图管理所有地图不包括禁用
    public List<Map_2d> getMapId(Integer uid) {

        return memberMapper.getMapId(uid);
    }

    //获取地图管理所有地图包括禁用
    public List<Map_2d> getMapIdAll(Integer uid) {
        return memberMapper.getMapIdAll(uid);
    }

    public List<Map_2d> getMapId2(Integer userId) {
        return memberMapper.getMapId2(userId);
    }

    public Member findByName(String membername) {
        return memberMapper.findByName(membername);
    }

    public Member findByPhone(String phone) {
        Member member = memberMapper.findByPhone(phone);
        if (member != null) {
            List<Permission> permissions = permissionMapper.findByUid(member.getUid(), localUtil.getLocale());
            if (permissions != null) {
                member.setPermissions(permissions);
            }
        }
        return member;
    }

    public Boolean insertMember(Member member) {
        return memberMapper.insertMember(member) > 0;
    }

    public Boolean insertMember1(Member member) {
        Company c1 = companyMapper.findById(member.getCid());
        member.setEnabled(c1.getEnabled());
        member.setRawPassword(member.getPassword());
        member.setPassword(new SimpleHash(algorithmName, member.getPassword(), salt, hashIterations).toString());
        return memberMapper.insertMember(member) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public int insertMemberMap(Integer uid, String mapid) {
        try {
            return memberMapper.insertMemberMap(uid, mapid);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }

    }

    public Boolean updateMember(Member member) {
        member.setRawPassword(member.getPassword());
        if(!NullUtils.isEmpty(member.getPassword()) || !"".equals(member.getPassword())){
            member.setPassword(new SimpleHash(algorithmName, member.getPassword(), salt, hashIterations).toString());
        }
        return memberMapper.updateMember(member) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public int delMemberMap(List<String> mapIds, Integer uid) {
        try {
            //修改人员 先清空
            memberMapper.delMemberMap(uid);
            //重新插入
            return memberMapper.addMemberMap(mapIds, uid);
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return -1;
        }

    }

    /*
     * 修改成员密码
     * */
    public boolean updatePassword(Integer uid, String password) {
        return memberMapper.updatePassword(uid, password) > 0;
    }

    /*
     * 修改成员登录时间
     * */
    public boolean updateAddTime(Integer uid, String loginTime) {
        return memberMapper.updateAddTime(uid, loginTime) > 0;
    }

    /*
     * 删除成员信息
     * */
    public Integer delMember(String ids) {
        String[] split = ids.split(",");
        int num = 0;
        for (String s : split) {
            //删除成员绑定的权限
            permissionMapper.delMemberPermission(s, null);
            //删除成员绑定的实例
            memberMapper.delMember_project(s, null);
            memberMapper.delMemberMap(Integer.valueOf(s));
            if (memberMapper.delMember(s) > 0) {
                num++;
            }
        }
        return num;
    }

    public void updateUid(Integer uid, String project_ids) {
        try {
            //修改成员绑定的项目
            if (NullUtils.isEmpty(project_ids)) {
                memberMapper.delMember_project(String.valueOf(uid), null);
                return;
            }
            String[] split = project_ids.split(",");
            memberMapper.delMember_project(String.valueOf(uid), split);
            for (String s : split) {
                //如果该成员没有绑定这个实例则添加
                if (memberMapper.findByUid(uid, Integer.valueOf(s)) == 0) {
                    memberMapper.insertMember_project(uid, Integer.valueOf(s));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 登录记录查询 phone-->登录手机号  startTime，endTime登录时间区间
     * */
    public List<LoginRecord> findByLonginRecord(String phone, String startTime, String endTime) {
        String beginDate = "";
        String endDate = "";
        if (startTime != null && endTime != null) {
            beginDate = startTime.replace("00:00", "00:00:00");
            endDate = endTime.replace("24:00", "23:59:59");
        }
        return memberMapper.findByLonginRecord(phone, beginDate, endDate);
    }

    public List<LoginRecord> findByLonginRecord2(String phone, String startTime, String endTime,Integer uid) {
        String beginDate = "";
        String endDate = "";
        if (startTime != null && endTime != null) {
            beginDate = startTime.replace("00:00", "00:00:00");
            endDate = endTime.replace("24:00", "23:59:59");
        }
        return memberMapper.findByLonginRecord2(phone, beginDate, endDate,uid);
    }

    /*
     * 新增登录记录
     * */
    public boolean addLonginRecord(Integer uid,String phone, String ip) {
        try {
            LoginRecord login = new LoginRecord();
            login.setUid(uid);
            InetAddress address = InetAddress.getLocalHost();//获取的是本地的IP地址
            //String  = address.getHostAddress();
            login.setIp(ip);

            String res = IpInf.sendGet(IpInf.getIpLocationInf(ip));
            JSONObject json = JSONObject.fromObject(res);
            //IpLocation ipLocation=(IpLocation)JSONObject.toBean(json, IpLocation.class);
            // login.setAddress(ipLocation.getProvince()+","+ipLocation.getCity());
            String city = json.getString("city");
            String Province = json.getString("province");
            if (city.equals("[]")) {
                city = "";
            }
            login.setAddress(ip + "," + Province + " " + city);
            login.setPhone(phone);
            return memberMapper.addLonginRecord(login) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //查询人员有哪些地图
    public List<Map_2d> getMemberMap(Integer uid) {
        return memberMapper.getMemberMap(uid);
    }
}
