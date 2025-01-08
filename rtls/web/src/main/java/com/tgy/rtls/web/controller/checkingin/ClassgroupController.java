package com.tgy.rtls.web.controller.checkingin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.checkingin.Classgroup;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.checkingin.ClassgroupMapper;
import com.tgy.rtls.data.service.checkingin.ClassgroupService;
import com.tgy.rtls.data.service.common.RedisService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.checkingin
 * @date 2020/11/16
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/classgroup")
public class ClassgroupController  {
    @Autowired
    private ClassgroupService classgroupService;
    @Autowired(required = false)
    private ClassgroupMapper classgroupMapper;

    @Autowired
    private RedisService redisService;
    @RequestMapping(value = "/getClassgroupSel")
   @RequiresPermissions("classgroup:sel")
   @ApiOperation(value = "班组查询接口",notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "name",value = "班组名",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getClassgroupSel(String name,@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Classgroup> classgroups = classgroupService.findByAllLike(name,instanceid);
                return new CommonResult<>(200,LocalUtil.get( KafukaTopics.QUERY_SUCCESS),classgroups);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=classgroupService.findByAllLike(name,instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Classgroup> classgroups = classgroupService.findByAllLike(name,instanceid);
            PageInfo<Classgroup> pageInfo=new PageInfo<>(classgroups);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addClassgroup")
    @ApiOperation(value = "班组新增接口",notes = "班组信息")
    public CommonResult<Integer> addClassgroup(Classgroup classgroup,String personids){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            String name=classgroup.getName();
            if(name==null||name.trim().isEmpty()){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }else{
                classgroup.setName(classgroup.getName().trim());
            }
            


            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
           
            String instanceid=(redisService.get("instance"+uid));
            List<Classgroup> sameName = classgroupService.findByAllEqual(name.trim(), instanceid);
            if(sameName!=null&&sameName.size()>0){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }
            classgroup.setInstanceid(instanceid);

            if (classgroupService.addClassgroup(classgroup,personids)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),classgroup.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateClassgroup")
    @RequiresPermissions("classgroup:update")
    @ApiOperation(value = "班组修改接口",notes = "班组信息")
    public CommonResult updateClassgroup(Classgroup classgroup,String personids){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
        /*    String name=classgroup.getName();
            if(name==null||name.trim().equals("")){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }*/
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            String instanceid = redisService.get("instance" + uid);
            if(personids==null) {
                String name = classgroup.getName().trim();
                if (name == null || name.trim().isEmpty()) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_EMPTY));
                }
                List<Classgroup> sameName = classgroupService.findByAllEqual(name, instanceid);
                if (sameName != null && sameName.size() > 0 && !classgroup.getId().equals(sameName.get(0).getId())) {
                    return new CommonResult<>(400, LocalUtil.get(KafukaTopics.NAME_CONFLICT));
                }
            }
            classgroup.setInstanceid(instanceid);
            classgroup.setUpdateTime(new Date());
            if(personids!=null){
                if (classgroupService.updateClassgroup(classgroup,personids)){
                    return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),classgroup.getId());
                }
            }else {
                classgroupMapper.updateClassgroup(classgroup);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), classgroup.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    @RequestMapping(value = "/delClassgroup/{ids}")
    @ApiOperation(value = "班组删除接口",notes = "班组id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "班组id集",required = true,dataType = "String")
    public CommonResult delClassgroup(@PathVariable("ids")String ids){
        try {
            if (classgroupService.delClassgroup(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
