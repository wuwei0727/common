package com.tgy.rtls.web.controller.checkingin;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.checkingin.*;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.checkingin.WorkorderService;
import com.tgy.rtls.data.service.common.RedisService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.checkingin
 * @date 2020/11/13
 * 考勤管理
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/workorder")
public class WorkorderController {
    @Autowired
    private WorkorderService workorderService;

    @Autowired
    private RedisService redisService;
    @RequestMapping(value = "/getWorkorderSel")
    @ApiOperation(value = "班次查询接口",notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getWorkorderSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<WorkorderVO> workorderVOList = workorderService.findByAll(instanceid);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),workorderVOList);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=workorderService.findByAll(instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<WorkorderVO> workorderVOList = workorderService.findByAll(instanceid);
            PageInfo<WorkorderVO> pageInfo=new PageInfo<>(workorderVOList);
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

    @RequestMapping(value = "/getWorkorderId/{id}")
    @ApiOperation(value = "班次详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "班次id",required = true,dataType = "int")
    public CommonResult<Object> getWorkorderId(@PathVariable("id")Integer id){
        try {
            WorkorderVO workorderVO=workorderService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),workorderVO);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addWorkorder")
    @ApiOperation(value = "班次新增接口",notes = "班次信息")
    public CommonResult<Integer> addWorkorder(@RequestBody WorkorderVO workorderVO){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Workorder workorder=workorderService.findByNum(workorderVO.getNum());
            if (!NullUtils.isEmpty(workorder)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.WORK_RANGENAME_CONFIX));
            }
            workorderVO.setInstanceid(instanceid);
            if (workorderService.addWorkorder(workorderVO)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),workorderVO.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateWorkorder")
    @ApiOperation(value = "班次修改接口",notes = "班次信息")
    public CommonResult updateWorkorder(@RequestBody WorkorderVO workorderVO){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Workorder workorder=workorderService.findByNum(workorderVO.getNum());
            WorkorderVO workorderVO1=workorderService.findById(workorderVO.getId());
            if (!NullUtils.isEmpty(workorder)&&!workorderVO1.getNum().equals(workorderVO.getNum())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.WORK_RANGENAME_CONFIX));
            }
            workorderVO.setInstanceid(instanceid);
            if (workorderService.updateWorkorder(workorderVO)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),workorderVO.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delWorkorder/{ids}")
    @ApiOperation(value = "班次删除接口",notes = "班次id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "班次id集",required = true,dataType = "String")
    public CommonResult delWorkorder(@PathVariable("ids")String ids){
        try {
            if (workorderService.delWorkorder(ids)){
                String[] woids=ids.split(",");
                for (String id:woids
                     ) {
                    workorderService.delSchedulingWoid(Integer.valueOf(id),null);
                }

                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/addScheduling")
    @ApiOperation(value = "新增排班接口",notes = "排班信息")
    public CommonResult<Integer> addScheduling(Scheduling scheduling,String personids){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            scheduling.setInstanceid(instanceid);
            if (workorderService.addScheduling(scheduling,personids)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateScheduling")
    @ApiOperation(value = "修改排班接口",notes = "排班信息")
    public CommonResult updateScheduling(Scheduling scheduling,String personids){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            scheduling.setInstanceid(instanceid);
            if (workorderService.updateScheduling(scheduling,personids)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }
    @RequestMapping(value = "/delScheduling")
    @ApiOperation(value = "排班删除接口",notes = "班次id集")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "woid",value = "班次id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = false,dataType = "string")
    })
    public CommonResult delScheduling(Integer woid,String month){
        try {
            if (workorderService.delSchedulingWoid(woid,month)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/getSchedulingSel/{month}")
    @ApiOperation(value = "排班详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "month",value = "年月",required = true,dataType = "string")
    public CommonResult<Object> getSchedulingSel(@PathVariable("month")String month){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<SchedulingVO> schedulingVO=workorderService.findByScheduling(month,instanceid);
            List<WorkorderVO> workorderVO=workorderService.findBySchedulingWorkorder(month,instanceid);
            Map<String,Object> map=new HashMap<>();
            map.put("workorderVO",workorderVO);//该月份使用的班次信息
            map.put("schedulingVO",schedulingVO);//该月份的排班信息
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/exportSchedulingSel/{month}")
    @ApiOperation(value = "考勤排班详情接口（用于打印）",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "month",value = "年月",required = true,dataType = "string")
    public CommonResult<Object> exportSchedulingSel(@PathVariable("month")String month){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<PersonSchedulingVO> personSchedulingVOS=workorderService.exportSchedulingSel(month,instanceid);
            List<WorkorderVO> workorderVO=workorderService.findBySchedulingWorkorder(month,instanceid);
            Map<String,Object> map=new HashMap<>();
            map.put("workorderVO",workorderVO);//该月份使用的班次信息
            map.put("personSchedulingVOS",personSchedulingVOS);//
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 考勤排班导出
     * */

    @RequestMapping("/exportScheduling")
    @ApiOperation(value = "考勤排班导出接口",notes = "输入查询条件")
    @ApiImplicitParam(paramType = "path",name = "month",value = "年月",required = true,dataType = "string")
    public void exportScheduling(String month, String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
           Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.WORK_RANGE) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            workorderService.exportRouteTask(out,month,instanceid,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * 查询人员班次名称
     * */

    @RequestMapping("/getWorkInfoName")
    @ApiOperation(value = "考勤规则名称",notes = "输入人员id")
    public CommonResult<? extends Object> getWorkInfoName(Integer personId){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<WorkInf> workName = workorderService.findWorkInfNameByPersonIdAsso(personId);

            return new CommonResult<>(200,KafukaTopics.QUERY_SUCCESS,workName);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 查询人员班次详情
     * */

    @RequestMapping("/getWorkInfo")
    @ApiOperation(value = "班次详情查询",notes = "人员id")
    public CommonResult<? extends Object> getWorkInf(Integer personId){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            List<WorkInf> workName = workorderService.findWorkInfByPersonId(personId);
            return new CommonResult<>(200,KafukaTopics.QUERY_SUCCESS,workName);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

}
