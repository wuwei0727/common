package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.park.ShowScreenConfigAreaPlace;
import com.tgy.rtls.data.entity.park.ShowScreenConfigArea;
import com.tgy.rtls.data.mapper.park.ShowScreenConfigMapper;
import com.tgy.rtls.data.service.park.ShowScreenConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class ShowScreenConfigServiceImpl extends ServiceImpl<ShowScreenConfigMapper, ShowScreenConfig> implements ShowScreenConfigService {

    @Autowired
    private ShowScreenConfigMapper showScreenConfigMapper;


    @Override
    public List<ShowScreenConfig> getAllShowScreenConfigOrConditionQuery(String map, String deviceNum, String screenNum, String screenPosition, String bindArea, String desc, String floorName, String[] mapids) {
        return showScreenConfigMapper.getAllShowScreenConfigOrConditionQuery(map, deviceNum, screenNum, screenPosition, bindArea, desc, floorName,mapids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShowScreenConfig(ShowScreenConfig showScreenConfig) {
        showScreenConfigMapper.addShowScreenConfig(showScreenConfig);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShowScreenConfigArea(List<ShowScreenConfigArea> area) {
        showScreenConfigMapper.addShowScreenConfigArea(area);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShowScreenConfigArea12(ShowScreenConfigArea area) {
        showScreenConfigMapper.addShowScreenConfigArea12(area);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShowScreenConfigAreaPlace(List<ShowScreenConfigAreaPlace> areaPlace) {
        showScreenConfigMapper.addShowScreenConfigAreaPlace(areaPlace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addShowScreenConfigAreaPlace12(ShowScreenConfigAreaPlace areaPlace) {
        showScreenConfigMapper.addShowScreenConfigAreaPlace12(areaPlace);
    }

    @Override
    public List<ShowScreenConfig> getShowScreenConfigById(String sid) {
        return showScreenConfigMapper.getShowScreenConfigById(sid);
    }

    @Override
    public void delShowScreenConfigByAreaId(String areaId) {
        showScreenConfigMapper.delShowScreenConfigByAreaId(areaId);
    }

    @Override
    public void updateShowScreenConfig(ShowScreenConfig showScreenConfig) {
        showScreenConfigMapper.updateShowScreenConfig(showScreenConfig);
    }

    @Override
    public void updateShowScreenConfigArea(ShowScreenConfigArea area) {
        showScreenConfigMapper.updateShowScreenConfigArea(area);
    }

    @Override
    public void updateShowScreenConfigAreaPlace(ShowScreenConfigAreaPlace areaPlace) {
        showScreenConfigMapper.updateShowScreenConfigAreaPlace(areaPlace);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delShowScreenConfig(String[] split) {
        for (String id : split) {
            showScreenConfigMapper.delShowScreenConfigByAreaId(id);
            showScreenConfigMapper.delShowScreenConfigArea(id);
            showScreenConfigMapper.delShowScreenConfig(id);
        }
    }

    @Override
    public ShowScreenConfig getGuideScreenIdByGuideScreenIsRepeated(String add,String update) {
        return showScreenConfigMapper.selectById1(add,update);
    }

    @Override
    public List<ParkingCompanyVo> getAllShowScreenByMapId(String[] mapids) {
        return showScreenConfigMapper.getAllShowScreenByMapId(mapids);
    }
}
