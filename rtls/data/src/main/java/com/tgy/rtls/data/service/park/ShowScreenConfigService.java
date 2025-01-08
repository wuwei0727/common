package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.park.ShowScreenConfigAreaPlace;
import com.tgy.rtls.data.entity.park.ShowScreenConfigArea;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-12 10:02
 * @Description: TODO
 * @Version: 1.0
 */
public interface ShowScreenConfigService extends IService<ShowScreenConfig> {

    List<ShowScreenConfig> getAllShowScreenConfigOrConditionQuery(String map, String deviceNum, String screenNum, String screenPosition, String bindArea, String desc, String floorName, String[] mapids);

    void addShowScreenConfig(ShowScreenConfig showScreenConfig);

    void addShowScreenConfigArea(List<ShowScreenConfigArea> area);
    void addShowScreenConfigArea12(ShowScreenConfigArea area);

    void addShowScreenConfigAreaPlace(List<ShowScreenConfigAreaPlace> areaPlace);
    void addShowScreenConfigAreaPlace12(ShowScreenConfigAreaPlace areaPlace);

    List<ShowScreenConfig> getShowScreenConfigById(String sid);

    void delShowScreenConfigByAreaId(String areaId);

    void updateShowScreenConfig(ShowScreenConfig showScreenConfig);

    void updateShowScreenConfigArea(ShowScreenConfigArea area);

    void updateShowScreenConfigAreaPlace(ShowScreenConfigAreaPlace areaPlace);

    void delShowScreenConfig(String[] split);
    ShowScreenConfig getGuideScreenIdByGuideScreenIsRepeated(String add,String update);

    List<ParkingCompanyVo> getAllShowScreenByMapId(String[] mapids);
}
