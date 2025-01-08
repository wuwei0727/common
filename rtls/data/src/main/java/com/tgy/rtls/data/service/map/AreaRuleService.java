package com.tgy.rtls.data.service.map;

import com.tgy.rtls.data.entity.map.AreaDetection;
import com.tgy.rtls.data.entity.map.AreaOverload;
import com.tgy.rtls.data.entity.map.AreaTurnover;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map
 * @date 2020/11/25
 */
public interface AreaRuleService {
    /*
     * 查询区域下启动的规则且在开启时间范围内 area-->区域id  currentTime-->当前时间  type-->类型 0入口/进入 1出口/离开
     * */
    List<AreaTurnover> findByTurnoverEnable(Integer area,Integer type);

    List<AreaOverload> findByOverloadEnable(Integer area);

    List<AreaDetection> findByDetectionEnable(Integer area,Integer type);
    /*
     * 判断人员是否属于进出区域规则的白名单
     * */
    int findByWhitelist(Integer turnoverid,Integer personid);

}
