package com.tgy.rtls.web.controller.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.*;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.MonitorService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.tool.DateUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.common
 * @date 2020/11/5
 *
 */
@RestController
@RequestMapping(value = "/monitor")
@CrossOrigin
public class MonitorController {
    @Autowired
    private MonitorService monitorService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private LocalUtil localUtil;
    @RequestMapping(value = "/getMonitorSel")
    @ApiOperation(value = "检测信息详情查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String")
    })

    public CommonResult<Object> getMonitorSel(String map,String departmentid, String worktypeid,String jobid,String startTime,
                                                String endTime){
        try {

            //项目信息统计
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Monitor monitorProject=monitorService.findByProject(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            //地图信息统计
            List<Monitor> monitorMap=monitorService.findByMap(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            //区域信息统计
            List<MonitorArea> monitorAreas=monitorService.findByArea(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            //分站信息统计
            List<MonitorSub> monitorSubs=monitorService.findBySub(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            Map<String,Object> result=new HashMap<>();
            result.put("project",monitorProject);
            result.put("map",monitorMap);
            result.put("area",monitorAreas);
            result.put("sub",monitorSubs);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/getMonitorProjectSel")
    @ApiOperation(value = "检测信息详情项目查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMonitorProjectSel(String departmentid, String worktypeid,String jobid,String startTime,String endTime,
                                                     @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                     @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<MonitorPerson> personList=monitorService.findByProjectPerson(instanceid,departmentid,worktypeid,jobid,startTime,endTime);
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=monitorService.findByProjectPerson(instanceid,departmentid,worktypeid,jobid,startTime,endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<MonitorPerson> personList=monitorService.findByProjectPerson(instanceid,departmentid,worktypeid,jobid,startTime,endTime);
            PageInfo<MonitorPerson> pageInfo=new PageInfo<>(personList);
            //项目信息统计
            Monitor monitorProject=monitorService.findByProject(instanceid,null,departmentid,worktypeid,jobid,startTime,endTime);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("monitorProject",monitorProject);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMonitorMapSel")
    @ApiOperation(value = "检测信息详情地图查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMonitorMapSel(String map,String departmentid, String worktypeid,String jobid,String startTime,String endTime,
                                                     @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                     @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            String uid="12";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<MonitorPerson> personList=monitorService.findByMapPerson(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=monitorService.findByMapPerson(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<MonitorPerson> personList=monitorService.findByMapPerson(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            PageInfo<MonitorPerson> pageInfo=new PageInfo<>(personList);
            //地图信息统计
            Monitor monitorMap=monitorService.findByMapId(instanceid,map,departmentid,worktypeid,jobid,startTime,endTime);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("monitorMap",monitorMap);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMonitorAreaSel")
    @ApiOperation(value = "检测信息详情区域查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "area",value = "区域id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMonitorAreaSel(String map,String area,String departmentid, String worktypeid,String jobid,
                                                  String startTime,String endTime,
                                                 @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                 @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            String uid="12";
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<MonitorPerson> personList=monitorService.findByAreaPerson(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime);
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=monitorService.findByAreaPerson(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<MonitorPerson> personList=monitorService.findByAreaPerson(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime);
            PageInfo<MonitorPerson> pageInfo=new PageInfo<>(personList);
            //区域信息统计
            MonitorArea monitorArea=monitorService.findByAreaId(instanceid,map,area,departmentid,worktypeid,jobid,startTime,endTime);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("monitorArea",monitorArea);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getMonitorSubSel")
    @ApiOperation(value = "检测信息详情区域查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "sub",value = "分站id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMonitorSubSel(String map,String sub,String departmentid, String worktypeid,String jobid,
                                                  String startTime,String endTime,
                                                  @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                  @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<MonitorPerson> personList=monitorService.findBySubPerson(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime);
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=monitorService.findBySubPerson(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<MonitorPerson> personList=monitorService.findBySubPerson(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime);
            PageInfo<MonitorPerson> pageInfo=new PageInfo<>(personList);
            //分站信息统计
            MonitorSub monitorSub=monitorService.findBySubId(instanceid,map,sub,departmentid,worktypeid,jobid,startTime,endTime);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            result.put("monitorSub",monitorSub);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
    * 获取区域下拉框信息
    * */
    @RequestMapping(value = "/getAreaId/{map}")
    @ApiOperation(value = "区域下拉框信息",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "map",value = "地图id",required = true,dataType = "String")
    public CommonResult<List<MonitorAreatype>> getAreaId(@PathVariable("map")String map){
        try {
            List<MonitorAreatype> areatypes=monitorService.findByAreatype(map);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),areatypes);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 获取区域下拉框信息
     * */
    @RequestMapping(value = "/getSubId/{map}")
    @ApiOperation(value = "分站下拉框信息",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "map",value = "地图id",required = true,dataType = "String")
    public CommonResult<Object> getSubId(@PathVariable("map")String map){
        try {
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),monitorService.findBySubtype(map));
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
