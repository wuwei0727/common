package com.tgy.rtls.data.mapper.map;

import com.tgy.rtls.data.entity.map.AreaDot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.map
 * @date 2020/10/21
 * 区域点管理
 */
public interface AreaDotMapper {
    /*
    * 根据区域id查找区域点集合
    * */
    List<AreaDot> findByArea(@Param("area")String area);


    /*
    *新增区域点
    * */
    int addAreaDot(@Param("dot")AreaDot dot);

    /*
    * 删除区域点
    * */
    int delAreaDot(@Param("area")String area);
}
