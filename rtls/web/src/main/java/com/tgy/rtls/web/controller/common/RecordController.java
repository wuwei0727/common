package com.tgy.rtls.web.controller.common;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.user.PersonArea;
import com.tgy.rtls.data.entity.user.PersonIncoal;
import com.tgy.rtls.data.entity.user.PersonOff;
import com.tgy.rtls.data.entity.user.PersonSub;
import com.tgy.rtls.data.service.common.RecordService;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.common
 * @date 2020/10/29
 * 项目概览 记录数据
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/record")
public class RecordController {
    @Autowired
    private RecordService recordService;

    @RequestMapping(value = "/getPersonIncoalSel")
    @ApiOperation(value = "井下人数信息查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonSel(Integer map,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,
                                             @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                             @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonIncoal> personList = recordService.findByIncal(map,departmentid,worktypeid,jobid,keyword);
                return new CommonResult<Object>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=recordService.findByIncal(map,departmentid,worktypeid,jobid,keyword).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<PersonIncoal> personList = recordService.findByIncal(map,departmentid,worktypeid,jobid,keyword);
            PageInfo<PersonIncoal> pageInfo=new PageInfo<>(personList);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPersonOffSel")
    @ApiOperation(value = "离线人数信息查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonOffSel(Integer map,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,
                                             @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                             @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonOff> personList = recordService.findByOff(map,departmentid,worktypeid,jobid,keyword);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=recordService.findByOff(map,departmentid,worktypeid,jobid,keyword).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<PersonOff> personList = recordService.findByOff(map,departmentid,worktypeid,jobid,keyword);
            PageInfo<PersonOff> pageInfo=new PageInfo<>(personList);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPersonOvertimeSel")
    @ApiOperation(value = "超时人数信息查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonOvertimeSel(Integer map,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,
                                                @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonIncoal> personList = recordService.findByOvertime(map,departmentid,worktypeid,jobid,keyword,null,null);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int num=recordService.findByOvertime(map,departmentid,worktypeid,jobid,keyword,null,null).size();
            if (num!=0) {
                if (pageIndex > num / pageSize) {
                    if (num % pageSize == 0) {
                        pageIndex = num / pageSize;
                    } else {
                        pageIndex = num / pageSize + 1;
                    }
                }
            }

            List<PersonIncoal> personList = recordService.findByOvertime(map,departmentid,worktypeid,jobid,keyword,pageIndex-1,pageSize);
            Map<String,Object> result=new HashMap<>();
            result.put("list",personList);
            result.put("pageIndex", pageIndex);
            result.put("total", num);
            result.put("pages", num/pageSize+1);
            return new CommonResult<>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getSubSel")
    @ApiOperation(value = "分站查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "num",value = "分站ID",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "网络状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "powerstate",value = "供电状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "error",value = "故障信息",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getSubSel(String num, Integer networkstate,Integer powerstate,Integer map,Integer error,
                                          @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                          @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<SubSyn> substations = recordService.findBySub(map,num,networkstate,powerstate,error);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),substations);
            }
            /*
             * 分页 total-->总数量
             * */
            int total= recordService.findBySub(map,num,networkstate,powerstate,error).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<SubSyn> substations = recordService.findBySub(map,num,networkstate,powerstate,error);
            PageInfo<SubSyn> pageInfo=new PageInfo<>(substations);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getPersonSubSel")
    @ApiOperation(value = "地图分站数信息查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "num",value = "分站id",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonSubSel(String num,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,
                                                     @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                     @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonSub> personList = recordService.findByPersonSub(num,departmentid,worktypeid,jobid,keyword);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int count= recordService.findByPersonSub(num,departmentid,worktypeid,jobid,keyword).size();
            if (pageIndex > count / pageSize) {
                if (count % pageSize == 0) {
                    pageIndex = count / pageSize;
                } else {
                    pageIndex = count / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<PersonSub> personList = recordService.findByPersonSub(num,departmentid,worktypeid,jobid,keyword);
            PageInfo<PersonSub> pageInfo=new PageInfo<>(personList);
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


    @RequestMapping(value = "/getPersonAreaSel")
    @ApiOperation(value = "地图区域数信息查询接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "area",value = "区域id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getPersonAreaSel(String area,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,
                                                     @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                                     @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询

            //pageSize<0时查询所有
            if (pageSize<0){
                List<PersonArea> personList = recordService.findByPersonArea(area,departmentid,worktypeid,jobid,keyword);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),personList);
            }
            /*
             * 分页 num-->总数量
             * */
            int count= recordService.findByPersonArea(area,departmentid,worktypeid,jobid,keyword).size();
            if (pageIndex > count / pageSize) {
                if (count % pageSize == 0) {
                    pageIndex = count / pageSize;
                } else {
                    pageIndex = count / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<PersonArea> personList = recordService.findByPersonArea(area,departmentid,worktypeid,jobid,keyword);
            PageInfo<PersonArea> pageInfo=new PageInfo<>(personList);
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

    @RequestMapping(value = "/getAreaSel")
    @ApiOperation(value = "区域查询接口",notes = "无")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "区域名",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "type",value = "区域类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "enable",value = "是否启用",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public CommonResult<Object> getAreaSel(String name, Integer type,Integer enable,Integer map,
                                          @RequestParam(value = "pageIndex",defaultValue = "1")Integer pageIndex,
                                          @RequestParam(value = "pageSize",defaultValue = "1")Integer pageSize){
        try {
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize<0){
                List<AreaVO> areaVOs = recordService.findByArea(map,name,type,enable);
                return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),areaVOs);
            }
            /*
             * 分页 total-->总数量
             * */
            int total= recordService.findByArea(map,name,type,enable).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex,pageSize);
            List<AreaVO> areaVOs = recordService.findByArea(map,name,type,enable);
            PageInfo<AreaVO> pageInfo=new PageInfo<>(areaVOs);
            Map<String,Object> result=new HashMap<>();
            result.put("list",pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            return new CommonResult<Object>(200,LocalUtil.get(KafukaTopics.QUERY_SUCCESS),result);
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }


    /*
    *导出功能
    * */

    @RequestMapping("/exportPersonIncoal")
    @ApiOperation(value = "井下人数信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int")
    })
    public void exportPersonIncoal(Integer map, String keyword, Integer departmentid, Integer worktypeid, Integer jobid,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try{
            ServletOutputStream out=response.getOutputStream();
            String fileName=new String((LocalUtil.get(KafukaTopics.PERSONINFO_INCOAL)+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportPersonIncoalToExcel(out,map,departmentid,worktypeid,jobid,keyword,title);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportPersonOff")
    @ApiOperation(value = "离线人数信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int")
    })
    public void exportPersonOff(Integer map, String keyword, Integer departmentid, Integer worktypeid, Integer jobid, String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try{
            ServletOutputStream out=response.getOutputStream();
            String fileName=new String((LocalUtil.get(KafukaTopics.PERSONINFO_OFFLINE)+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportPersonOff(out,map,departmentid,worktypeid,jobid,keyword,title);
        } catch(Exception e){
            e.printStackTrace();
        }
    }


    @RequestMapping("/exportPersonOvertime")
    @ApiOperation(value = "超时人数信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int")
    })
    public void exportPersonOvertime(Integer map, String keyword, Integer departmentid, Integer worktypeid, Integer jobid,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try{
            ServletOutputStream out=response.getOutputStream();
            String fileName=new String((LocalUtil.get(KafukaTopics.PERSONINFO_OVERTIME)+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportPersonOvertimeToExcel(out,map,departmentid,worktypeid,jobid,keyword,title);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportSub")
    @ApiOperation(value = "地图分站信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "num",value = "分站ID",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "networkstate",value = "网络状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "powerstate",value = "供电状态",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "error",value = "故障信息",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageIndex",value = "当前页",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "pageSize",value = "页面大小",required = false,dataType = "int")
    })
    public void getSubSel(String num, Integer networkstate,Integer powerstate,Integer map,Integer error,String title, HttpServletResponse response) {
        response.setContentType("application/binary;charset=UTF-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.SUB_MAP) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportexportSubToExcel(out,map,num,networkstate,powerstate,error,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportPersonSub")
    @ApiOperation(value = "地图分站人数信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "num",value = "分站id",required = true,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int")
    })
    public void exportPersonSub(String num,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.PERSONINFO_SUB)+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportPersonSub(out,num,departmentid,worktypeid,jobid,keyword,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportArea")
    @ApiOperation(value = "重点区域信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query",name = "name",value = "区域名",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "type",value = "区域类型",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "map",value = "地图id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "enable",value = "是否启用",required = false,dataType = "int")
    })
    public void exportArea(String name, Integer type,Integer enable,Integer map,String title, HttpServletResponse response) {
        response.setContentType("application/binary;charset=UTF-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String((LocalUtil.get(KafukaTopics.AREAINFO) + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportAreaToExcel(out,map,name,type,enable,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/exportPersonArea")
    @ApiOperation(value = "区域人数信息导出接口",notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query",name = "area",value = "区域id",required = true,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "keyword",value = "关键字",required = false,dataType = "String"),
            @ApiImplicitParam(paramType = "query",name = "departmentid",value = "部门id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "worktypeid",value = "工种id",required = false,dataType = "int"),
            @ApiImplicitParam(paramType = "query",name = "jobid",value = "职务id",required = false,dataType = "int")
    })
    public void exportPersonArea(String area,String keyword, Integer departmentid, Integer worktypeid, Integer jobid,String title, HttpServletResponse response){
        response.setContentType("application/binary;charset=UTF-8");
        try {
            ServletOutputStream out = response.getOutputStream();
            String fileName = new String(( LocalUtil.get(KafukaTopics.PERSONINFO_INAREA)+ new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())).getBytes(), StandardCharsets.ISO_8859_1);
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");
            recordService.exportPersonArea(out,area,departmentid,worktypeid,jobid,keyword,title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
