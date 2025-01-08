package com.tgy.rtls.data.service.type.impl;

import com.tgy.rtls.data.entity.type.Worktype;
import com.tgy.rtls.data.mapper.type.WorktypeMapper;
import com.tgy.rtls.data.service.type.WorktypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type.impl
 * @date 2020/10/15
 */
@Service
@Transactional
public class WorktypeServiceImpl implements WorktypeService {
    @Autowired(required = false)
    private WorktypeMapper worktypeMapper;

    @Override
    public List<Worktype> findByAll(Integer instanceid) {
        List<Worktype> worktypes=worktypeMapper.findByAll(instanceid);
        return worktypes;
    }

    @Override
    public Worktype findById(Integer id) {
        Worktype worktype=worktypeMapper.findById(id);
        return worktype;
    }

    @Override
    public Boolean addWorktype(Worktype worktype) {
        return worktypeMapper.addWorktype(worktype)>0;
    }

    @Override
    public Boolean updateWorktype(Worktype worktype) {
        return worktypeMapper.updateWorktype(worktype)>0;
    }

    @Override
    public Boolean delWorktype(String ids) {
        String[] split=ids.split(",");
        return worktypeMapper.delWorktype(split)>0;
    }

    @Override
    public List<Worktype> getWorkType(Integer instanceid, String name) {
        return worktypeMapper.findByName(instanceid,name);
    }
}
