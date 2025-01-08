package com.tgy.rtls.data.service.type;

import com.tgy.rtls.data.entity.type.Department;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type
 * @date 2020/10/15
 */
public interface DepartmentService {
    /*
     * 查询实例下的部门 instanceid-->实例id
     * */
    List<Department> findByAll(Integer instanceid);

    /*
     * 实例下部门详情 id-->部门id
     * */
    Department findById(Integer id);

    /*
     * 实例下新增部门
     * */
    Boolean addDepartment(Department department);

    /*
     * 实例下修改部门
     * */
    Boolean updateDepartment(Department department);

    /*
     * 实例下删除部门 ids-->部门id集 1,2,3
     * */
    Boolean delDepartment(String ids);

}
