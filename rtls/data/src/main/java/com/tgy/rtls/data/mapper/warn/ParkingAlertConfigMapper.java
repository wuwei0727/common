package com.tgy.rtls.data.mapper.warn;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.entity.warn.ParkingAlertConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.warn
 * @Author: wuwei
 * @CreateTime: 2024-10-21 15:54
 * @Description: TODO
 * @Version: 1.0
 */
public interface ParkingAlertConfigMapper extends BaseMapper<ParkingAlertConfig> {
    List<ParkingAlertConfig> getAllOrFilteredParkingAlertConfig(@Param("configName") String configName, @Param("status") String status, @Param("map") String map, @Param("desc") String desc, @Param("mapids") String[] mapids);

    ParkingAlertConfig getParkingAlertConfigById(int id);

    @Select("SELECT map, COUNT(place) as total " +
            "FROM place_userecord " +
            "WHERE start > DATE_SUB(NOW(), INTERVAL #{t2Hours} HOUR) " +  // t2时间段内
            "GROUP BY map " +
            "HAVING COUNT(place) >= #{threshold}")  // 超过n个车位
    List<ViewVo2> getMapChangesInT2Period(@Param("t2Hours") Integer t2Hours, @Param("threshold") Integer threshold);

    /**
     * 查询在t2时间段内变化的具体车位信息
     */
    @Select("SELECT r.place as placeName,r.map " +
            "FROM place_userecord r " +
            "WHERE r.start > DATE_SUB(NOW(), INTERVAL #{t2Hours} ${timeUnit}) " +
            "AND r.map = #{mapId} " +
            "GROUP BY r.map,r.place " +
            "HAVING COUNT(*) > #{threshold}")
    List<ViewVo2> getChangedPlacesInT2Period(@Param("mapId") Integer mapId,@Param("t2Hours") Integer t2Hours,@Param("threshold") Integer threshold,@Param("timeUnit") String timeUnit);


//    @Select("SELECT r.place as placeName " +
//            "FROM place_userecord r " +
//            "INNER JOIN parking_place p ON r.place = p.id " +
//            "WHERE r.start > DATE_SUB(NOW(), INTERVAL #{t2Hours} HOUR) " +
//            "GROUP BY r.place " +
//            "HAVING COUNT(place) >= #{threshold}")  // 超过n个车位)
//    List<ViewVo2> getChangedPlacesInT3Period(@Param("t2Hours") Integer t2Hours,@Param("threshold") Integer threshold);

    @Select("SELECT r.place as placeName,r.map, COUNT(*) as count " +
            "FROM place_userecord r " +
            "WHERE r.map = #{mapId}  AND DATE(r.start) = DATE(NOW())" +
            "AND r.start > DATE_SUB(NOW(), INTERVAL #{t3Hours} ${timeUnit})" +
            "GROUP BY r.place " +
            "HAVING COUNT(*) > #{threshold}")
    List<ViewVo2> getChangedPlacesInT3Period(@Param("mapId") Integer mapId,@Param("t3Hours") Integer t3Hours,@Param("threshold") Integer threshold, @Param("timeUnit") String timeUnit);

}