package com.tgy.rtls.web.controller.app;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.user.SysUserService;
import com.tgy.rtls.data.tool.IpUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-07-22 13:06
 * @Description: TODO
 * @Version: 1.0
 */
@RequestMapping(value = "/user")
@CrossOrigin
@RestController
@Api(value = "web端app用户接口")
public class WebAppUserController {
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;

    @RequestMapping(value = "/getAllUsers")
    @ApiOperation(value = "web端app查询用户接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "keyword", value = "关键字", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enabled", value = "是否启用", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getAllUsers(String keyword,
                                            @RequestParam(value = "desc ", defaultValue = "createdTime desc") String desc,
                                            Integer enable,
                                            @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                            @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize) {
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            List<SysUser> CreateBy = sysUserService.getCreateByuid(member.getUid());
            if (NullUtils.isEmpty(CreateBy)) {
                return new CommonResult<>(400, LocalUtil.get("你没有在当前账号创建用户账号！！！"));
            }
            if (pageSize < 0) {
                List<SysUser> sysUsers = sysUserService.getAllUsers2(keyword, enable, CreateBy.get(0).getCreateuId(), desc);
                //查询成功
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), sysUsers);
            }
            int num = sysUserService.getAllUsers2(keyword, enable, CreateBy.get(0).getCreateuId(), desc).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<SysUser> sysUsers = sysUserService.getAllUsers2(keyword, enable, CreateBy.get(0).getCreateuId(), desc);
            PageInfo<SysUser> pageInfo = new PageInfo<>(sysUsers);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            //查询成功
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            //系统繁忙
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"apr:add"})
    @RequestMapping(value = "/addAppUser")
    @ApiOperation(value = "web端app新增用户接口", notes = "用户信息")
    public CommonResult<Integer> addAppUser(SysUser sysUser, String mapid, HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (sysUser.getUserName() == null || "".equals(sysUser.getUserName())) {
                //用户名为空
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.USERNAME_NULL));
            }
            if (sysUser.getUserName().length() < 6) {
                //用户名
                return new CommonResult<>(400, LocalUtil.get("用户名长度不能低于6位"));
            }
            if (sysUser.getPassword() == null || sysUser.getPassword().length() < 6 || "".equals(sysUser.getPassword())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            }
            if (NullUtils.isEmpty(mapid)) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.MAP_IS_NOT_NULL));
            }
            SysUser userName = sysUserService.findUserName(sysUser.getUserName());
            //判断是否重复
            if (!NullUtils.isEmpty(userName)) {
                //用户名不能重复
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UserName_EXIST_ERROR));
            }
            String[] spilt = mapid.split(",");
            if (sysUserService.saveAppUserMap(sysUser)) {
                for (String str : spilt) {
                    sysUserService.insertUserMap(sysUser.getUserId(), str);
                }
//                打开shiro才开
                operationlogService.addOperationloguser(sysUser.getUserId(), LocalUtil.get(KafukaTopics.ADD_PERSONPERMISSION) + sysUser.getUserName());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.ADD.concat(LocalUtil.get(KafukaTopics.MOBILE_USER_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), sysUser.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequiresPermissions(value = {"apr:see","apr:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getAppUserId/{id}")
    @ApiOperation(value = "web端app用户详情接口", notes = "用户id")
    @ApiImplicitParam(paramType = "path", name = "id", value = "成员id", required = true, dataType = "int")
    public CommonResult<Object> getAppUserId(@PathVariable("id") Integer userId) {
        try {
            SysUser sysUser = sysUserService.getByUserId(userId);
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

    @RequiresPermissions({"apr:edit"})
    @RequestMapping(value = "/updateAppUser")
    @ApiOperation(value = "web端app修改用户接口", notes = "用户信息")
    public CommonResult<Integer> updateAppUser(SysUser sysUser, Integer userId, String mapid,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            SysUser sysUser1 = sysUserService.getByUserId(userId);
            if (sysUserService.findUserName(sysUser.getUserName()) != null && !sysUser.getUserName().equals(sysUser1.getUserName())) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UserName_EXIST_ERROR));
            }
            //if (sysUser.getPassword() == null || sysUser.getPassword().length() < 6 || sysUser.getPassword().equals("")) {
            //    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            //}
            if(!"".equals(sysUser.getPassword())&&sysUser.getPassword().length() <6){
                sysUser.setPassword("");
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PASSWORD_LENGTH));
            }
            String[] split = mapid.split(",");

            if (sysUserService.updateByUserId(sysUser)) {
//                for (String str : split ) {
                sysUserService.delUserMap(Arrays.asList(split), sysUser.getUserId());
//                }
                operationlogService.addOperationloguser(sysUser.getUserId(), LocalUtil.get(KafukaTopics.UPDATE_GROUPPEMISSION) + sysUser.getUserName());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.EDIT.concat(LocalUtil.get(KafukaTopics.MOBILE_USER_INFO)), now);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), sysUser.getUserId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    /**
     * @param ids
     * @param sysUser 用户实体类
     * @return
     */
    @RequiresPermissions({"apr:del"})
    @RequestMapping(value = "/delAppUser/{ids}")
    @ApiOperation(value = "web端app删除用户接口", notes = "用户id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "部门id集", required = true, dataType = "String")
    public CommonResult<Object> delAppUser(@PathVariable("ids") String ids, SysUser sysUser,HttpServletRequest request) {
        try {
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            CommonResult<Object> commonResult = new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            int i = sysUserService.removeByIds(ids.split(","));
            if (i > -1) {
                operationlogService.addOperationloguser(member.getUid(), LocalUtil.get(KafukaTopics.UPDATE_GROUPPEMISSION) + sysUser.getUserName());
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.MOBILE_USER_INFO)), now);
                return commonResult;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

//    @RequiresPermissions({"apr:permiss"})
//    @RequestMapping(value = "/appUserPermission")
//    @ApiOperation(value = "用户权限设置接口",notes = "输入查询条件")
//    @ApiImplicitParams({
//            @ApiImplicitParam(paramType = "query",name = "cid",value = "部门id",required = false,dataType = "int"),
//            @ApiImplicitParam(paramType = "query",name = "permiss",value = "权限id集",required = false,dataType = "String"),
//            @ApiImplicitParam(paramType = "query",name = "project_ids",value = "实例id集",required = false,dataType = "String")
//    })
//    public  CommonResult<Integer> appUserPermission(@RequestParam("permiss") String permissIds,@RequestParam("userId") Integer userId){
//            try {
//                String[] split = permissIds.split(",");
//                List<Integer> permissIdList = Stream.of(split).map(Integer::parseInt).collect(Collectors.toList());
//                int  i = sysUserService.setUserPermiss(permissIdList,userId);
//                if(i > -1 ){
////                    SysUserDemo sysUser=(SysUserDemo) SecurityUtils.getSubject().getPrincipal();
////                    Set<String> permissions = sysUserService.findByUserId(userId);
////                    sysUser.setPermissions(permissions);
//                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
//                } else {
//                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
//            }
//    }
}
