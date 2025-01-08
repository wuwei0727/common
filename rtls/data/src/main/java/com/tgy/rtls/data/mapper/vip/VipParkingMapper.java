package com.tgy.rtls.data.mapper.vip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.vip.VipParking;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VipParkingMapper extends BaseMapper<VipParking> {
    List<VipParking> getVipParkingSpaceInfo(@Param("parkingName") String parkingName, @Param("license") String license, @Param("map") Long map, @Param("phone") String phone, @Param("state") Short state, @Param("type") Short type, @Param("desc") String desc, @Param("floorName") String floorName, @Param("status") Integer status, @Param("mapids") String[] mapids);

    List<ParkingPlace> getInfoByMapAndName(@Param("map") Long map, @Param("name") String name,@Param("state") Integer state,@Param("type") Integer type);

    void addVipParingSpaceInfo(VipParking vipParking);

    void delVipParingSpaceInfo(@Param("id") String id);

    void editVipParingSpaceInfo(VipParking vipParking);

    VipParking getVipParingSpaceInfoById(Integer id);

    VipParking getVipParingSpaceInfoBySomeTimePart(@Param("id") Long id, @Param("name") String name, @Param("license") String license, @Param("map") Long map, @Param("startTime") String startTime, @Param("endTime") String endTime);

    List<ParkingCompanyVo> getAllPlaceNameByMapId(@Param("mapIds") String[] mapIds, @Param("type") String type);

    @Select("select v.id," +
            "v.name, " +
            "v.map," +
            "m2d.name as mapName," +
            "pp.name as parkingName ," +
            "v.license, " +
            "v.reservation_person as reservationPerson, " +
            "v.phone, " +
            "v.start_time as startTime, v.end_time as endTime " +
            "from vip_parking v " +
            "left join map_2d m2d on v.map = m2d.id " +
            "left join parking_place pp on v.place = pp.id " +
            "where TIMESTAMPDIFF(MINUTE, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i'), end_time) = 10 and pp.state =1")
    List<VipParking> getVipParkingPlaceTimeoutParking();
    @Select(
            "select v.id," +
                    "v.name, " +
                    "v.map," +
                    "m2d.name as mapName," +
                    "pp.name as parkingName ," +
                    "v.license, " +
                    "v.reservation_person as reservationPerson, " +
                    "v.phone, " +
                    "v.start_time as startTime, " +
                    "v.end_time as endTime " +
                    "from vip_parking v " +
                    "left join map_2d m2d on v.map = m2d.id " +
                    "left join parking_place pp on v.place = pp.id " +
                    "where TIMESTAMPDIFF(MINUTE, DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i'), start_time) = 10 and pp.state =1"
    )
    List<VipParking> getVipParkingOccupyReminderUser();

    @Select("select p.`map` mapId,p.`name`,p.x,p.y,p.`floor`,p.fid,v.id vipPlaceId,v.`start_time` as startTime,v.`end_time`as endTime,f.device_num as deviceId \n" +
            "from parking_place p " +
            "left join vip_parking v on p.id = v.place\n" +
            "left join floor_lock f on f.place = p.id \n" +
            "where v.id=#{id}")
    VipParking getByIds(Integer id);

    //    @Select({"SELECT * FROM vip_parking WHERE license = #{license} AND ((start_time <= #{start} AND end_time >= #{start})  OR (start_time <= #{end} AND end_time >= #{end}) OR (start_time >= #{start} AND end_time <= #{end}))"})
    @Select("SELECT * FROM vip_parking WHERE map=#{map} and license = #{license} AND (start_time < #{end} AND end_time > #{start})")
    List<VipParking> selectBookPlaceByLicenseAndTime(@Param("map") Integer map, @Param("license") String license, @Param("start") String start, @Param("end") String end);
}