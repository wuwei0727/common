package com.tgy.rtls.data.mapper.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.Maptheme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MapthemeMapper extends BaseMapper<Maptheme> {
    int updateBatch(List<Maptheme> list);

    int batchInsert(@Param("list") List<Maptheme> list);

    int addMaptheme(Maptheme maptheme);

    int editMaptheme(Maptheme maptheme);
}