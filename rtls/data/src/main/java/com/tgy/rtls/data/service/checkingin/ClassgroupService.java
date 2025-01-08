package com.tgy.rtls.data.service.checkingin;

import com.tgy.rtls.data.entity.checkingin.Classgroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.checkingin
 * @date 2020/11/16
 */
public interface ClassgroupService {
    /*
     * 实例下班组信息查询
     * */
    List<Classgroup> findByAllLike(String name,Integer instanceid);


    /*
     * 实例下班组信息查询
     * */
    List<Classgroup> findByAllEqual(String name,String instanceid);

    /*
     * 新增班组
     * */
    boolean addClassgroup(Classgroup classgroup,String personids);

    /*
     * 修改班次
     * */
    boolean updateClassgroup(Classgroup classgroup,String personids);

    /*
     * 删除班次
     * */
    boolean delClassgroup(String ids);

    /*
    * 删除实例下班次信息
    * */
    boolean delClassgroupInstanceid(@Param("instanceid")Integer instanceid);
}
