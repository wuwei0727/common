package com.tgy.rtls.web.controller.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.common.Operationlog;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.tool.DateUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.common
 * @date 2020/11/19
 * 操作日志
 */
@RestController
@RequestMapping(value = "/operation")
@CrossOrigin
public class OperationlogController {
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private LocalUtil localUtil;

    @RequestMapping(value = "/getOperationSel")
//    @RequiresPermissions("operationlog:sel")
    @ApiOperation(value = "操作日志查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getOperationSel(String userName,String ip,String startTime, String endTime,
                                               @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                               @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd",localUtil.getCurrentLocale());
            if(endTime!=null&&!endTime.trim().isEmpty()) {
                Date date = dateFormat.parse(endTime);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                Calendar after = DateUtils.getAfterDay(calendar);
                endTime = dateFormat.format(after.getTime());
            }
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Operationlog> operationlogList = operationlogService.findByAll(userName,ip,startTime,endTime);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),operationlogList);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=operationlogService.findByAll(userName, ip, startTime,endTime).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Operationlog> operationlogList = operationlogService.findByAll(userName, ip, startTime,endTime);
            PageInfo<Operationlog> pageInfo=new PageInfo<>(operationlogList);
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


    @RequestMapping("/exportOperation")
    @ApiOperation(value = "操作日志导出接口",notes = "输入查询条件")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "startTime",value = "开始时间",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "endTime",value = "结束时间",required = false,dataType = "String")
    })
    public void exportEventlog( String startTime, String endTime,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.OPERATION_LOG) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            operationlogService.exportOperation(out,startTime,endTime,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
