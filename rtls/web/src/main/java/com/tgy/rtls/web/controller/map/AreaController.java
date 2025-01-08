package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.map.AreaSyn;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.AreaService;
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
 * @Package com.tgy.rtls.web.controller.map
 * @date 2020/10/22
 * 区域管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/area")
public class AreaController {
    @Autowired
    private AreaService areaService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    
    @RequestMapping(value = "/getAreaSel")
    @ApiOperation(value = "区域查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "type",value = "类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "区域名",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getAreaSel(String name,Integer map,Integer type, @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                          @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<=0){
                List<Area> areas = areaService.findByAll(map,type,name);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),areas);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=areaService.findByAll(map,type,name).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Area> areas = areaService.findByAll(map,type,name);
            PageInfo<Area> pageInfo=new PageInfo<>(areas);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            Integer uid=1;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)) {
                uid = member.getUid();
            }
            operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.QUERY_AREAINFO));
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getAreaId/{id}")
    @ApiOperation(value = "区域详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "区域id",required = true,dataType = "int")
    public CommonResult<Object> getAreaId(@PathVariable("id")Integer id){
        try {
            Map<String,Object> result=areaService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/addArea")
/*    @RequiresPermissions("area:add")*/
    @ApiOperation(value = "区域新增接口",notes = "区域信息")
    public CommonResult<Integer> addArea(@RequestBody AreaSyn areaSyn){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Area area=areaService.findByName(areaSyn.getName());
            if (!NullUtils.isEmpty(area)){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.AREANAME_CONFIX));
            }
            if (areaService.addArea(areaSyn,instanceid)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.ADD_AREA)+areaSyn.getName());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),areaSyn.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateArea")
   /* @RequiresPermissions("area:update")*/
    @ApiOperation(value = "区域修改接口",notes = "区域信息")
    public CommonResult updateArea(@RequestBody AreaSyn areaSyn){
        try {
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            Area area=areaService.findByName(areaSyn.getName());
            Area area1=areaService.findByAreaId(areaSyn.getId());
            if (!NullUtils.isEmpty(area)&&!area.getName().equals(area1.getName())){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.AREANAME_CONFIX));
            }
            if (areaService.updateArea(areaSyn,instanceid)){
                operationlogService.addOperationlog(Integer.valueOf(uid),LocalUtil.get(KafukaTopics.UPDATE_AREA)+areaSyn.getName());
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),areaSyn.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delArea/{id}")
   /* @RequiresPermissions("area:del")*/
    @ApiOperation(value = "区域删除接口",notes = "区域id集")
    @ApiImplicitParam(paramType = "path",name = "id",value = "区域id",required = true,dataType = "int")
    public CommonResult delArea(@PathVariable("id")Integer id){
        try {
            Integer uid=1;
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= member.getUid();
            }
            Area area=areaService.findByAreaId(id);
            if (areaService.delArea(id,true)){

                operationlogService.addOperationlog(uid,LocalUtil.get(KafukaTopics.DELETE_AREA)+area.getName());
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
