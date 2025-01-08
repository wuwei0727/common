package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.type.Job;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/10/14
 */
public interface JobMapper {
    /*
    * 查询实例下的职务 instanceid-->实例id
    * */
    List<Job> findByAll(@Param("instanceid")Integer instanceid);

    /*
    * 实例下职务详情
    * */
    Job findById(@Param("id")Integer id);

    List<Job> findByName(@Param("instanceid")Integer instanceid,@Param("name")String name);

    /*
    * 实例下新增职务
    * */
    int addJob(@Param("job")Job job);

    /*
    * 实例下修改职务
    * */
    int updateJob(@Param("job")Job job);

    /*
    * 实例下删除职务 ids--职务id集
    * */
    int delJob(@Param("ids")String[] ids);

    int delJobInstance(@Param("instanceid")Integer instanceid);
}
