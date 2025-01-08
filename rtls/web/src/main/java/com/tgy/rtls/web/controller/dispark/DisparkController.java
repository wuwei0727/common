package com.tgy.rtls.web.controller.dispark;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.dispark
 * @date 2020/12/8
 */
@RestController
@RequestMapping(value = "/dispark")
public class DisparkController {
    @Autowired
    private SubService subService;
    @Autowired
    private BsConfigService bsConfigService;

    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    LocalUtil localUtil;
    /*
    * 添加分站信息和分站参数信息
    * */
    @RequestMapping(value = "/addSub")
    @ApiOperation(value = "分站新增接口",notes = "分站信息")
    public CommonResult<Object> addSub(BsConfig bsConfig){
        try {
            Substation substation1=subMapper.findByNum(bsConfig.getNum(),localUtil.getLocale());
            if (substation1!=null){
                return new CommonResult<>(400,LocalUtil.get(KafukaTopics.SUB_INUSE));
            }
            if (subService.addDisparkSub(bsConfig,bsConfig.getNum())){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),bsConfig.getId());
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400,LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @RequestMapping(value = "/updateBsConfig")
    @ApiOperation(value = "分站参数修改接口",notes = "分站参数信息")
    public CommonResult updateBsConfig(BsConfig bsConfig){
        try {
            if (bsConfigService.updateBsConfig(bsConfig,bsConfig.getNum())){
                return new CommonResult<>(200,LocalUtil.get(KafukaTopics.UPDATE_SUCCESS),bsConfig);
            }
        }catch (Exception e){
            e.printStackTrace();
            return new CommonResult<>(500,LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400,LocalUtil.get(KafukaTopics.UPDATE_FAIL));
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

}
