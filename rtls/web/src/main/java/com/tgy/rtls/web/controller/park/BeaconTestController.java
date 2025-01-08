package com.tgy.rtls.web.controller.park;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.BeaconTest;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.BeaconTestService;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.park
 * @Author: wuwei
 * @CreateTime: 2022-12-09 11:41
 * @Description: TODO
 * @Version: 1.0
 * 信标测试
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/beaconTest")
public class BeaconTestController {
    @Autowired
    private BeaconTestService beaconTestService;
    @Autowired
    private OperationlogService operationlogService;


    @RequestMapping(value = "/addBeaconTest")
    @ApiOperation(value = "分站新增接口", notes = "分站信息")
    public CommonResult<Object> addBeaconTest(@RequestBody List<BeaconTest> beaconTest) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            if(beaconTest.get(0).getTimestamp()!=null){

                beaconTestService.batchInsert(beaconTest);
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.ADD_SUB) + beaconTest.get(0).getId());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS),beaconTest.get(0).getId());
            }
            beaconTest.get(0).setTimestamp(beaconTest.get(0).getTimestamp());
            beaconTestService.batchInsert(beaconTest);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

}
