package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import com.tgy.rtls.data.entity.park.ShowScreenConfigAreaPlace;
import com.tgy.rtls.data.entity.park.ShowScreenConfigArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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
public interface ShowScreenConfigMapper extends BaseMapper<ShowScreenConfig> {
    List<ShowScreenConfig> getAllShowScreenConfigOrConditionQuery(@Param("map") String map, @Param("deviceNum") String deviceNum, @Param("screenNum") String screenNum, @Param("screenPosition") String screenPosition, @Param("bindArea") String bindArea, @Param("desc") String desc,@Param("floorName") String floorName, @Param("mapids") String[] mapids);

    void addShowScreenConfig(ShowScreenConfig showScreenConfig);

    void addShowScreenConfigArea(@Param("area") List<ShowScreenConfigArea> area);
    void addShowScreenConfigArea12(ShowScreenConfigArea area);

    void addShowScreenConfigAreaPlace(@Param("areaPlace") List<ShowScreenConfigAreaPlace> areaPlace);
    void addShowScreenConfigAreaPlace12(ShowScreenConfigAreaPlace areaPlace);

    List<ShowScreenConfig> getShowScreenConfigById(String sid);

    void delShowScreenConfigByAreaId(@Param("areaId")String areaId);

    void updateShowScreenConfig(ShowScreenConfig showScreenConfig);
    void updateShowScreenConfigArea(ShowScreenConfigArea area);
    void updateShowScreenConfigAreaPlace(ShowScreenConfigAreaPlace areaPlace);

    void delShowScreenConfigArea(String id);
    void delShowScreenConfig(String id);

    @Select("<script>"+
            "select id," +
            "guide_screen_id," +
            "devicenum," +
            "screennum," +
            "`map` " +
            "from showscreenconfig " +
            "where 1=1 " +
            "<if test='add!=null'>" +
            "and guide_screen_id = #{add}" +
            "</if> " +
            "<if test='update!=null'>" +
            "and guide_screen_id != #{update}" +
            "</if>"+
            "</script>"
    )
    ShowScreenConfig selectById1(@Param("add") String add, @Param("update") String update);


    List<ParkingCompanyVo> getAllShowScreenByMapId(@Param("mapIds") String[] mapIds);
}