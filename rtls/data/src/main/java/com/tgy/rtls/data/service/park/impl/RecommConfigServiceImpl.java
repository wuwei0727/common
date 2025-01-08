package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.RecommConfig;
import com.tgy.rtls.data.entity.park.RecommConfigArea;
import com.tgy.rtls.data.entity.park.RecommConfigAreaPlace;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.park.RecommConfigMapper;
import com.tgy.rtls.data.service.park.RecommConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.vip
 * @Author: wuwei
 * @CreateTime: 2023-05-12 10:02
 * @Description: TODO
 * @Version: 1.0
 */
@Service
public class RecommConfigServiceImpl extends ServiceImpl<RecommConfigMapper, RecommConfig> implements RecommConfigService {

    @Autowired
    private RecommConfigMapper recommConfigMapper;
    @Autowired
    private ParkMapper parkMapper;

    @Override
    public int updateBatch(List<RecommConfig> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int batchInsert(List<RecommConfig> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public List<RecommConfig> getAllRecommConfigOrConditionQuery(String map, String areaname, String recommelevel, String desc, String[] mapids) {
        return recommConfigMapper.getAllRecommConfigOrConditionQuery(map, areaname, recommelevel, desc, mapids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecommConfig(RecommConfig recommConfig) {
        RecommConfigArea area = new RecommConfigArea();
        if(recommConfigMapper.addRecommConfig(recommConfig)>0){
            area.setRecommConfigId(recommConfig.getId());
            this.addRecommConfigAreaVertexCoordinate(area);
            area.setAreaId(area.getId());
            if (!NullUtils.isEmpty(recommConfig.getVertexInfo())) {
                for (int i = 0; i < recommConfig.getVertexInfo().size(); i++) {
                    RecommConfigArea recommConfigArea1 = recommConfig.getVertexInfo().get(i);
                    for (RecommConfigArea recommConfigArea2 : recommConfigArea1.getPoints()) {
                        area.setX(recommConfigArea2.getX());
                        area.setY(recommConfigArea2.getY());
                        area.setFloor(recommConfigArea1.getFloor());

                        area.setAreaQuFen(String.valueOf(i + 1));
                        this.addRecommConfigArea(area);
                    }

                }
            }

            if (!NullUtils.isEmpty(recommConfig.getPlaceList())) {
                for (String placeName : recommConfig.getPlaceList().split(",")) {
                    ParkingPlace placeByPlaceNames = parkMapper.getPlaceByPlaceNames(Integer.valueOf(recommConfig.getMap()), placeName.replaceAll("\\(.*?\\)", ""),null,null);
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        RecommConfigAreaPlace areaPlace = new RecommConfigAreaPlace();
                        areaPlace.setAreaid(area.getAreaId());
                        areaPlace.setPlaceid(Long.valueOf(placeByPlaceNames.getId()));
                        this.addRecommConfigAreaPlace(areaPlace);
                    }
                }
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void test(RecommConfigArea recommconfigarea) {
        this.addRecommConfigAreaVertexCoordinate(recommconfigarea);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecommConfigAreaVertexCoordinate(RecommConfigArea recommconfigarea) {
        recommConfigMapper.addRecommConfigAreaVertexCoordinate(recommconfigarea);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecommConfigArea(RecommConfigArea recommconfigarea) {
        recommConfigMapper.addRecommConfigArea(recommconfigarea);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addRecommConfigAreaPlace(RecommConfigAreaPlace recommconfigareaplace) {
        recommConfigMapper.addRecommConfigAreaPlace(recommconfigareaplace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRecommConfig(String[] ids) {
        for (String id : ids) {
            recommConfigMapper.delRecommConfigByAreaId(Long.valueOf(id));
            recommConfigMapper.delRecommConfigAreaVertexCoordinate(id);
            recommConfigMapper.delRecommConfigAreaHost(id);
            recommConfigMapper.delRecommConfig(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delRecommConfigByAreaId(Long ids) {
        recommConfigMapper.delRecommConfigByAreaId(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRecommConfig(RecommConfig recommConfig) {
        recommConfig.setEndtime(LocalDateTime.now());
        if(recommConfigMapper.updateRecommConfig(recommConfig)>0){
            List<RecommConfig> recommConfigById = this.getRecommConfigByAreaId(String.valueOf(recommConfig.getId()));
            RecommConfigArea area = new RecommConfigArea();
            int count = 0;
            if (!NullUtils.isEmpty(recommConfig.getVertexInfo())) {
                recommConfigMapper.delRecommConfigAreaVertexCoordinate(String.valueOf(recommConfigById.get(0).getAreaid()));
                for (int i = 0; i < recommConfig.getVertexInfo().size(); i++) {
                    RecommConfigArea recommConfigArea1 = recommConfig.getVertexInfo().get(i);
                    for (RecommConfigArea recommConfigArea2 : recommConfigArea1.getPoints()) {
                        area.setX(recommConfigArea2.getX());
                        area.setY(recommConfigArea2.getY());
                        area.setFloor(recommConfigArea1.getFloor());
                        area.setAreaId(recommConfig.getId());
                        area.setAreaQuFen(String.valueOf(i + 1));
                        this.addRecommConfigArea(area);
                        count++;
                    }
                }
            }
            if (!NullUtils.isEmpty(recommConfig.getPlaceList())) {
                this.delRecommConfigByAreaId(recommConfigById.get(0).getAreaid());
                for (String placeName : recommConfig.getPlaceList().split(",")) {
                    ParkingPlace placeByPlaceNames = parkMapper.getPlaceByPlaceNames(Integer.valueOf(recommConfig.getMap()), placeName,null,null);
                    if (!NullUtils.isEmpty(placeByPlaceNames)) {
                        RecommConfigAreaPlace areaPlace = new RecommConfigAreaPlace();
                        areaPlace.setAreaid(recommConfigById.get(0).getAreaid());
                        areaPlace.setPlaceid(Long.valueOf(placeByPlaceNames.getId()));
                        this.addRecommConfigAreaPlace(areaPlace);
                    }
                }
            }
        }
    }

    @Override
    public void updateRecommConfigArea(RecommConfigArea recommconfigarea) {
        recommConfigMapper.updateRecommConfigArea(recommconfigarea);
    }

    @Override
    public void updateRecommConfigAreaPlace(RecommConfigAreaPlace recommconfigareaplace) {
        recommConfigMapper.updateRecommConfigAreaPlace(recommconfigareaplace);
    }

    @Override
    public List<RecommConfig> getRecommConfigById(String id,String placerId,String idstr) {
        return recommConfigMapper.getRecommConfigById(id,placerId,idstr);
    }

    @Override
    public List<RecommConfig> getRecommConfigByAreaId(String id) {
        return recommConfigMapper.getRecommConfigByAreaId(id);
    }

    @Override
    public List<ParkingPlace> getPlaceByPlaceNames(Integer mapId, String placeName) {
        return recommConfigMapper.getPlaceByPlaceNames(mapId, placeName);
    }

    @Override
    public RecommConfig getRecommConfigByPlaceId(Integer map, Integer placeId) {
        return recommConfigMapper.getRecommConfigByPlaceId(map,placeId);
    }
}
