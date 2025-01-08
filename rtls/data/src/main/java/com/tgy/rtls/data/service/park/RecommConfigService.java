package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.RecommConfig;
import com.tgy.rtls.data.entity.park.RecommConfigArea;
import com.tgy.rtls.data.entity.park.RecommConfigAreaPlace;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-12 10:02
 * @Description: TODO
 * @Version: 1.0
 */
public interface RecommConfigService extends IService<RecommConfig> {


    int updateBatch(List<RecommConfig> list);

    int batchInsert(List<RecommConfig> list);

    List<RecommConfig> getAllRecommConfigOrConditionQuery(String map, String areaname, String recommelevel, String desc, String[] mapids);

    void addRecommConfig(RecommConfig recommConfig);

    void addRecommConfigAreaVertexCoordinate(RecommConfigArea recommconfigarea);
    void addRecommConfigArea(RecommConfigArea recommconfigarea);
    void addRecommConfigAreaPlace(RecommConfigAreaPlace recommconfigareaplace);
    void delRecommConfig(String[] ids);
    void delRecommConfigByAreaId(Long ids);

    void updateRecommConfig(RecommConfig recommConfig);
    void updateRecommConfigArea(RecommConfigArea recommconfigarea);
    void updateRecommConfigAreaPlace(RecommConfigAreaPlace recommconfigareaplace);

    List<RecommConfig> getRecommConfigById(String id,String placerId,String idstr);
    List<RecommConfig> getRecommConfigByAreaId(String id);

    List<ParkingPlace> getPlaceByPlaceNames(Integer mapId, String placeName);

    RecommConfig getRecommConfigByPlaceId(Integer map, Integer placeId);
}
