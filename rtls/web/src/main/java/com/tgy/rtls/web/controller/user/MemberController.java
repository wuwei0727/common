package com.tgy.rtls.web.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.ByteUtils;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.LoginRecord;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.MemberService;
import com.tgy.rtls.data.service.user.impl.PermissionService;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.web.util.PinyinUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 许强
 * @Package com.tuguiyao.controller.user
 * @date 2019/10/25
 */
@RequestMapping(value = "/member")
@CrossOrigin
@RestController
/**
 * 人员账户管理
 */
public class MemberController {
    @Autowired
    private MemberService memberService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @MyPermission
    @RequestMapping(value = "/getMemberSel")
    @ApiOperation(value = "权限管理成员查询接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "keyword", value = "关键字", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enabled", value = "是否启用", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getMemberSel(String keyword,
                                             @RequestParam(value = "desc ", defaultValue = "addTime desc") String desc,
                                             Integer enabled,
                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                             @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize, String cname, String maps) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            Integer cid = member.getUid();
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize < 0) {
                List<Member> members = memberService.findByAll2(keyword, enabled, String.valueOf(cid), desc, cname, mapids);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), members);
            }
            int num = memberService.findByAll2(keyword, enabled, String.valueOf(cid), desc, cname, mapids).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<Member> members = memberService.findByAll2(keyword, enabled, String.valueOf(cid), desc, cname, mapids);
            PageInfo<Member> pageInfo = new PageInfo<>(members);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());

            operationlogService.addOperationlog(member.getUid(), LocalUtil.get(KafukaTopics.QUERY_PERSONPERMISSION));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getLoginRecordSel")
    @ApiOperation(value = "登录日志", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "phone", value = "手机号", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "startTime", value = "开始时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "endTime", value = "结束时间", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getLoginRecordSel(String phone, String startTime, String endTime,
                                                  @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                                  @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            Integer uid = null;
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            if (pageSize < 0) {
                List<LoginRecord> loginRecords = memberService.findByLonginRecord2(phone, startTime, endTime,uid);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), loginRecords);
            }
            int num = memberService.findByLonginRecord(phone, startTime, endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<LoginRecord> loginRecords = memberService.findByLonginRecord2(phone, startTime, endTime,uid);
            PageInfo<LoginRecord> pageInfo = new PageInfo<>(loginRecords);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            if (uid == null) {
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
            }
            operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.QUERY_LOGINLOG));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"mer:see","mer:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getMemberId")
    @ApiOperation(value = "权限管理成员详情接口", notes = "成员id")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "成员id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "cid", value = "角色id", required = true, dataType = "Integer")
    })
    public CommonResult<Object> getMemberId(@RequestParam("id") Integer uid, @RequestParam("cid") Integer cid) {
        try {
            Member member = memberService.findById(cid, uid);
            List<Permission> permissions = permissionService.findByCidAll(cid);
            List<Instance> projects = instanceService.findByCid(member.getCid());
            List<Instance> project1 = instanceService.findByUid(uid);
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("msg", LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            map.put("list", member);
            map.put("allPermission", permissions);//成员可选择的权限
            map.put("allProject", PinyinUtils.pinyinInstance(projects));//成员可选择的实例
            map.put("projects", project1);//成员已拥有的实例
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    @RequestMapping(value = "/getMemberPermissons")
    @ApiOperation(value = "获取人员权限", notes = "成员id")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", name = "id", value = "成员id", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "cid", value = "角色id", required = true, dataType = "Integer")
    })
    public CommonResult<Object> getMemberPermissons(@RequestParam("id") Integer uid,Integer cid) {
        try {
            Member member = memberService.findPermissionById(cid, uid);
            List<Permission> permissions = permissionService.findByCidAll(cid);
            List<Instance> projects = instanceService.findByCid(member.getCid());
            List<Instance> project1 = instanceService.findByUid(uid);
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("msg", LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            map.put("list", member);
            map.put("allPermission", permissions);//成员可选择的权限
            map.put("allProject", PinyinUtils.pinyinInstance(projects));//成员可选择的实例
            map.put("projects", project1);//成员已拥有的实例
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
    /**
     * 编辑用户权限
     */
    @RequiresPermissions("mer:pss")
    @RequestMapping(value = "/memberPermission")
    @ApiOperation(value = " 编辑用户权限接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "uid", value = "成员id", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "permission_ids", value = "权限id集", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "project_ids", value = "实例id集", required = false, dataType = "String")
    })
    public CommonResult<Integer> memeberPermission(Integer uid, String permissionIds, String projectIds,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            Subject subject = SecurityUtils.getSubject();
            String[] split = Optional.ofNullable(permissionIds).orElse("").split(",");
            List<Integer> permissIdList;
            if (!NullUtils.isEmpty(split)) {
                permissIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                int i = permissionService.setMemberPermiss(permissIdList, uid);
                if (i > -1) {
                    memberService.updateUid(uid, projectIds);
                    Member member1 = (Member) subject.getPrincipal();
                    List<Permission> permissions = permissionService.findByUid(uid);
                    member1.setPermissions(permissions);
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("edit权限设置"), now);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                } else {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
                }
            } else {
                if (permissionService.delMemberPermissions(uid)) {
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("delete权限设置"), now);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                } else {
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"mer:del"})
    @RequestMapping(value = "/delMember/{ids}")
    @ApiOperation(value = "权限管理成员删除接口", notes = "成员id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "成员id集", required = true, dataType = "String")
    public CommonResult<Integer> delMember(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            String memberId = String.valueOf(member.getUid());
            String[] split = ids.split(",");
            int num = 0;
            for (String s : split) {
                if(memberId.equals(s)){
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.delSelectHaveYourAccoutCannotDel));
                }
            }
                num = memberService.delMember(ids);
            Integer uid = null;

            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            if (num > 0) {
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.DELETE_PERSONPERMISSION) + memberService.findByNameId(ids));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS) + num + LocalUtil.get(KafukaTopics.N_COUNTINFO));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequiresPermissions({"mer:add"})
    @RequestMapping(value = "/addMember")
    @ApiOperation(value = "权限管理成员新增接口", notes = "部门信息")
    public CommonResult<Integer> addMember(Member member, String mapid, Integer uid,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (member.getPhone() == null || !ByteUtils.isPhoneLegal(member.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }

            if (member.getPassword() == null || member.getPassword().length() < 6) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            }
            Member member2 = memberService.findByPhone(member.getPhone());
            if (!NullUtils.isEmpty(member2)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            Member member1 = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member1)) {
                uid = member1.getUid();
            }
