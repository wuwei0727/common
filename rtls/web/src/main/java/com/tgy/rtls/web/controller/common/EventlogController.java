package com.tgy.rtls.web.controller.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.common.Eventlog;
import com.tgy.rtls.data.entity.common.EventlogType;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.EventlogService;
import com.tgy.rtls.data.service.common.RedisService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
 * @Package com.tgy.rtls.web.controller.common
 * @date 2020/11/9
 * 事件日志
 */
@RestController
@RequestMapping(value = "/eventlog")
@CrossOrigin
public class EventlogController {
    @Autowired
    private EventlogService eventlogService;
    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/getEventlogSel")
    @RequiresPermissions("eventlog:sel")
    @ApiOperation(value = "事件日志查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "type",value = "事件类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "typeSimple",value = "事件类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "departmentId",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "personName",value = "人员名称",required = false,dataType = "String")
    })
    public CommonResult<Object> getEventlogSel(Integer type,Integer typeSimple,String startTime,String endTime, Integer map,
                                          @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                          @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize,Integer departmentId,String personName ){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
        /*    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",localUtil.getCurrentLocale());
           if(endTime!=null&&!endTime.trim().isEmpty()) {
               Date date = dateFormat.parse(endTime);
               Calendar calendar = Calendar.getInstance();
               calendar.setTime(date);
               Calendar after = DateUtils.getAfterDay(calendar);
               endTime = dateFormat.format(after.getTime());
           }*/
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Eventlog> eventlogList = eventlogService.findByAll(instanceid,map,typeSimple,type,startTime,endTime, departmentId, personName);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),eventlogList);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=eventlogService.findByAll(instanceid,map,typeSimple,type,startTime,endTime, departmentId, personName).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Eventlog> eventlogList = eventlogService.findByAll(instanceid,map,typeSimple,type,startTime,endTime,departmentId, personName);
            PageInfo<Eventlog> pageInfo=new PageInfo<>(eventlogList);
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
     * 事件日志类型接口
     * */
    @RequestMapping(value = "/getEventlogType")
    public CommonResult<Object> getEventlogType(){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
           // List<EventlogType> eventlogTypes=eventlogService.findByType(instanceid);
            List<EventlogType> eventlogTypes=eventlogService.findByTypeSimple();
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),eventlogTypes);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping("/exportEventlog")
    @ApiOperation(value = "事件日志导出接口",notes = "输入查询条件")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "type",value = "事件类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "typeSimple",value = "事件类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "departmentId",value = "部门id",required = false,dataType = "int")
    })
    public void exportEventlog(Integer type,Integer typeSimple,String startTime,String endTime, Integer map,String title,Integer departmentId,String personName,HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.EVENT_LOG) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            eventlogService.exportEventlog(out,instanceid,map,typeSimple,type,startTime,endTime,title, departmentId, personName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /*
     * 删除redis数据
     * */
    @RequestMapping(value = "/deleteAllRedisData")
    @ApiOperation(value = "删除redis数据")
    public CommonResult<Object> deleteRedisData(String in){
   /*  for(int i=0;i<10;i++){*/
   /*      Thread re=new Thread(new Runnable() {
             @Override
             public void run() {
                 try {
                     //logger.info(Thread.currentThread().getId()+"run");
                     testLock(redissonDistributedLocker);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         });

         re.start();*/
    /* }*/
        redisService.delateAllData(in);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS),  null);

    }




}
