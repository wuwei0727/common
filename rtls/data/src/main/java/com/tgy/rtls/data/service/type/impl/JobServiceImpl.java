package com.tgy.rtls.data.service.type.impl;

import com.tgy.rtls.data.entity.type.Job;
import com.tgy.rtls.data.mapper.type.JobMapper;
import com.tgy.rtls.data.service.type.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type.impl
 * @date 2020/10/15
 */
@Service
@Transactional
public class JobServiceImpl implements JobService {
    @Autowired(required = false)
    private JobMapper jobMapper;

    @Override
    public List<Job> findByAll(Integer instanceid) {
        List<Job> jobList=jobMapper.findByAll(instanceid);
        return jobList;
    }

    @Override
    public Job findById(Integer id) {
        Job job=jobMapper.findById(id);
        return job;
    }

    @Override
    public Boolean addJob(Job job) {
        return jobMapper.addJob(job)>0;
    }

    @Override
    public Boolean updateJob(Job job) {
        return jobMapper.updateJob(job)>0;
    }

    @Override
    public Boolean delJob(String ids) {
        String[] split=ids.split(",");
        return jobMapper.delJob(split)>0;
    }

    @Override
    public List<Job> findJobByName(Integer instance, String name) {
        return jobMapper.findByName(instance,name);
    }
}
