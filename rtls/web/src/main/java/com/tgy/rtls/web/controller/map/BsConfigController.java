package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.service.map.BsConfigService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.map
 * @date 2020/10/20
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/bsconfig")
public class BsConfigController {
    @Autowired
    private BsConfigService bsConfigService;

    @RequestMapping(value = "/getBsConfigSel")
    @ApiOperation(value = "分站参数查询接口,巡检小程序,蓝牙信标查询",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getBsConfigSel(@RequestParam(value = "map") Integer map,
                                               @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                               @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize){
        try {
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<BsConfig> byAll = bsConfigService.findByAll(map);
                return new CommonResult<> (200, LocalUtil.get (KafukaTopics.QUERY_SUCCESS),byAll);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=bsConfigService.findByAll(map).size ();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<BsConfig> bsConfigs = bsConfigService.findByAll(map);
            PageInfo<BsConfig> pageInfo=new PageInfo<>(bsConfigs);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<> (200, LocalUtil.get (KafukaTopics.QUERY_SUCCESS), result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<> (500, LocalUtil.get (KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBsConfigId/{id}")
    @ApiOperation(value = "分站参数详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "id",value = "分站参数id",required = true,dataType = "int")
    public CommonResult<BsConfig> getBsConfigId(@PathVariable("id")Integer id){
        try {
            BsConfig bsConfig=bsConfigService.findById(id);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),bsConfig);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getBsConfigNum/{num}")
    @ApiOperation(value = "分站编号查询详情接口",notes = "无")
    @ApiImplicitParam(paramType = "path",name = "num",value = "分站编号",required = true,dataType = "string")
    public CommonResult<BsConfig> getBsConfigNum(@PathVariable("num")String num){
        try {
            BsConfig bsConfig=bsConfigService.findByNum(num);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),bsConfig);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/updateBsConfig")
    @ApiOperation(value = "分站参数修改接口",notes = "分站参数信息")
    public CommonResult updateBsConfig(BsConfig bsConfig){
        try {
            if (bsConfigService.updateBsConfig(bsConfig)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequestMapping(value = "/delBsConfig/{ids}")
    @ApiOperation(value = "分站参数删除接口",notes = "分站参数id集")
    @ApiImplicitParam(paramType = "path",name = "ids",value = "分站参数id集",required = true,dataType = "String")
    public CommonResult delBsConfig(@PathVariable("ids")String ids){
        try {
            if (bsConfigService.delBsConfig(ids)){
                return new CommonResult(200,LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }
}
