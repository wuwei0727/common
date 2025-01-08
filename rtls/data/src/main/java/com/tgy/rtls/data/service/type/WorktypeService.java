package com.tgy.rtls.data.service.type;

import com.tgy.rtls.data.entity.type.Worktype;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type
 * @date 2020/10/15
 */
public interface WorktypeService {
    /*
     * 查询实例下的工种 instanceid-->实例id
     * */
    List<Worktype> findByAll(Integer instanceid);

    /*
     * 实例下工种详情
     * */
    Worktype findById(Integer id);

    /*
     * 实例下新增工种
     * */
    Boolean addWorktype(Worktype worktype);

    /*
     * 实例下修改工种
     * */
    Boolean updateWorktype(Worktype worktype);

    /*
     *实例下删除工种
     * */
    Boolean delWorktype(String ids);

    List<Worktype> getWorkType(Integer instanceid,String name);

}
