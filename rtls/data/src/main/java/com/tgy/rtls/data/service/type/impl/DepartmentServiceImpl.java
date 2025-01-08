package com.tgy.rtls.data.service.type.impl;

import com.tgy.rtls.data.entity.type.Department;
import com.tgy.rtls.data.mapper.type.DepartmentMapper;
import com.tgy.rtls.data.service.type.DepartmentService;
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
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired(required = false)
    private DepartmentMapper departmentMapper;

    @Override
    public List<Department> findByAll(Integer instanceid) {
        List<Department> departments=departmentMapper.findByAll(instanceid);
        return departments;
    }

    @Override
    public Department findById(Integer id) {
        Department department=departmentMapper.findById(id);
        return department;
    }

    @Override
    public Boolean addDepartment(Department department) {
        return departmentMapper.addDepartment(department)>0;
    }

    @Override
    public Boolean updateDepartment(Department department) {
        return departmentMapper.updateDepartment(department)>0;
    }

    @Override
    public Boolean delDepartment(String ids) {
        String[] split=ids.split(",");
        return departmentMapper.delDepartment(split)>0;
    }
}
