package com.tgy.rtls.web.controller.type;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.type.Job;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.type.JobService;
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
 * 职务管理类
 * */
@RestController
@CrossOrigin
@RequestMapping(value = "/job")
public class JobController {
    @Autowired
    private JobService jobService;
    @Autowired
    private RedisService redisService;
    @RequestMapping(value = "/getJobSel")
    @ApiOperation(value = "职务查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getJobSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<Job> jobList = jobService.findByAll(instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),jobList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=jobService.findByAll(instanceid).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Job> jobList = jobService.findByAll(instanceid);
            PageInfo<Job> pageInfo=new PageInfo<>(jobList);
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

    @RequestMapping(value = "/getJobId/{id}")
    @ApiOperation(value = "职务详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "职务id",required = true,dataType = "int")
    public CommonResult<Job> getJobId(@PathVariable("id")Integer id){
        try {
            Job job=jobService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),job);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/addJob")
    @ApiOperation(value = "职务新增接口",notes = "职务信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "职务名",required = true,dataType = "String")})
    public CommonResult<Integer> addJob(Job job){
        try {
            //实例
            String uid="12";
            if (job.getName()==null||job.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.JOB_EMPTY));
            }
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (job.getName()==null||job.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.JOB_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Job> list = jobService.findJobByName(instanceid, job.getName().trim());
            if(list!=null&&list.size()>0){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.JOB_EXIST));
            }

            job.setInstanceid(instanceid);
            if (jobService.addJob(job)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),job.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,e.getMessage());
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateJob")
    @ApiOperation(value = "职务修改接口",notes = "职务信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "id",value = "职务id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "职务名",required = true,dataType = "String")
    })
    public CommonResult updateJob(Job job){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (job.getName()==null||job.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.JOB_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Job> list = jobService.findJobByName(instanceid, job.getName().trim());
            if(list!=null&&list.size()>1||list.size()==1&&list.get(0).getId().intValue()!=job.getId().intValue()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.JOB_EXIST));
            }job.setInstanceid(instanceid);
            if (jobService.updateJob(job)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delJob/{ids}")
    @ApiOperation(value = "职务删除接口",notes = "职务id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "职务id集",required = true,dataType = "String")
    public CommonResult delJob(@PathVariable("ids")String ids){
        try {
            if (jobService.delJob(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult(500,e.getMessage());
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
