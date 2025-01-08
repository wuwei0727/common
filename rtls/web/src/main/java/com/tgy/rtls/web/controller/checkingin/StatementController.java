package com.tgy.rtls.web.controller.checkingin;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.checkingin.Attendancerule;
import com.tgy.rtls.data.entity.checkingin.StatementVO;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.checkingin.StatementService;
import com.tgy.rtls.data.service.common.RedisService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.checkingin
 * @date 2020/11/16
 * 考勤报表管理
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/statement")
public class StatementController {
    @Autowired
    private StatementService statementService;
    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/getStatementSel")
    @RequiresPermissions("statement:sel")
    @ApiOperation(value = "排班详情接口",notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = true,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "classid",value = "班组id",required = false,dataType = "int")
    })
    public CommonResult<Object> getStatementSel(String month,Integer departmentid,Integer worktypeid,Integer jobid,Integer classid){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<StatementVO> statementVOList=statementService.findByAll(instanceid,month,departmentid,worktypeid,jobid,classid);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),statementVOList);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getRuleSel")
    @RequiresPermissions("statementrule:sel")
    @ApiOperation(value = "考勤规则接口",notes = "无")
    public CommonResult<Object> getStatementSel(){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Attendancerule> rules=statementService.findByRule(instanceid);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),rules);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateRule")
    @RequiresPermissions("statementrule:update")
    @ApiOperation(value = "考勤规则修改接口",notes = "规则信息")
    public CommonResult<Object> updateWarnRule(@RequestBody List<Attendancerule> rules){
        try {
            if (statementService.updateRule(rules)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Object>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }

    /*
    * 考勤报表导出
    * */

    @RequestMapping("/exportStatement")
    @ApiOperation(value = "考勤报表导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = true,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "classid",value = "班组id",required = false,dataType = "int")
    })
    public void exportStatement(String month,Integer departmentid,Integer worktypeid,Integer jobid,Integer classid,String title,HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.WORK_ATTENDANCE) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            statementService.exportStatementVO(out,instanceid,month,departmentid,worktypeid,jobid,classid,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
