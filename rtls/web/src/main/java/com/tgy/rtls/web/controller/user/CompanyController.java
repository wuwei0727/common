package com.tgy.rtls.web.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.*;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.CompanyService;
import com.tgy.rtls.data.service.user.impl.PermissionService;
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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 许强
 * @Package com.tuguiyao.controller.user
 * @date 2019/10/28
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/company")
/**
 * 账户权限管理
 */
public class CompanyController {
    @Autowired
    private CompanyService companyService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private InstanceService instanceService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @RequestMapping(value = "/getCompanySel")
    @ApiOperation(value = "权限管理部门查询接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "keyword", value = "关键字", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enabled", value = "是否启用", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getCompanySel(String keyword, @RequestParam(value = "desc", defaultValue = "addTime desc") String desc,
                                              Integer enabled, @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                              @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize, String cname, String maps) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            List<Company> createBy = companyService.getCreateByuid(member.getUid());
            if (NullUtils.isEmpty(createBy)) {
                return new CommonResult<>(400, LocalUtil.get("你没有在当前账号创建权限组！！！"));
            }
            String cid = null;
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            if (pageSize < 0) {
                List<Company> companyList = companyService.findByAll2(keyword, enabled, cid, desc, createBy.get(0).getCreateuId(), cname);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), companyList);
            }
            int num = companyService.findByAll2(keyword, enabled, cid, desc, createBy.get(0).getCreateuId(), cname).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<Company> companyList = companyService.findByAll2(keyword, enabled, cid, desc, createBy.get(0).getCreateuId(), cname);
            PageInfo<Company> pageInfo = new PageInfo<>(companyList);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            Integer uid = null;
            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }

           // operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.QUERY_GROUPPERMISSION));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"rle:see","rle:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getCompanyId/{id}")
    @ApiOperation(value = "权限管理部门详情接口", notes = "部门id")
    @ApiImplicitParam(paramType = "path", name = "id", value = "部门id", required = true, dataType = "int")
    public CommonResult<Object> getCompanyId(@PathVariable("id") Integer id) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            Company company = companyService.findById(id);
            //List<Permission> permissions = permissionService.findByAll();
            List<Permission> permissions = permissionService.getPermisByUser(member.getUid());
            //查角色有哪些权限
            List<Instance> projects = instanceService.findByCid(id);
            Map<String, Object> map = new HashMap<>();
            map.put("list", company);
            map.put("allProject", PinyinUtils.pinyinInstance(instanceService.findByAll(null, null)));
            map.put("allPermission", permissions);
            map.put("projects", projects);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPermissionsCompanyById/{id}")
    @ApiOperation(value = "权限管理部门详情接口", notes = "部门id")
    @ApiImplicitParam(paramType = "path", name = "id", value = "部门id", required = true, dataType = "int")
    public CommonResult<Object> getPermissionsCompanyById(@PathVariable("id") Integer id) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            Company company = companyService.findById(id);
            //List<Permission> permissions = permissionService.findByAll();
            List<Permission> permissions = permissionService.getPermisByUser(member.getUid());
            //查角色有哪些权限
            List<Instance> projects = instanceService.findByCid(id);
            Map<String, Object> map = new HashMap<>();
            map.put("list", company);
            map.put("allProject", PinyinUtils.pinyinInstance(instanceService.findByAll(null, null)));
            map.put("allPermission", permissions);
            map.put("projects", projects);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"rle:add"})
    @RequestMapping("/addCompany")
    @ApiOperation(value = "权限管理部门新增接口", notes = "部门信息")
    public CommonResult<Integer> addCompany(Company company, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (companyService.findByName(company.getCname()) != null) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EXIST));
            }
            Integer uid = null;

            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            company.setCreateuId(member.getUid());
            if (companyService.insertCompany(company)) {
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.ADD_GROUPPERMISSION) + company.getCname());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.ROLE_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), company.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequiresPermissions({"rle:edit"})
    @RequestMapping("/updateCompany")
    @ApiOperation(value = "权限管理部门修改接口", notes = "部门信息")
    public CommonResult<Integer> updateCompany(Company company,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Company c1 = companyService.findById(company.getId());
            if (companyService.findByName(company.getCname()) != null && !c1.getCname().equals(company.getCname())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EXIST));
            }
            Integer uid = null;
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            if (companyService.updateCompany(company)) {
                List<Member> member1 = companyService.findMember(c1.getId());
                for (Member member2 : member1) {
                    companyService.UpdateMember(member2.getUid(), company.getEnabled());
                }
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.ROLE_INFO)), now);
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.UPDATE_GROUPPEMISSION) + company.getCname());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), company.getId());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequiresPermissions({"rle:del"})
    @RequestMapping(value = "/delCompany/{ids}")
    @ApiOperation(value = "权限管理部门删除接口", notes = "部门id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "部门id集", required = true, dataType = "String")
    public CommonResult<Integer> delCompany(@PathVariable("ids") String ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
//            int num=companyService.delCompany(ids);
            int num = companyService.delCompany1(ids);
            Integer uid = null;
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            if (num > 0) {
                companyService.getMemberByCid(ids.split(","));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.ROLE_INFO)), now);
                operationlogService.addOperationlog(uid, LocalUtil.get(KafukaTopics.DELETE_GROUPPERMISSION) + companyService.findByNameId(ids));
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS) + num + LocalUtil.get(KafukaTopics.N_COUNTINFO));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequiresPermissions({"rle:pss"})
    @RequestMapping(value = "/companyPermission")
    @ApiOperation(value = "权限组权限设置接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "cid", value = "部门id", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "permission_ids", value = "权限id集", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "project_ids", value = "实例id集", required = false, dataType = "String")
    })
    public CommonResult<Integer> companyPermission(Integer cid, String permission_ids, String project_ids,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            Subject subject = SecurityUtils.getSubject();
            Company c1 = companyService.findById(cid);
            List<Member> member1 = companyService.findMember(c1.getId());
            if (NullUtils.isEmpty(member1)) {
                if (permissionService.companyPermission(cid, permission_ids)) {
                    companyService.updateCid(cid, project_ids);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                }
            }
            String[] split1 = permission_ids.split(",");
            boolean str = Arrays.asList(split1).contains("");
            if (!NullUtils.isEmpty(member1) && !str) {
                if (permissionService.companyPermission(cid, permission_ids)) {
                    List<Integer> permissIdList = Stream.of(split1).map(Integer::parseInt).collect(Collectors.toList());
                    permissionService.setMemberPermiss(permissIdList, member1.get(0).getUid());

                    companyService.updateCid(cid, project_ids);
                    String ip = IpUtil.getIpAddr(request);
                    String address = ip2regionSearcher.getAddressAndIsp(ip);
                    operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("edit账户权限"), now);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                }
            } else {
                String[] split = permission_ids.split(",");
                if (!NullUtils.isEmpty(split)) {
                    List<Integer> permissIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
                    if (permissionService.companyPermission(cid, permission_ids)) {
                        permissionService.setMemberPermiss(permissIdList, member1.get(0).getUid());

                        companyService.updateCid(cid, project_ids);
                        String ip = IpUtil.getIpAddr(request);
                        String address = ip2regionSearcher.getAddressAndIsp(ip);
                        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("edit账户权限"), now);
                        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                    }
                } else {
                    permissionService.companyPermission(cid, permission_ids);
                    if (permissionService.delMemberPermissions(cid)) {
                        String ip = IpUtil.getIpAddr(request);
                        String address = ip2regionSearcher.getAddressAndIsp(ip);
                        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("delete账户权限"), now);
                        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                    } else {
                        String ip = IpUtil.getIpAddr(request);
                        String address = ip2regionSearcher.getAddressAndIsp(ip);
                        operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), LocalUtil.get("delete账户权限"), now);
                        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @GetMapping("/index")
    public String index(Model model) {
        // 获取所有菜单权限
        List<MenuVO> menus = permissionService.getAllMenuPermissions();
        // 2. 遍历主菜单获取对应的权限
        for (MenuVO menu : menus) {
            String permissions = permissionService.getMainMenuPermissions(menu.getId());
            menu.setPermissions(permissions);
        }

        model.addAttribute("menus", menus);

        return "index";
    }

////    @RequiresPermissions({"rle:permiss"})
//    @RequestMapping(value = "/companyPermission")
//    @ApiOperation(value = "权限组权限设置接口",notes = "输入查询条件")
//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "query",name = "cid",value = "部门id",required = false,dataType = "int"),
//            @ApiImplicitParam(paramType = "query",name = "permission_ids",value = "权限id集",required = false,dataType = "String"),
//            @ApiImplicitParam(paramType = "query",name = "project_ids",value = "实例id集",required = false,dataType = "String")
//    })
//    public  CommonResult<Integer> companyPermission(Integer cid,Integer uid,String permission_ids,String project_ids){
//        try {
//            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
//            String[] split = permission_ids.split(",");
//            List<Integer> permissIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
//            if (permissionService.companyPermission(cid,permission_ids)) {
//                permissionService.setMemberPermiss(permissIdList,member.getUid());
//                companyService.updateCid(cid,project_ids);
//                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
//            }
//
//        }catch (Exception e){
//            e.printStackTrace();
//            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//        }
//        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
//    }

}
