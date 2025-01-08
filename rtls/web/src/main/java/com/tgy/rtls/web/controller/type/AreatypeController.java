package com.tgy.rtls.web.controller.type;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.type.Areatype;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.map.AreaService;
import com.tgy.rtls.data.service.type.AreatypeService;
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
 * @date 2020/10/20
 * 区域类型管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/areatype")
public class AreatypeController {
    @Autowired
    private AreatypeService areatypeService;
    @Autowired
    private AreaService areaService;
    @Autowired
    private RedisService redisService;
    @RequestMapping(value = "/getAreatypeSel")
    @ApiOperation(value = "区域类型查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getAreatypeSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<Areatype> areatypeList = areatypeService.findByAll(instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),areatypeList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=areatypeService.findByAll(instanceid).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                }else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Areatype> areatypeList = areatypeService.findByAll(instanceid);
            PageInfo<Areatype> pageInfo=new PageInfo<>(areatypeList);
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

    @RequestMapping(value = "/getAreatypeId/{id}")
    @ApiOperation(value = "区域类型详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "区域类型id",required = true,dataType = "int")
    public CommonResult<Areatype> getAreatypeId(@PathVariable("id")Integer id){
        try {
            Areatype areatype=areatypeService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),areatype);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/addAreatype")
    @ApiOperation(value = "区域类型新增接口",notes = "区域类型信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "区域类型名",required = true,dataType = "String")})
    public CommonResult<Integer> addAreatype(Areatype areatype){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            areatype.setInstanceid(instanceid);
            if (areatypeService.addAreatype(areatype)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),areatype.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,e.getMessage());
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateAreatype")
    @ApiOperation(value = "区域类型修改接口",notes = "区域类型信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "id",value = "区域类型id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "区域类型名",required = true,dataType = "String")
    })
    public CommonResult updateAreatype(Areatype areatype){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            areatype.setInstanceid(instanceid);
            if (areatypeService.updateAreatype(areatype)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delAreatype/{ids}")
    @ApiOperation(value = "区域类型删除接口",notes = "区域类型id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "区域类型id集",required = true,dataType = "String")
    public CommonResult delAreatype(@PathVariable("ids")String ids){
        try {
            //删除区域类型时先判断该是否还存在该类型的区域
            List<Area> areas=areatypeService.findByAreatypeId(Integer.valueOf(ids));
            if(!NullUtils.isEmpty(areas)){
                return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_AREAFIRST));
            }
            if(areatypeService.delAreatype(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch(Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
