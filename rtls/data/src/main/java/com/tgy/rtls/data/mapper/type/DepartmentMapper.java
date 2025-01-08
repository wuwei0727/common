package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.type.Department;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/10/14
 */
public interface DepartmentMapper {
    /*
    * 查询实例下的部门 instanceid-->实例id
    * */
    List<Department> findByAll(@Param("instanceid")Integer instanceid);

    /*
    * 实例下部门详情 id-->部门id
    * */
    Department findById(@Param("id")Integer id);

    List<Department> findByName(@Param("instanceid")Integer instanceid,@Param("name")String name);

    /*
    * 实例下新增部门
    * */
    int addDepartment(@Param("department")Department department);

    /*
    * 实例下修改部门
    * */
    int updateDepartment(@Param("department")Department department);

    /*
    * 实例下删除部门 ids-->部门id集
    * */
    int delDepartment(@Param("ids")String[] ids);

    int delDepartmentInstance(@Param("instanceid")Integer instanceid);
}
