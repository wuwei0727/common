package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.type.Worktype;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/10/14
 */
public interface WorktypeMapper {
    /*
    * 查询实例下的工种 instanceid-->实例id
    * */
    List<Worktype> findByAll(@Param("instanceid")Integer instanceid);

    /*
    * 实例下工种详情
    * */
    Worktype findById(@Param("id")Integer id);

    /*
    * 查询工种名称
    * */
    List<Worktype> findByName(@Param("instanceid")Integer instanceid,@Param("name")String name);

    /*
    * 实例下新增工种
    * */
    int addWorktype(@Param("worktype")Worktype worktype);

    /*
    * 实例下修改工种
    * */
    int updateWorktype(@Param("worktype")Worktype worktype);

    /*
    *实例下删除工种
    * */
    int delWorktype(@Param("ids")String[] ids);

    int delWorktypeInstance(@Param("instanceid")Integer instanceid);
}
