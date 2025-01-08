package com.tgy.rtls.web.controller.user;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Instance;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.lock.impl.RedissonDistributedLocker;
import com.tgy.rtls.data.service.user.InstanceService;
import com.tgy.rtls.data.service.user.impl.PermissionService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.user
 * @date 2020/11/12
 * 实例管理
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/instance")
public class InstanceController {
    @Autowired(required = false)
    private InstanceService instanceService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedissonDistributedLocker redissonDistributedLocker;
    @RequestMapping(value = "/getInstanceSel")
    @ApiOperation(value = "实例查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "name",value = "名称",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getInstanceSel(String name,
                                             @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                             @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            Integer uid=null;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= member.getUid();
            }
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Instance> instances=instanceService.findByAll(uid,name);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),instances);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=instanceService.findByAll(uid,name).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Instance> instances=instanceService.findByAll(uid,name);
            PageInfo<Instance> pageInfo=new PageInfo<>(instances);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getInstanceId/{id}")
    @ApiOperation(value = "实例详情接口",notes = "实例id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "实例id",required = true,dataType = "int")
    public CommonResult<Instance> getInstanceId(@PathVariable("id")Integer id){
        try {
            Instance instance=instanceService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),instance);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/addInstance")
    @RequiresPermissions("instance:add")
    @ApiOperation(value = "实例新增接口",notes = "实例信息")
    public CommonResult<Object> addInstance(Instance instance){
        try {
            //识别码2重名判断
            Instance instance1=instanceService.findByCode2(instance.getCode2());
            if (!NullUtils.isEmpty(instance1)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.IDCODE_INUSE));
            }
            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyyMMhhHHmmss");
            instance.setNum(dateFormat.format(new Date()));
            if (instanceService.addInstance(instance)){
                Integer uid=null;
                Member member=(Member) SecurityUtils.getSubject().getPrincipal();
                if(!NullUtils.isEmpty(member)){
                    uid= member.getUid();
                }
                //新增项目后将该项目的权限提供给登录人员
                permissionService.insertMemberProject(uid,member.getCid(),instance.getId());
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.ADD_PROJECT)+instance.getName(),instance.getId());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),instance.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateInstance")
    @RequiresPermissions("instance:update")
    @ApiOperation(value = "实例修改接口",notes = "实例信息")
    public CommonResult<Object> updateInstance(Instance instance){
        try {
            //识别码2重名判断
            Instance instance1=instanceService.findById(instance.getId());
            Instance instance2=instanceService.findByCode2(instance.getCode2());
            if (!instance1.getCode2().equals(instance.getCode2())&&!NullUtils.isEmpty(instance2)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.IDCODE_INUSE));
            }
            if (instanceService.updateInstance(instance)){
                Integer uid=null;
                Member member=(Member) SecurityUtils.getSubject().getPrincipal();
                if(!NullUtils.isEmpty(member)){
                    uid= member.getUid();
                }
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.UPDATE_PROJECT)+instance.getName());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS),instance.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    @RequestMapping(value = "/delInstance/{id}")
    @RequiresPermissions("instance:del")
    @ApiOperation(value = "实例删除接口",notes = "实例id集")
    @ApiImplicitParam(paramType = "path",name = "id",value = "实例id",required = true,dataType = "int")
    public CommonResult delInstance(@PathVariable("id")Integer id){
        try {
           String instanceName= instanceService.findByNameId(id);
            if (instanceService.delInstance(id)){
                Integer uid=null;
                Member member=(Member) SecurityUtils.getSubject().getPrincipal();
                if(!NullUtils.isEmpty(member)){
                    uid=member.getUid();
                }
                //清除缓存
                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.DELETE_PROJECT)+instanceName,id);
                redisService.remove("instance"+uid);
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_PROJECT_FAIL));
    }

    @RequestMapping(value = "/goInstance/{id}")
    @ApiOperation(value = "进入实例接口",notes = "实例id")
    @ApiImplicitParam(paramType = "path",name = "id",value = "实例id",required = true,dataType = "int")
    public CommonResult goInstance(@PathVariable("id")Integer id){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            redisService.set("instance"+uid, String.valueOf(id));
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.GO_PROJECT)+instanceService.findByNameId(id));
            return new CommonResult(200,LocalUtil.get(KafukaTopics.SUCCESS));
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
    * 判断是否有进入项目的操作
    * */
    @RequestMapping(value = "/goProject")
    @ApiOperation(value = "进入实例接口",notes = "实例id")
    public CommonResult goProject(){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (!NullUtils.isEmpty(redisService.get("instance"+uid))) {
                return new CommonResult(200, LocalUtil.get(KafukaTopics.SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.SELECTPROJECT_FIRST));
    }
}
