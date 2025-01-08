package com.tgy.rtls.web.controller.type;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.type.Level;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.type.LevelService;
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
 * 等级管理类
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/level")
public class LevelController {
    @Autowired
    private LevelService levelService;
    @Autowired
    private RedisService redisService;

    @RequestMapping(value = "/getLevelSel")
    @ApiOperation(value = "等级查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getLevelSel(@RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
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
                List<Level> levelList = levelService.findByAll(instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),levelList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=levelService.findByAll(instanceid).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Level> levelList = levelService.findByAll(instanceid);
            PageInfo<Level> pageInfo=new PageInfo<>(levelList);
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

    @RequestMapping(value = "/getLevelId/{id}")
    @ApiOperation(value = "等级详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "等级id",required = true,dataType = "int")
    public CommonResult<Level> getLevelId(@PathVariable("id")Integer id){
        try {
            Level level=levelService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),level);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,e.getMessage());
        }
    }

    @RequestMapping(value = "/addLevel")
    @ApiOperation(value = "等级新增接口",notes = "等级信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "等级名",required = true,dataType = "String")
    })
    public CommonResult<Integer> addLevel(Level level){
        try {
            //实例
            String uid="12";
            if (level.getName()==null||level.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.LEVEL_EMPTY));
            }
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Level> list = levelService.findLevelByName(instanceid, level.getName().trim());
            if(list!=null&&list.size()>0){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.LEVEL_EXIST));
            }
            level.setInstanceid(instanceid);
            if (levelService.addLevel(level)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.ADD_SUCCESS),level.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<Integer>(500,e.getMessage());
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateLevel")
    @ApiOperation(value = "等级修改接口",notes = "等级信息")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "id",value = "等级id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "name",value = "等级名",required = true,dataType = "String")
    })
    public CommonResult updateLevel(Level level){
        try {
            //实例
            String uid="12";
            Member member=(Member) SecurityUtils.getSubject().getPrincipal();
            if(!NullUtils.isEmpty(member)){
                uid= String.valueOf(member.getUid());
            }
            if (level.getName()==null||level.getName().trim().isEmpty()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.LEVEL_EMPTY));
            }
            int instanceid= Integer.parseInt(redisService.get("instance"+uid));
            List<Level> list = levelService.findLevelByName(instanceid, level.getName().trim());
            if(list!=null&&list.size()>1||list.size()==1&&list.get(0).getId().intValue()!=level.getId().intValue()){
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.LEVEL_EXIST));
            }
            level.setInstanceid(instanceid);
            if (levelService.updateLevel(level)){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),level.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delLevel/{ids}")
    @ApiOperation(value = "等级删除接口",notes = "等级id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "等级id集",required = true,dataType = "String")
    public CommonResult delLevel(@PathVariable("ids")String ids){
        try {
            if (levelService.delLevel(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
