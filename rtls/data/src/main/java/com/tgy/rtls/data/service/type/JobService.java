package com.tgy.rtls.data.service.type;

import com.tgy.rtls.data.entity.type.Job;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type
 * @date 2020/10/15
 */
public interface JobService {
    /*
     * 查询实例下的职务 instanceid-->实例id
     * */
    List<Job> findByAll(Integer instanceid);

    /*
     * 实例下职务详情
     * */
    Job findById(Integer id);

    /*
     * 实例下新增职务
     * */
    Boolean addJob(Job job);

    /*
     * 实例下修改职务
     * */
    Boolean updateJob(Job job);

    /*
     * 实例下删除职务 ids--职务id集
     * */
    Boolean delJob(String ids);

    List<Job> findJobByName(Integer instance,String name);

}
