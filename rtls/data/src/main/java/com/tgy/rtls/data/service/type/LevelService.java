package com.tgy.rtls.data.service.type;

import com.tgy.rtls.data.entity.type.Level;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type
 * @date 2020/10/15
 */
public interface LevelService {
    /*
     * 查询实例下的等级 instanceid-->实例id
     * */
    List<Level> findByAll(@Param("instanceid")Integer instanceid);

    /*
     * 实例下等级详情
     * */
    Level findById(@Param("id")Integer id);

    /*
     * 实例下新增等级
     * */
    Boolean addLevel(@Param("level")Level level);

    /*
     * 实例下修改等级
     * */
    Boolean updateLevel(@Param("level")Level level);

    /*
     * 实例下删除等级 ids-->等级id集
     * */
    Boolean delLevel(@Param("ids")String ids);

    List<Level> findLevelByName(Integer instance,String name);
}
