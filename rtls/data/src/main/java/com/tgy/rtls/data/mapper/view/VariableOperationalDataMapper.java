package com.tgy.rtls.data.mapper.view;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.entity.userinfo.WechatUserInfo;
import com.tgy.rtls.data.entity.view.UserVo;
import com.tgy.rtls.data.entity.view.VariableOperationalData;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.entity.vo.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/3/25 - 12:03
 */
public interface VariableOperationalDataMapper extends BaseMapper<VariableOperationalData> {
    @Select("select id from wechat_userinfo")
    List<WechatUserInfo> getAllUsers();
    @Select("select id from map_2d")
    List<Map_2d> getAllMaps();

    @Select("select id,map,place,start,end from place_userecord")
    List<PlaceUseRecord> getAllPlaceUseRecord();

    @Select("select count from user_search_log where map=#{map}")
    Integer getUserSearchTotal(Integer map);
    @Select("SELECT count FROM location_sharing_log where map=#{map}")
    Integer getLocationShareTotal(Integer map);
    void insertUsersTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers,@Param("map") Integer map,@Param("visitCount") Integer visitCount);

    void insertUserSearchTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("result") Integer result, @Param("userId") String userId);

    void insertTop10Business(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("businessId") Integer businessId, @Param("s") String s);

    void insertLocationShareTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("result") Integer result, @Param("s") String s);

    void insertPlaceUseTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("hourDifference") Integer hourDifference,@Param("map") Integer map, @Param("placeId") Integer placeId);

    void insertPlaceNavigationTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("placeId") Integer placeId, @Param("placeName") String placeName, @Param("s") String s
    );

    void insertPlaceNavigationUseRate(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("placeId") Integer placeId, @Param("placeName") String placeName, @Param("s") String s);

    void insertReservationTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("placeId") Integer placeId, @Param("placeName") String placeName, @Param("license") String license, @Param("reservationPerson") String reservationPerson, @Param("phone") String phone, @Param("s") String s);

    void insertReverseCarSearchTotal(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("placeId") Integer placeId, @Param("placeName") String placeName, @Param("userId") Integer userId, @Param("s") String s);
    void insertUserVisitCount(@Param("year") Integer year, @Param("month") Integer month, @Param("numDays") Integer numDays, @Param("numbers") Integer numbers, @Param("map") Integer map, @Param("userId") Integer userId);


    List<ViewVo2> getVariableOperationalData(@Param("type") Integer type, @Param("map") Integer map);

    // ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————

    List<UserStatisticsVo> getUserStatistics(@Param("map") Integer map, @Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<UserStatisticsVo> getActiveUserCount(@Param("map") Integer map, @Param("time") String time, @Param("start") String start, @Param("end") String end);

    Integer getTotalUserCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);

    List<UserVo> getNewUserCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);

    Integer getTotalVisitCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);


    Integer getTotalActiveUserCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    // ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    Integer getTotalUserSearchCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    Integer getTotalShareCount(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
// ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    List<DevicesVo> getDevices(@Param("map") Integer map, @Param("time") String time);
    List<DevicesVo> getDevicesTotal(@Param("map") Integer map, @Param("time") String time);
// ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————

    List<ParkingReservationVo> getParkingReservation(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);

    List<ParkingReservationVo> getParkingReservationTotal(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    // ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    List<ReverseCarSearchVo> getReverseCarSearchData(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    List<ReverseCarSearchVo> getReverseCarSearchTotal(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
// ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    List<ParkingDataVo> getParkingData(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    List<ParkingDataVo> getIdleParkingNumbers(@Param("map") Integer map);
    List<ParkingDataVo> getParkingTotal(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    // ———————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————————
    List<ParkingUsageVo> getParkingUsageData(@Param("map") Integer map, @Param("time") String time, @Param("monthSecond") Integer monthSecond,@Param("start") String start, @Param("end") String end);
    List<ParkingUsageVo> getParkingNavigationTotal(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);
    List<ParkingUsageVo> getParkingUseTotal(@Param("map") Integer map, @Param("time") String time,@Param("start") String start, @Param("end") String end);

    List<ParkingUsageVo> getParkingUsageTotal(@Param("map") Integer map, @Param("time") String time, @Param("day") Integer day, @Param("hour")  Integer hour,@Param("start") String start, @Param("end") String end);

    void deleteBatchByIds(@Param("ids") List<Long> ids);
}