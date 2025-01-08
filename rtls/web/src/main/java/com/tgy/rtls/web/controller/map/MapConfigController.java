package com.tgy.rtls.web.controller.map;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.mapconfig.JumpMapParam;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.mapconfig.JumpMapParamService;
import io.swagger.annotations.Api;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.mapconfig
 * @Author: wuwei
 * @CreateTime: 2022-11-22 16:58
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("mapConfig")
@Api(value = "用于小程序地图配置接口")
public class MapConfigController {
    @Autowired
    private JumpMapParamService jumpMapParamService;

    /**
     * 得到地图配置
     *
     * @param keyword   关键字
     * @param desc      desc
     * @param pageIndex 页面索引
     * @param pageSize  页面大小
     * @param maps      地图
     * @return {@link CommonResult}<{@link Object}>
     */
    @RequestMapping(value = "/getMapConfig")
    @ApiOperation(value = "web端app查询用户接口", notes = "输入查询条件")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "keyword", value = "关键字", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "desc", value = "排序", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "enabled", value = "是否启用", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageIndex", value = "当前页", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "页面大小", required = false, dataType = "int")
    })
    public CommonResult<Object> getMapConfig(String keyword,
                                             @RequestParam(value = "desc ", defaultValue = "createdTime desc") String desc,
                                             @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,
                                             @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize,Integer mapId,String[] maps){
        try {
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            List<JumpMapParam> getAllMapconfig = jumpMapParamService.getMapconfig(keyword, desc,mapId,maps);
            if (NullUtils.isEmpty(getAllMapconfig)) {
                return new CommonResult<>(400, LocalUtil.get("当前账号没有配置小程序跳转地图的参数！！！"));
            }
            if (pageSize < 0) {
                List<JumpMapParam> sysUsers = jumpMapParamService.getMapconfig(keyword, desc,mapId,maps);
                //查询成功
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), sysUsers);
            }
            int num = jumpMapParamService.getMapconfig(keyword, desc,mapId,maps).size();
            if (pageIndex > num / pageSize) {
                if (num % pageSize == 0) {
                    pageIndex = num / pageSize;
                } else {
                    pageIndex = num / pageSize + 1;
                }
            }
            List<JumpMapParam> mapParams = jumpMapParamService.getMapconfig(keyword, desc,mapId,maps);
            PageHelper.startPage(pageIndex, pageSize);
            PageInfo<JumpMapParam> pageInfo = new PageInfo<>(mapParams);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("pageIndex", pageIndex);
            map.put("total", pageInfo.getTotal());
            map.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map);
        } catch (Exception e) {
            e.printStackTrace();
            //系统繁忙
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions({"appUser:add"})
    @RequestMapping(value = "/addAppUser")
    @ApiOperation(value = "web端app新增用户接口", notes = "用户信息")
    public CommonResult<Integer> addAppUser(JumpMapParam mapParam) {
        try {
            if (!"".equals(mapParam.getAppId()) && null==mapParam.getAppId()) {
                return new CommonResult<>(400, LocalUtil.get("appId为空！"));
            }
            if (!"".equals(mapParam.getAppSecret()) && null==mapParam.getAppSecret()) {
                return new CommonResult<>(400, LocalUtil.get("appSecret为空！"));
            }
            if (!"".equals(mapParam.getName())&&null==mapParam.getName()) {
                return new CommonResult<>(400, LocalUtil.get("小程序地图名字为空！"));
            }
            if(!"".equals(mapParam.getShortLink())&&null==mapParam.getShortLink()){
                return new CommonResult<>(400, LocalUtil.get("小程序地图跳转链接为空！"));
            }
            if(!"".equals(mapParam.getMapId())&&null==mapParam.getMapId()){
                return new CommonResult<>(400, LocalUtil.get("请选择你要绑定的地图！"));
            }
            boolean save = jumpMapParamService.save(mapParam);
            if(save){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }
}
