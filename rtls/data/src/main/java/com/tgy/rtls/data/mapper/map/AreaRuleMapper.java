package com.tgy.rtls.data.mapper.map;

import com.tgy.rtls.data.entity.map.AreaDetection;
import com.tgy.rtls.data.entity.map.AreaOverload;
import com.tgy.rtls.data.entity.map.AreaTurnover;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.map
 * @date 2020/10/21
 * 区域规则管理
 */
public interface AreaRuleMapper {

    List<AreaTurnover> findByTurnover(@Param("area")Integer area);

    List<AreaOverload> findByOverload(@Param("area")Integer area);

    List<AreaDetection> findByDetection(@Param("area")Integer area);

    /*
    * 新增进出报警规则
    * */
    int addAreaTurnover(@Param("turnover")AreaTurnover turnover);

    /*
    * 新增超员报警规则
    * */
    int addAreaOverload(@Param("overload")AreaOverload overload);

    /*
    * 新增出入口检测规则
    * */
    int addAreaDetection(@Param("detection")AreaDetection detection);

    /*
     * 删除进出报警规则
     * */
    int delAreaTurnover(@Param("id")Integer id);

    int delAreaTurnoverArea(@Param("area")Integer area);

    /*
     * 删除超员报警规则
     * */
    int delAreaOverload(@Param("id")Integer id);

    int delAreaOverloadArea(@Param("area")Integer area);

    /*
     * 删除出入口检测规则
     * */
    int delAreaDetection(@Param("id")Integer id);

    int delAreaDetectionArea(@Param("area")Integer area);


    /*
    * 新增白名单 turnoverid-->进出报警规则id personid-->人员id
    * */
    int addWhitelist(@Param("turnoverid")Integer turnoverid,@Param("personid")Integer personid);

    /*
    * 删除白名单
    * */
    int delWhitelist(@Param("turnoverid")Integer turnoverid);

    /*
     * 查询区域下启动的规则且在开启时间范围内 area-->区域id  currentTime-->当前时间  type-->类型 0入口/进入 1出口/离开
     * */
    List<AreaTurnover> findByTurnoverEnable(@Param("area")Integer area,@Param("type")Integer type);

    List<AreaOverload> findByOverloadEnable(@Param("area")Integer area);

    List<AreaDetection> findByDetectionEnable(@Param("area")Integer area,@Param("type")Integer type);

    /*
    * 判断人员是否属于进出区域规则的白名单
    * */
    int findByWhitelist(@Param("turnoverid")Integer turnoverid,@Param("personid")Integer personid);

}
