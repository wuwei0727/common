package com.tgy.rtls.data.mapper.view;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.entity.view.UserVo;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.entity.view.ViewVo2;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ViewMapper extends BaseMapper<ViewVo> {

    //获取所有用户信息，性别
    List<ViewVo> getAllUserInfo(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    //按月获取所有用户总数
    List<ViewVo2> getAllUserTotalNumByMonth0(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo> getAllUserTotalNumByMonth();
    List<ViewVo2> getAllUserTotalNumByMonth2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    //获取累计使用频率
    List<ViewVo> getCumulativeUseFrequency(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo> getCumulativeUseFrequency2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    /**
     * 车位以及硬件统计信息
     * 1获取所有车位总数，空闲车 位数，充电车位数
     * 2获取所有车位检测器总数，在线数量，离线数量
     * 3获取所有信标总数，在线数量，离线数量
     * 4获取所有网关总数，在线数量，离线数量
     *
     * @return
     */
    //
    List<ViewVo> getCarBitAndHardwareInfo();
    List<ViewVo> getCarBitAndHardwareInfo2(@Param("time") String time, @Param("mapId") String mapId);

    //获取所有入驻商家数和入驻公司数
    List<ViewVo> getSettledMerchantAndFirmInfo();

    //获取单个停车场的车位状态
    List<ViewVo> getSingleMapParkingStateInfo();

    //实时进出数据
    List<ViewVo> getRealTimeInAndOutData(String mapId);
    List<ViewVo> getRealTimeInAndOutData1();

    List<ViewVo> getFindCarFrequency();
    List<ViewVo> getUseCarFrequency();
    List<ViewVo> getRecommendCarFrequency();

//    List<ViewVo> getFrequency();


//***************************************************************
    ViewVo getSingleAddOrUpdateCarBitUseRecord(@Param("mapId") Integer map,@Param("placeId") Integer placeId);
//***************************************************************


    /**
     * 获取月活跃用户信息
     *
     * @param month 1/2/3/4/5/6/7/....
     * @return
     */
    List<ViewVo> getMonthActiveUser(@Param("month") String month);

    //获取所有用户区域
    List<ViewVo> getAllUserArea();

    //获取所有车位总数，空闲车位数，充电车位数
    List<ViewVo> getParkingCountAndStateInfo();

    //获取所有信标总数，在线数量，离线数量
    List<ViewVo> getSubCountAndStateInfo();

    //获取所有网关总数，在线数量，离线数量
    List<ViewVo> getGatewayCountAndStateInfo();
    //***************************************************************
    // 新增用户数
    List<ViewVo2> getWithinThreeMonthsNewUsers(String time);
    // 用户检索前10停车场
    List<ViewVo2> getTop10ParkingPlaces();

    // 用户检索前10商家
    List<ViewVo2> getTop10Business(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getTop10Business2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 车位使用次数总数
    List<ViewVo2> getPlaceUseTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getPlaceUseTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 车位空闲总时长
    List<ViewVo2> getPlaceIdleTotalDuration(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getPlaceIdleTotalDuration2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 车位预约总数
    List<ViewVo2> getReservationTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getReservationTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 平台车位利用率
    List<ViewVo2> getPlatformPlaceUtilizationRate(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo2> getPlatformPlaceUtilizationRate2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 停车场车位利用率

    // 停车场车位利用率
    List<ViewVo2> getMapPlaceUtilizationRate(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getMapPlaceUtilizationRate2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 停车场车位空闲率
    List<ViewVo2> getPlaceAvailabilityRate(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getPlaceAvailabilityRate2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 车位导航总数
    List<ViewVo2> getPlaceNavigationTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getPlaceNavigationTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);
    // 车位导航使用率
    List<ViewVo2> getPlaceNavigationUseRate(@Param("time")String time,@Param("start") String start, @Param("end") String end);

    List<ViewVo2> getPlaceNavigationUseRate2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);
    // 每小时空车位数
    List<ViewVo2> getPerHourNullPlaceNumber();
    List<ViewVo2> getPerHourNullPlaceNumber2(@Param("mapId") String mapId);

    // 位置分享总数
    List<ViewVo2> getLocationShareTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo2> getLocationShareTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 用户搜索总数
    List<ViewVo2> getUserSearchTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo2> getUserSearchTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    // 反向寻车总数
    List<ViewVo2> getReverseCarSearchTotal(@Param("time") String time, @Param("start") String start, @Param("end") String end);
    List<ViewVo2> getReverseCarSearchTotal2(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);
    // 空闲车位数
    List<ViewVo2> getIdlePlaceNumber(String time);
    // 活跃用户数
    List<ViewVo2> getActiveUserTotal(String time);
    // 统计近一年活跃用户数
    List<ViewVo2> getActiveUserTotal2(@Param("time") String time, @Param("mapId") String mapId);
    // 单地图用户总数
    List<ViewVo2> getMapUsersTotal(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getWithinThreeMonthsNewUsers2(@Param("time") String time, @Param("mapId") String mapId);


    List<ViewVo2> getActiveUserNumber(@Param("time") String time, @Param("mapId") String mapId, @Param("start") String start, @Param("end") String end);

    List<ViewVo2> getManyMapActiveUserNumber(@Param("time") String time, @Param("start") String start, @Param("end") String end);


    @Select("<script>" +
            "SELECT place,start,end FROM place_userecord " +
            "<where>" +
            "<if test='mapId != null'> " +
            "AND map=#{mapId} " +
            "</if>" +
            "</where>" +
            "</script>")
    List<PlaceUseRecord> getPlaceUseRecords(@Param("mapId") String mapId);
    @Select("<script>" +
            "SELECT COUNT(*) FROM parking_place WHERE id IN (SELECT place FROM infrared) " +
            "<if test='mapId != null'> " +
            "AND map=#{mapId} " +
            "</if>" +
            "</script>")
    Integer getPlaceTotal(@Param("mapId") String mapId);

    @Select("<script>" +
            "SELECT userid userIds,loginTime loginTimes FROM map_monthactiveuserrecord where 1=1 " +
            "<if test='mapId != null'>and map=#{mapId}</if>" +
            "<if test='time != null'>\n" +
            "and loginTime >= DATE_SUB(NOW(),INTERVAL #{time} MONTH) AND loginTime &lt;= now() \n " +
            "</if>" +
            "</script>")
    List<UserVo> getUserTotal(@Param("mapId") String mapId, @Param("time")Integer time);


    @Select("select count(*) from map_monthactiveuserrecord where userid=#{userId} and map =#{map}")
    Integer getUserByIdSize(@Param("userId") Long userId, @Param("map") String map);

}
