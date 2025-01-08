package com.tgy.rtls.web.controller.message;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.message.WarnMap;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.type.Status;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.message.WarnRecordService;
import com.tgy.rtls.data.tool.DateUtils;
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
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.message
 * @date 2020/11/4
 * 报警功能
 */
@RestController
@RequestMapping(value = "/warn")
@CrossOrigin
public class WarnController {
    @Autowired
    private WarnRecordService warnRecordService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private LocalUtil localUtil;

    @RequestMapping(value = "/getWarnRecordSel")
    @ApiOperation(value = "报警记录查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "type",value = "类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "areaType",value = "区域类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "areaName",value = "区域名称",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "warnstate",value = "报警状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getWarnRecordSel(Integer type, Integer map, Integer areaType,String areaName,String startTime, String endTime,Integer warnstate,
                                           @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                           @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
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
            String instanceid= (redisService.get("instance"+uid));
            operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.QUERY_WARNINGRECORD),null);
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<WarnRecord> warnRecordList = warnRecordService.findByRecordAll(instanceid,map,startTime,endTime,type,warnstate,areaType,areaName);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),warnRecordList);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=warnRecordService.findByRecordAll(instanceid,map,startTime,endTime,type,warnstate,areaType,areaName).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<WarnRecord> warnRecordList = warnRecordService.findByRecordAll(instanceid,map,startTime,endTime,type,warnstate,areaType,areaName);
            PageInfo<WarnRecord> pageInfo=new PageInfo<>(warnRecordList);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
    * 查看报警类型
    * */
    @RequestMapping(value = "/getWarnType")
    public CommonResult<Object> getWarnType(){
        try {
            List<Status> warnType=warnRecordService.findByWarnType();
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),warnType);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    @RequestMapping(value = "/getWarnRuleSel")
    @ApiOperation(value = "报警规则查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "type",value = "类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "enable",value = "是否启用",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getWarnRuleSel(Integer type, Integer map, Integer enable,
                                           @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<WarnMap> maps=warnRecordService.findByMap(map);
                for (WarnMap warnMap:maps){
                    List<WarnRule> warnRuleList = warnRecordService.findByRuleAll(instanceid,type,warnMap.getId(),enable);
                    warnMap.setWarnRules(warnRuleList);
                }
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),maps);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=warnRecordService.findByMap(map).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                }else{
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<WarnMap> maps=warnRecordService.findByMap(map);
            for (WarnMap warnMap:maps){
                List<WarnRule> warnRuleList = warnRecordService.findByRuleAll(instanceid,type,warnMap.getId(),enable);
                warnMap.setWarnRules(warnRuleList);
            }
            PageInfo<WarnMap> pageInfo=new PageInfo<>(maps);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateWarnRuleType")
    @ApiOperation(value = "报警规则修改接口",notes = "报警规则信息")
    public CommonResult<Object> updateWarnRule(@RequestBody List<WarnRule> rules){
        try {
            if (warnRecordService.updateWarnRule(rules)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.SAVE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Object>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SAVE_FAIL));
    }



    @RequestMapping("/exportWarnRecord")
    @ApiOperation(value = "报警记录导出接口",notes = "输入查询条件")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "type",value = "类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "areaType",value = "区域类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "warnstate",value = "是否结束",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "areaName",value = "区域名称",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String")
    })
    public void exportWarnRecord(Integer type,Integer warnstate,  Integer areaType,String areaName,Integer map, String startTime, String endTime, String title,HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            String instanceid = redisService.get("instance" + uid);;
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.WARNINGRECORD) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            warnRecordService.exportWarnRecord(out,instanceid,map,startTime,endTime,type,title,areaType,areaName,warnstate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
