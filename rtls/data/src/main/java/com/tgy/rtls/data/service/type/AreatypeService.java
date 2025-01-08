package com.tgy.rtls.data.service.type;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.type.Areatype;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.type
 * @date 2020/10/20
 */
public interface AreatypeService {

    /*
     * 实例下的区域类型信息
     * */
    List<Areatype> findByAll(Integer instanceid);

    /*
     * 区域类型信息详情
     * */
    Areatype findById(Integer id);

    /*
     * 新增
     * */
    Boolean addAreatype(Areatype areatype);
    /*
     * 修改
     * */
    Boolean updateAreatype(Areatype areatype);

    /*
     * 删除实例下的区域类型
     * */
    Boolean delAreatype(String ids);

    /*
     * 通过区域类型id查询区域信息
     * */
    List<Area> findByAreatypeId(Integer id);

}
