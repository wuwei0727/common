package com.tgy.rtls.data.service.user;

import com.tgy.rtls.data.entity.user.Instance;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.user
 * @date 2020/11/12
 */
public interface InstanceService {

    /*
     *查询实例列表 userid-->登录用户id
     * */
    List<Instance> findByAll(Integer userid, String name);

    /*
     * 通过实例id查询实例名
     * */
    String findByNameId(Integer id);

    /*
     * 查询该权限部门拥有多少实例
     * */
    List<Instance> findByCid(Integer cid);

    /*
     * 查询该权限成员拥有多少实例
     * */
    List<Instance> findByUid(Integer uid);

    /*
     * 实例详情
     * */
    Instance findById(Integer id);


    /*
     * 编号重名判断
     * */
    Instance findByNum(String num);

    /*
     * 识别码2重名判断
     * */
    Instance findByCode2(String code2);
    /*
     * 添加实例
     * */
    boolean addInstance(Instance instance);

    /*
     * 修改实例
     * */
    boolean updateInstance(Instance instance);

    /*
     * 删除实例 id-->实例id
     * */
    boolean delInstance(Integer id);

    /*
    * 新增事件日志
    * */

}
