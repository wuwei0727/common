package com.tgy.rtls.data.mapper.user;

import com.tgy.rtls.data.entity.user.Instance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.user
 * @date 2020/10/13
 */
public interface InstanceMapper {
    /*
    *查询实例列表 userid-->登录用户id
    * */
    List<Instance> findByAll(@Param("userid")Integer userid,@Param("name")String name);

    /*
    * 实例详情
    * */
    Instance findById(@Param("id")Integer id);

    /*
    * 通过实例id查询实例名
    * */
    String findByNameId(@Param("id")Integer id);

    /*
    * 查询该权限部门拥有多少实例
    * */
    List<Instance> findByCid(@Param("cid")Integer cid);

    /*
     * 查询该权限成员拥有多少实例
     * */
    List<Instance> findByUid(@Param("uid")Integer uid);

    /*
    * 编号重名判断
    * */
    Instance findByNum(@Param("num")String num);

    /*
     * 识别码2重名判断
     * */
    Instance findByCode2(@Param("code2")String code2);

    /*
    * 添加实例
    * */
    int addInstance(@Param("instance")Instance instance);

    /*
    * 修改实例
    * */
    int updateInstance(@Param("instance")Instance instance);

    /*
    * 删除实例 id-->实例id
    * */
    int delInstance(@Param("id")Integer id);

    /*
     * 新增事件日志类型
     * */
    int addEventlogType(@Param("instanceid")Integer instanceid);

    /*
     * 删除事件日志类型
     * */
    int delEventlogType(@Param("instanceid")Integer instanceid);

    /*
    * 实例下人员的id集
    * */
    String delPerson(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下区域类型
    * */
    int delAreaType(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下标签信息
    * */
    int delTag(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下部门信息
    * */
    int delDepartment(@Param("instanceid")Integer instanceid);
    /*
     * 删除实例下工种信息
     * */
    int delWorktype(@Param("instanceid")Integer instanceid);
    /*
     * 删除实例下职务信息
     * */
    int delJob(@Param("instanceid")Integer instanceid);
    /*
     * 删除实例下等级信息
     * */
    int delLevel(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下排班信息
    * */
    int delScheduling(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下的文件信息
    * */
    int delTextrecord(@Param("instanceid")Integer instanceid);
    /*
    * 删除实例下的音频信息
    * */
    int delVoicerecord(@Param("instanceid")Integer instanceid);

}
