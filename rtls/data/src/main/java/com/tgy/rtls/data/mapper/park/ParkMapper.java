package com.tgy.rtls.data.mapper.park;

import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.excel.ExcelDataVo;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.*;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.message
 * @date 2020/10/26
 * 车位查询
 */
public interface ParkMapper{

    /**
     * 查找所有公司
     * */
    List<ParkingCompany> findByAllCompany(@Param("id") Integer id, @Param("name") String name, @Param("map") Integer map,
                                          @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    List<ParkingCompany> findByAllCompany2(@Param("id") Integer id, @Param("name") String name, @Param("map") Integer map,
                                           @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize,@Param("floorName") String floorName, @Param("maps") String[] maps);

    /**
     * 查找所有公司
     * */
    Map_2d findByCompanyMap(@Param("companyId") Integer companyId);

    /**
     * 查找所有停车位置
     */
    List<ParkingPlace> findByAllPlace(@Param("id") Integer id, @Param("name") String name,
                                      @Param("companyName") String companyName,
                                      @Param("map") Integer mapid, @Param("license") String license, @Param("state") Short state, @Param("company") Integer company, @Param("floor") String floor,
                                      @Param("charge") Short charge,
                                      @Param("type") Short type,
                                      @Param("instanceid") String instanceid, @Param("fid") String fid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, @Param("phone") String phone);

    /**
     * 查找所有停车位置
     */
    List<ParkingPlace> findByAllPlace2(@Param("id") Integer id, @Param("name") String name,
                                       @Param("companyName") String companyName,
                                       @Param("map") Integer mapid, @Param("license") String license, @Param("carbitType") String carbitType, @Param("state") Short state, @Param("company") Integer company, @Param("floor") String floor,
                                       @Param("charge") Short charge,
                                       @Param("type") Short type,
                                       @Param("reserve") String reserve, @Param("fid") String fid, @Param("configWay") String configWay, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, @Param("desc") String desc, @Param("floorName") String floorName, @Param("isReservable") String isReservable, @Param("maps") String[] maps);

    /**
     * 根据关键字搜索车位
     * */
    List<ParkingPlace> findByAllCompanyName(@Param("id") Integer id, @Param("name") String name,
                                            @Param("companyName") String companyName,
                                            @Param("map") Integer mapid, @Param("license") String license, @Param("state") Short state, @Param("company") Integer company, @Param("floor") String floor,
                                            @Param("charge") Short charge,
                                            @Param("type") Short type,
                                            @Param("instanceid") String instanceid, @Param("fid") String fid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    /**
     * 根据关键字搜索车位
     * */
    List<ParkingPlace> wechatFindByAllCompanyName(
            @Param("companyName") String companyName,
            @Param("map") Integer mapid,
            @Param("instanceid") String instanceid,
            @Param("company") String company,
            @Param("floor") String floor
    );

    Integer addCompany(@Param("parkingCompany") ParkingCompany parkingCompany);

    void updateCompany(@Param("parkingCompany") ParkingCompany parkingCompany);

    void deleteCompany(@Param("ids") String[] ids);

    Integer addPlace(@Param("parkingPlace") ParkingPlace parkingPlace);

    void updatePlace(@Param("parkingPlace") ParkingPlace parkingPlace);
    void updatePlace2(@Param("parkingPlace") ParkingPlace parkingPlace);
    void updateInfraredPlace(@Param("placeVo") PlaceVo placeVo);
    void updatePlaceTest(@Param("parkingPlace") ParkingPlace parkingPlace);

    void deletePlace(@Param("ids") String[] ids);

    void updatePlaces(@Param("ids") String[] placeids, @Param("newCompanyid") Integer newCompanyid, @Param("oldCompanyid") Integer oldCompanyid);

    List findFloorByMapid(@Param("map") Integer map);

    /**
     * 查找违停信息
     * */
    List<WeiTing> findByAllWeiTing(@Param("id") Integer id,
                                   @Param("license") String license,
                                   @Param("start") String start, @Param("end") String end,
                                   @Param("map") Integer map,
                                   @Param("state") Integer state,
                                   @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    List<WeiTing> findByAllWeiTing2(@Param("id") Integer id,
                                    @Param("license") String license,
                                    @Param("start") String start, @Param("end") String end,
                                    @Param("map") Integer map,
                                    @Param("state") Integer state,
                                    @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, @Param("maps") String[] maps);

    /**
     * 查找违停信息
     * */
    List<ShangJia> findByAllShangjia(@Param("id") Integer id,
                                     @Param("map") Integer map,
                                     @Param("type") Integer type,
                                     @Param("name") String name,
                                     @Param("instanceid") Integer instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    List<ShangJia> findByAllShangjia2(@Param("id") Integer id,
                                      @Param("map") Integer map,
                                      @Param("type") Integer type,
                                      @Param("name") String name,
                                      @Param("instanceid") Integer instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize,@Param("floorName") String floorName, @Param("maps") String[] maps, @Param("fid") String fid);

    /**
     * 查找违停信息
     * */
    List<ShangJia> findShangjiaByName(@Param("map") Integer map, @Param("name") String name);

    /**
     * 查找违停信息
     * */
    List<ShangJiaType> findByAllShangjiaType(@Param("id") Integer id,
                                             @Param("instanceid") Integer instanceid);

    Integer addShangjia(@Param("shangJia") ShangJia shangJia);

    void updateShangjia(@Param("shangJia") ShangJia shangJia);

    void delShangjia(@Param("ids") String[] ids);

    List<MapPlace> findPlaceCountGroupByMap(@Param("enable") Integer enable, @Param("placeType") Integer placeType, @Param("mapId") Integer mapId, @Param("hasVIP") Integer hasVIP);

    List<CompanyPlace> findPlaceCountByCompany(@Param("map") Integer map, @Param("enable") Integer enable, @Param("companyName") String name, @Param("status") String status,@Param("type") Integer place_type);
    ParkingPlace findPlace(@Param("id") Integer id, @Param("placeName") String name,@Param("map") String map,@Param("fid") String fid);

    List<CompanyPlace> findPlaceCountByCompanyContainPlaceList(@Param("map") Integer map, @Param("enable") Integer enable, @Param("companyName") String name);

    List<ParkingExit> findExit(@Param("fid") String fid, @Param("name") String name, @Param("map") Integer map, @Param("type") Integer type);

    List<ParkingExit> getExit(@Param("mapId") Integer map, @Param("name") String name, @Param("fid") String fid);

    List<CrossLevelCorridor> getCrossFloorByMap(@Param("map") Integer map);

    int addExit(@Param("parkingExit") ParkingExit parkingExit);

    void updateExit(@Param("parkingExit") ParkingExit parkingExit);

    int addCrossFloor(@Param("parkingCrossFloor") ParkingCrossFloor parkingCrossFloor);

    void updateCrossFloor(@Param("parkingCrossFloor") ParkingCrossFloor parkingCrossFloor);

    CrossLevelCorridor findCrossFloor(@Param("fid") String fid, @Param("map") Integer map, @Param("name") String name);

    List<ParkingPlace> findPlaceByMapAndName(@Param("map") Integer map,
                                             @Param("name") String name,
                                             @Param("fid") String fid
    );

    ParkingCompany findCompanyByPhone(@Param("phone") String phone);

    int addLicensePos(@Param("licensePos") LicensePos licensePos);

    void updateLicensePos(@Param("licensePos") LicensePos licensePos);

    LicensePos findLicensePosByLicenseAndMap(@Param("map") Integer map, @Param("license") String license, @Param("userid") Integer userid);

    boolean updatePlaceCompany(@Param("parkingPlace") ParkingPlace parkingPlace);

    List<LicensePos> findLicensePosRecent(@Param("map") Integer map, @Param("start") Date Start, @Param("end") Date end, @Param("floor") String floor, @Param("license") String license);

    void addFeeRecord(@Param("fee") Fee fee);
    void addFeeRecordTest(@Param("fee") Fee fee);

    void updateFeeRecord(@Param("fee") Fee fee);

    List<Fee> findAllFee();

    List<SimulateTrail> getSimulateTrail(@Param("map") Integer map);

    List<RealTrail> getRealTrail(@Param("map") Integer map);

    List<ParkingCompany> getComByName(String companyName);

    List<ParkingCompany> getComByNameId(@Param("companyName") String companyName, @Param("companyId") Integer companyId,@Param("map") Integer map);

    List<Infrared> getPlaceByName(@Param("map") Integer map, @Param("placeName") String placeName);

    List<ShangJia> findShangjiaPhone(@Param("phone") String phone, @Param("id") Integer id);

    List<ShangJia> findShangjiaMapName(@Param("id") Integer id,@Param("name") String name, @Param("mapId") Integer mapId);

    List<ShangJia> findShangjiaMapName1(@Param("name") String name, @Param("mapId") Integer mapId, @Param("id") Integer id);

    List<ParkingCompany> getAllCom(@Param("map") String map);

    List<ParkingCompany> getComByxyf(@Param("x") String x, @Param("y") String y,@Param("map") String map,  @Param("floor") String floor, @Param("fid") String fid,@Param("id")Integer id);

    List<ShangJia> getShangjiaByxyf(@Param("x") String x, @Param("y") String y, @Param("map") String map, @Param("floor") String floor, @Param("fid") String fid, @Param("id")Integer id);

    List<ParkingCompanyVo> getAllMap(@Param("mapIds") String[] mapIds);

    Integer updatePlaceById(@Param("placeId") Integer placeId, @Param("state") Short state, @Param("license") String license, @Param("map") String map, @Param("name") String name,
                            @Param("detectionException") Integer detectionException, @Param("exceptionTime") LocalDateTime exceptionTime);
    Integer updatePlaceByTest(@Param("placeId")Integer placeId,@Param("state") Short state,@Param("license")String license);

    Map_2d getCurrentInfrared(@Param("num") Integer num,@Param("map") Integer map);

    Map_2d getCurrentInfraredMapName(@Param("placeId") Integer placeId,@Param("map") Integer map);

    ParkingPlace getPlaceByFid(@Param("fid")String fid);

    List<ShangJia> getShangjiaMap(@Param("id")Integer id);
    List<ParkingPlace> getPlaceById(@Param("placeId")Integer placeId);

    List<Integer> getPlaceById2(@Param("map") long map,@Param("companyId") long companyId);

    ParkingPlace getPlaceByPlaceId(@Param("placeId") Integer placeId, @Param("desc") String desc, @Param("name") String name,@Param("map") Integer map);

    List<ParkingPlace> getPlaceByComId(@Param("comId") Integer comId);

    void updatePlaceByComId(ParkingPlace parkingPlace);

    ParkingCompany getComById(@Param("id") String id);

    ShangJia getShangJiaById(@Param("id")String id);

    List<ParkingPlace> getPlaceByMapXY(@Param("map") Integer map, @Param("floor") String floor, @Param("x") String x, @Param("y") String y, @Param("companyId") String companyId);

    List<Object> getExclusiveAndFreePlaces(@Param("map") Integer map,@Param("companyId") Integer companyId,@Param("preferenceCarBit") Integer preferenceCarBit,@Param("isVip") Integer isVip,@Param("placeName") String placeName);
    List<Object> getOrdinaryPlaces(@Param("map") Integer map,@Param("companyId") Integer companyId,@Param("preferenceCarBit") Integer preferenceCarBit,@Param("placeId") String[] placeId,@Param("placeType") Integer placeType);


    List<ParkingPlace> getCurrentPlacesBindCompany(@Param("mapId")Integer mapId,@Param("placeName") String placeName,@Param("companyId") String companyId);
    void deleteById(String s);

    ParkingPlace getPlaceByPlaceNames(@Param("mapId")Integer mapId,@Param("placeName") String placeName,@Param("fid") String fid,@Param("companyId") String companyId);

    List<ParkingPlace> getPlaceByPlaceNameList(@Param("mapId") Integer mapId, @Param("nameList") List<String> nameList);

    List<ExcelDataVo> selectParkingPlaceList(@Param("mapId") Integer mapId);
   Integer selectEmptyCountByPlaces(@Param("list")List<Integer> list);

    Integer updateBatchById(@Param("placeVoList")List<PlaceVo> placeVoList);

    List<Infrared> getInfraredByPlaceId(@Param("num") String num, @Param("placeId") Integer placeId);

    List<Infrared> getInfraredByTime(@Param("minute") Integer minute, @Param("place") Integer place);

    void updateShangJiaById(ShangJia shangJia);

    @Select("<script>" +
            "select count(company) from parking_place where 1=1 " +
            "<if test='map!=null'>and map=#{map}</if>" +
            "<if test='company!=null'>and company=#{company}</if> " +
            "</script>")
    long getPlaceCountByCompany(@Param("map") Long map,@Param("company") Long company);

    List<ParkingPlace> getPlaceById3(@Param("placeId") Integer placeId, @Param("map") String map);

    @Select("SELECT id,`name`,`map`,(SELECT GROUP_CONCAT(NAME SEPARATOR ',') FROM parking_place WHERE parking_place.company=parking_company.id) AS places FROM parking_company WHERE map=#{mapId} AND id=#{companyId}")
    ParkingCompany getByMapIdAndCompanyId(@Param("mapId") Long mapId, @Param("companyId") Long companyId);

    @Select("select id,name,license from parking_place where map=178")
    List<PlaceVo> getPlace(int i);

    void updateBatchById1(PlaceVo user);
    void updateBatchById2(PlaceVo user);

    List<ParkingPlace> getPlaceListByPlaceId(@Param("placeId") Integer placeId, @Param("desc") String desc, @Param("name") String name, @Param("map") Integer map);
    ParkingPlace getPlaceDataById(@Param("map") String map,@Param("id") Integer id);


    @Select("<script>" +
            "SELECT * FROM parking_place WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    List<ParkingPlace> findByIds(@Param("ids") List<Long> ids);

    void updatePlaceTests(@Param("map") String map, @Param("now") LocalDateTime now);
}
