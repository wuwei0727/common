package com.tgy.rtls.data.mapper.message;

import com.tgy.rtls.data.entity.message.WarnMap;
import com.tgy.rtls.data.entity.message.WarnRule;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.message
 * @date 2020/10/30
 * 报警规则
 */
public interface WarnRuleMapper {
    /*
    * 查询报警规则信息
    * */
    List<WarnRule> findByAll(@Param("instanceid")Integer instanceid,@Param("type")Integer type,@Param("map")Integer map,@Param("enable")Integer enable,String name);

    List<WarnMap> findByMap(@Param("map")Integer map);

    WarnRule findByType(@Param("type")Integer type,@Param("map")Integer map,@Param("enable")Integer enable);


    /*
    * 报警规则编辑
    * */
    int updateWarnRule(@Param("rule")WarnRule rule);

    /*
    * 查询报警规则
    * */
    WarnRule findByWarnRuleId(@Param("id")Integer id);

    /*
    * 新增地图时生成相关报警规则 1 2 7 10 11
    * */

    int addWarnRule(@Param("map")Integer map);

    /*
     * 删除地图时将其相关的报警信息删除
     * */
    int delWarnRecord(@Param("maps")String[] maps);

    /*
    * 删除地图时 将其报警规则也删除
    * */
    int delWarnRule(@Param("maps")String[] maps);
}
