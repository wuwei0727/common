package com.tgy.rtls.data.mapper.checkingin;

import com.tgy.rtls.data.entity.checkingin.Classgroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.checkingin
 * @date 2020/11/16
 * 班组管理
 */
public interface ClassgroupMapper {
    /*
    * 实例下班组信息查询
    * */
    List<Classgroup> findByAllLike(@Param("name")String name,@Param("instanceid")Integer instanceid,@Param("sName")String sName);



    /*
     * 实例下班组信息查询
     * */
    List<Classgroup> findByAllEqual(@Param("name")String name,@Param("instanceid")String instanceid,@Param("sName")String sName);

    /*
    * 查询实例下班组id集
    * */
    String findByClassgroup(@Param("instanceid")Integer instanceid);

    /*
    * 新增班组
    * */
    int addClassgroup(@Param("classgroup")Classgroup classgroup);

    int addPersonClassgroup(@Param("personid")String personid,@Param("classgroupid")Integer classgroupid);

    /*
    * 修改班次
    * */
    int updateClassgroup(@Param("classgroup")Classgroup classgroup);

    /*
    * 删除班次
    * */
    int delClassgroup(@Param("ids")String[] ids);

    int delPersonClassgroupId(@Param("classgroupid")Integer classgroupid);

    int delPersonClassgroup(@Param("classgroupids")String[] classgroupids);

    int delClassgroupByperson(@Param("ids")String[] ids);


}
