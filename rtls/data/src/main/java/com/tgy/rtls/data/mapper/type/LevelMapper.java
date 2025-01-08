package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.type.Level;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/10/14
 */
public interface LevelMapper {
    /*
    * 查询实例下的等级 instanceid-->实例id
    * */
    List<Level> findByAll(@Param("instanceid")Integer instanceid);

    /*
    * 实例下等级详情
    * */
    Level findById(@Param("id")Integer id);

   List< Level> findByName(@Param("instanceid")Integer instanceid,@Param("name")String name);
    /*
    * 实例下新增等级
    * */
    int addLevel(@Param("level")Level level);

    /*
    * 实例下修改等级
    * */
    int updateLevel(@Param("level")Level level);

    /*
    * 实例下删除等级 ids-->等级id集
    * */
    int delLevel(@Param("ids")String[] ids);

    int delLevelInstance(@Param("instanceid")Integer instanceid);

}