//            mapid="75";
            String[] spilt = mapid.split(",");
            member.setCreatorId(String.valueOf(member1.getUid()));
            if (memberService.insertMember1(member)) {
                for (String str : spilt) {
                    memberService.insertMemberMap(member.getUid(), str);
                }
//                打开shiro才开
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.ADD_PERSONPERMISSION) + member.getMembername());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(uid,ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), member.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequiresPermissions(value = {"mer:edit"})
    @RequestMapping(value = "/updateMember")
    @ApiOperation(value = "权限管理成员修改接口", notes = "部门信息")
    public CommonResult<Integer> update(Member member, String mapid, Integer uid,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            if (member.getPhone() == null || !ByteUtils.isPhoneLegal(member.getPhone())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_ERROR));
            }
            Member member1 = memberService.findById(member.getUid());
//            Member member2 = memberService.findByPhone2(member.getPhone(),member1.getUid());
            Member member2 = memberService.findByPhone(member.getPhone());
            if (!member1.getPhone().equals(member.getPhone()) && member2 != null) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
            }
            //if (member.getPassword() == null || member.getPassword().length() < 6) {
            //    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            //}
//            Member memberPhone = memberService.findByPhone(member.getPhone());
//            if (!NullUtils.isEmpty(memberPhone)) {
//                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PHONENUM_EXIST));
//            }

            Member member3 = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member3)) {
                uid = member3.getUid();
            }
            String[] split = mapid.split(",");
            if (!NullUtils.isEmpty(split)) {
                memberService.delMemberMap(Arrays.asList(split), member.getUid());
            }

            if (memberService.updateMember(member)) {
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.UPDATE_PERSONPERMISSION) + member.getMembername());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(uid,ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MEMBER_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), member.getUid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/getCurrentMember")
    @ApiOperation(value = "修改当前人员信息", notes = "人员信息")
    public CommonResult<Object> getCurrentUser() {
        try {
            Member sysUser = (Member) SecurityUtils.getSubject().getPrincipal();
            Map<String, Object> map = new HashMap<>();
            map.put("status", true);
            map.put("msg", LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
            map.put("list", sysUser);

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /**
     * 开发者模式开启/关闭
     */
    @RequestMapping(value = "/developerPattern")
    @ApiOperation(value = "开发模式开启接口", notes = "")
    public CommonResult<String> developerPattern(boolean open, String password) {
        try {
            if (open) {//开启
                //1.判断密码
                if (!password.equals("tgy88888888")) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_ERROR));
                }
                //2.修改当前登录人员的开发者权限
                Integer uid = 1;
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                if (!NullUtils.isEmpty(member)) {
                    uid = member.getUid();
                }
                if (permissionService.insertMemberPermission(uid, "58")) {
                    //
                    Member member1 = (Member) SecurityUtils.getSubject().getPrincipal();
                    List<Permission> permissions = permissionService.findByUid(member.getUid());
                    member1.setPermissions(permissions);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPEN_SUCCESS));
                }
            } else {//关闭
                //1.删除当前登录人员的开发者权限
                Integer uid = 1;
                Member member = (Member) SecurityUtils.getSubject().getPrincipal();
                if (!NullUtils.isEmpty(member)) {
                    uid = member.getUid();
                }
                permissionService.delMemberPermission(uid, "58");
                Member member1 = (Member) SecurityUtils.getSubject().getPrincipal();
                List<Permission> permissions = permissionService.findByUid(member.getUid());
                member1.setPermissions(permissions);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.CLOSE_SUCCESS));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.FAIL));
    }

    /*
     * 修改当前登录成员密码
     * */
    @RequestMapping(value = "/updatePassword")
    @ApiOperation(value = "修改密码", notes = "输入密码")
    public CommonResult<String> updatePassword(String oldPword, String newPword) {
        try {
            //与当前登录信息对比 判断oldPassword是否正确
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!member.getPassword().equals(oldPword)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_ERROR));
            }
            //密码正确修改当前登录人员的密码
            if (memberService.updatePassword(member.getUid(), newPword)) {
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

}
