package com.tgy.rtls.check.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.mapper.check.TagcheckbsidDao;
import com.tgy.rtls.data.service.common.MonitorService;
import com.tgy.rtls.data.service.map.Map2dService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping(value = "/map")
@CrossOrigin
public class CheckMapController {

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private Map2dService map2dService;
    @Autowired(required = false)
    private TagcheckbsidDao tagcheckbsidDao;


    @RequestMapping(value = "/getMap2dSel")
    @ApiOperation(value = "2维地图查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "地图名",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getMap2dSel(String name, @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                            @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            String instanceid=null;
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<Map_2d> map2ds = map2dService.findByAll(name,1,instanceid);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map2ds);
            }
            /*
             * 分页 total-->总数量
             * */
            int total=map2dService.findByAll(name,1,instanceid).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<Map_2d> map2ds = map2dService.findByAll(name,1,instanceid);
            PageInfo<Map_2d> pageInfo=new PageInfo<>(map2ds);
            Map<String,Object> map=new HashMap<>();
            map.put("list",pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),map);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    /*
     * 获取区域下拉框信息
     * */
    @RequestMapping(value = "/getSubId/{map}")
    @ApiOperation(value = "分站下拉框信息",notes = "无")

    public CommonResult<Object> getSubId(@PathVariable("map")String map){
        try {
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),monitorService.findBySubtype(map));
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }



    public static void main(String[] args) {
         /*    String start1="2021-03-23 17:46:";
        String start2="2021-03-23 17:47:";
        String start3="2021-03-23 17:48:";
        String start4="2021-03-23 17:49:";
        List<String> sdas=new ArrayList<>();
        sdas.add(start1);
        sdas.add(start2);
        sdas.add(start3);
        sdas.add(start4);
        for(String date:sdas) {
            for (int i = 10; i < 59; ) {
                int k=i+2;
                String start=start1 + i;
                String end=start1 + k;
                List<TagcheckbsidEntity> res = tagcheckbsidDao.getLackTagid(start,end);
                System.out.println(start+"====="+end+":size"+res.size());
                i=i+2;
            }
        }*/

    }
}
