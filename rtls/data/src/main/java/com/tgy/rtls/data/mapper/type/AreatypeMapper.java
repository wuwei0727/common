package com.tgy.rtls.data.mapper.type;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.type.Areatype;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.type
 * @date 2020/10/20
 */
public interface AreatypeMapper {
    /*
    * 实例下的区域类型信息
    * */
    List<Areatype> findByAll(@Param("instanceid")Integer instanceid);

    /*
    * 区域类型信息详情
    * */
    Areatype findById(@Param("id")Integer id);

    /*
    * 新增
    * */
    int addAreatype(@Param("areatype")Areatype areatype);
    /*
    * 修改
    * */
    int updateAreatype(@Param("areatype")Areatype areatype);

    /*
    * 删除实例下的区域类型
    * */
    int delAreatype(@Param("ids")String[] ids);

    int delAreatypeInstance(@Param("instanceid")Integer instanceid);

    /*
    * 通过区域类型id查询区域信息
    * */
    List<Area> findByAreatypeId(@Param("id")Integer id);

}
