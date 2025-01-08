package com.tgy.rtls.web.controller.routing;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.routing.Route;
import com.tgy.rtls.data.entity.routing.RouteData;
import com.tgy.rtls.data.entity.routing.Routedot;
import com.tgy.rtls.data.entity.routing.Routetask;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.routing.RouteRecordService;
import com.tgy.rtls.data.service.routing.RouteService;
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
 * @Package com.tgy.rtls.web.controller.routing
 * @date 2020/11/23
 * 巡检管理
 */
@RestController
@RequestMapping(value = "/route")
@CrossOrigin
public class RouteController {
    @Autowired
    private RouteService routeService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RouteRecordService routeRecordService;
    
    @RequestMapping(value = "/getRouteSel")
 /*   @RequiresPermissions("router:sel")*/
    @ApiOperation(value = "巡检路线查询接口",notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getRouteSel(Integer map, @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                 @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Route> routes = routeService.findByAll(map,instanceid);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),routes);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=routeService.findByAll(map,instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Route> routes = routeService.findByAll(map,instanceid);
            PageInfo<Route> pageInfo=new PageInfo<>(routes);
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

    @RequestMapping(value = "/addRoute")
  /*  @RequiresPermissions("router:add")*/
    @ApiOperation(value = "巡检路线新增接口",notes = "巡检路线信息")
    public CommonResult<Integer> addRoute(@RequestBody Route route){
        try {
            if (NullUtils.isEmpty(route.getRoutedots())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.LINECHECKLINE_FIRST));
            }
            if(route.getName()==null|| route.getName().trim().equals("")){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }

            List<Route> sameName = routeService.findByRouteNameAndMap(route.getMap(), route.getName());
            if(sameName!=null&&sameName.size()>0){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }
            
            if (routeService.addRoute(route)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),route.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateRoute")
 /*   @RequiresPermissions("router:update")*/
    @ApiOperation(value = "巡检路线修改接口",notes = "巡检路线信息")
    public CommonResult updateRoute(Route route){
        try {
            if(route.getName()==null|| route.getName().trim().equals("")){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_EMPTY));
            }

            List<Route> sameName = routeService.findByRouteNameAndMap(route.getMap(), route.getName());
            if(sameName!=null&&sameName.size()>0&&!route.getId().equals(sameName.get(0).getId())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.NAME_CONFLICT));
            }

            if (routeService.updateRoute(route)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),route.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/updateRoutedot")
  /*  @RequiresPermissions("router:update")*/
    @ApiOperation(value = "巡检点修改接口",notes = "巡检路线信息")
    public CommonResult updateRoutedot(Routedot routedot){
        try {
            if (routeService. updateRoutedot(routedot)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),routedot.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delRoute/{id}")
   /* @RequiresPermissions("router:del")*/
    @ApiOperation(value = "巡检路线删除接口",notes = "巡检路线id集")
    @ApiImplicitParam(paramType = "path",name = "id",value = "巡检路线id",required = true,dataType = "int")
    public CommonResult delRoute(@PathVariable("id")Integer id){
        try {
            if (routeService.delRoute(id)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/delRoutedot/{id}")
 /*   @RequiresPermissions("router:update")*/
    @ApiOperation(value = "巡检点删除接口",notes = "巡检点id集")
    @ApiImplicitParam(paramType = "path",name = "id",value = "巡检点id",required = true,dataType = "int")
    public CommonResult delRoutedot(@PathVariable("id")Integer id){
        try {
            if (routeService.delRoutedot(id)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/getRouteTaskSel/{month}")
/*    @RequiresPermissions("routertask:sel")*/
    @ApiOperation(value = "巡检任务查询接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "month",value = "年月",required = true,dataType = "string")
    public CommonResult<Object> getWorkorderId(@PathVariable("month")String month){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Routetask> routetasks=routeRecordService.findByTask(month,instanceid);
            List<Route> routes=routeRecordService.findByRoute(month,instanceid);
            Map<String,Object> map=new HashMap<>();
            map.put("routetasks",routetasks);//该月份的巡检任务
            map.put("routes",routes);//该月份的使用过的路线
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addRoutetask")
 /*   @RequiresPermissions("routertask:add")*/
    @ApiOperation(value = "巡检任务新增接口",notes = "巡检路线信息")
    public CommonResult<Integer> addRoutetask(Routetask routetask){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            routetask.setInstanceid(instanceid);
            if (routeRecordService.addRoutetask(routetask)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),routetask.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateRoutetask")
/*    @RequiresPermissions("routertask:add")*/
    @ApiOperation(value = "巡检任务修改接口",notes = "巡检路线信息")
    public CommonResult<Integer> updateRoutetask(Routetask routetask){
        try {
            if (routeRecordService.updateRoutetask(routetask)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),routetask.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delRoutetask")
  /*  @RequiresPermissions("routertask:del")*/
    @ApiOperation(value = "巡检任务删除接口",notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "id",value = "路线id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = false,dataType = "string")
    })
    public CommonResult delRoutetask(Integer id,String month){
        try {
            if (routeRecordService.delRoutetask(id,month)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/getRouteRecordSel")
   /* @RequiresPermissions("routerexcel:sel")*/
    @ApiOperation(value = "巡检报表查询接口",notes = "无")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "name",value = "路线名",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字(姓名/工号)",required = false,dataType = "string")
    })
    public CommonResult<Object> getRouteRecordSel(String month,String name,String keyword,Integer map){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<RouteData> routeData=routeRecordService.findByRouteRecord(month,name,keyword,map,instanceid);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),routeData);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 巡检报表导出
     * */

    @RequestMapping("/exportRouteRecord")
 /*   @RequiresPermissions("routerexcel:export")*/
    @ApiOperation(value = "巡检报表导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "name",value = "路线名",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字(姓名/工号)",required = false,dataType = "string"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "所属地图",required = false,dataType = "string")
    })
    public void exportRouteRecord(String month,String name,String keyword,Integer map, String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.LINECHECK_EXCEL) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            routeRecordService.exportRouteRecord(out,month,name,keyword,map,instanceid,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 巡检任务导出
     * */

    @RequestMapping("/exportRouteTask")
    /*@RequiresPermissions("routerexcel:export")*/
    @ApiOperation(value = "巡检报表导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "month",value = "年月",required = false,dataType = "string"),
    })
    public void exportRouteTask(String month, String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.LINECHECK_TASK)  + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            routeRecordService.exportRouteTask(out,month,instanceid,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
