package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.RecommConfig;
import com.tgy.rtls.data.entity.park.RecommConfigArea;
import com.tgy.rtls.data.entity.park.RecommConfigAreaPlace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.park
*@Author: wuwei
*@CreateTime: 2023-05-12 10:02
*@Description: TODO
*@Version: 1.0
*/
@Mapper
public interface RecommConfigMapper extends BaseMapper<RecommConfig> {
    int updateBatch(List<RecommConfig> list);

    int batchInsert(@Param("list") List<RecommConfig> list);

    List<RecommConfig> getAllRecommConfigOrConditionQuery(@Param("map") String map, @Param("areaname") String areaname, @Param("recommelevel") String recommelevel, @Param("desc") String desc, @Param("mapids") String[] mapids);

    int addRecommConfig(RecommConfig recommConfig);
    void addRecommConfigAreaVertexCoordinate(RecommConfigArea recommconfigareaplace);
    void addRecommConfigArea(RecommConfigArea recommconfigarea);
    void addRecommConfigAreaPlace(RecommConfigAreaPlace recommconfigareaplace);
    void delRecommConfig(String id);
    void delRecommConfigAreaHost(String id);
    void delRecommConfigByAreaId(Long id);
    void delRecommConfigAreaVertexCoordinate(String id);
    int updateRecommConfig(RecommConfig recommConfig);
    void updateRecommConfigArea(RecommConfigArea recommconfigarea);
    void updateRecommConfigAreaPlace(RecommConfigAreaPlace recommConfigAreaPlace);

    List<RecommConfig> getRecommConfigById(@Param("id") String id, @Param("placeId") String placeId, @Param("idstr") String idstr);

    List<ParkingPlace> getPlaceByPlaceNames(@Param("mapId") Integer mapId, @Param("placeName") String placeName);

    List<RecommConfig> getRecommConfigByAreaId(String id);

    RecommConfig getRecommConfigByPlaceId(@Param("map") Integer map, @Param("placeId") Integer placeId);
}