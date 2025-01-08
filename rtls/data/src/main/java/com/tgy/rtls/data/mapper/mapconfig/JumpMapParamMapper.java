package com.tgy.rtls.data.mapper.mapconfig;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.mapconfig.JumpMapParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JumpMapParamMapper extends BaseMapper<JumpMapParam> {
    int updateBatch(List<JumpMapParam> list);

    int batchInsert(@Param("list") List<JumpMapParam> list);

    List<JumpMapParam> getMapconfig(@Param("keyword")String keyword, @Param("desc")String desc,@Param("mapId")Integer mapId, @Param("maps")String[] maps);
}