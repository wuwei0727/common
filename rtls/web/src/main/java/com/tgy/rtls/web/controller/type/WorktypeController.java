package com.tgy.rtls.web.controller.type;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.type.Worktype;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.type.WorktypeService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.type
 * @date 2020/10/15
 * 工种管理类
 */

@RestController
@CrossOrigin
@RequestMapping(value = "/worktype")
public class WorktypeController {
    @Autowired
    private WorktypeService worktypeService;
    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/getWorktypeSel")
    @ApiOperation(value = "工种查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getWorktypeSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                            @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<Worktype> worktypeList = worktypeService.findByAll(instanceid);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),worktypeList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=worktypeService.findByAll(instanceid).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Worktype> worktypeList = worktypeService.findByAll(instanceid);
            PageInfo<Worktype> pageInfo=new PageInfo<>(worktypeList);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/getWorktypeId/{id}")
    @ApiOperation(value = "工种详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "工种id",required = true,dataType = "int")
    public CommonResult<Worktype> getWorktypeId(@PathVariable("id")Integer id){
        try {
            Worktype worktype=worktypeService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),worktype);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/addWorktype")
    @ApiOperation(value = "工种新增接口",notes = "工种信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "工种名",required = true,dataType = "String")})
    public CommonResult<Integer> addWorktype(Worktype worktype){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (worktype.getName()==null||worktype.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.WORKTYPE_EMPTY));
            }


            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Worktype> list = worktypeService.getWorkType(instanceid, worktype.getName().trim());
            if(list!=null&&list.size()>0){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.WORKTYPE_EXIST));
            }

            worktype.setInstanceid(instanceid);
            if (worktypeService.addWorktype(worktype)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),worktype.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,e.getMessage());
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateWorktype")
    @ApiOperation(value = "工种修改接口",notes = "工种信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "id",value = "工种id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "工种名",required = true,dataType = "String")
    })
    public CommonResult updateWorktype(Worktype worktype){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (worktype.getName()==null||worktype.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.WORKTYPE_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Worktype> list = worktypeService.getWorkType(instanceid, worktype.getName().trim());
            if(list!=null&&list.size()>1||list.size()==1&&list.get(0).getId().intValue()!=worktype.getId().intValue()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.WORKTYPE_EXIST));
            }
            worktype.setInstanceid(instanceid);
            if (worktypeService.updateWorktype(worktype)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delWorktype/{ids}")
    @ApiOperation(value = "工种删除接口",notes = "工种id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "工种id集",required = true,dataType = "String")
    public CommonResult delWorktype(@PathVariable("ids")String ids){
        try {
            if (worktypeService.delWorktype(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
