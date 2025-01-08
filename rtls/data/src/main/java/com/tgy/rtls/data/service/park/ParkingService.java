package com.tgy.rtls.data.service.park;

import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.excel.ExcelDataVo;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

public interface ParkingService {

    /*
     * 查找所有公司
     * */
    List<ParkingCompany> findByAllCompany(@Param("id") Integer id, @Param("name") String name, @Param("map") Integer map,
                                          @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize);

    List<ParkingCompany> findByAllCompany2(@Param("id") Integer id, @Param("name") String name, @Param("map") Integer map,
                                           @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, String floorName, String[] maps);

    /*
     * 查找所有公司
     * */
    Map_2d findByCompanyMap(Integer companyId);

    /*
     * 查找所有停车位置
     * */
    List<ParkingPlace> findByAllPlace(@Param("id") Integer id, @Param("name") String name, @Param("companyName") String companyName, @Param("map") Integer mapid,
                                      @Param("license") String license, @Param("state") Short state, @Param("company") Integer companyid,
                                      @Param("floor") String floor,
                                      @Param("charge") Short charge,
                                      @Param("type") Short type,
                                      @Param("instanceid") String instanceid, @Param("fid") String fid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize,@Param("phone") String phone);

    List<ParkingPlace> findByAllPlace2(Integer id, String name, String companyName, Integer mapid,
                                       String license, String carbitType , Short state, Integer companyid,
                                       String floor, Short charge, Short type, String instanceid, String fid, String configWay,
                                       Integer pageIndex, Integer pageSize, String desc, String floorName, String isReservable, String[] maps);

    List<ParkingPlace> findByAllCompanyName(Integer id, String name, String companyName, Integer mapid, String license, Short state, Integer companyid, String floor, Short charge, Short type, String instanceid, String fid, Integer pageIndex, Integer pageSize);

    List<ParkingPlace> wechatFindByAllCompanyName(String companyName,Integer mapid, String company, String instanceid, String floor);

    Integer addCompany(@Param("parkingCompany") ParkingCompany parkingCompany);

    void updateCompany(@Param("parkingCompany") ParkingCompany parkingCompany);

    void deleteCompany(@Param("ids") String[] ids);

    Integer addPlace(@Param("parkingPlace") ParkingPlace parkingPlace);

    void updatePlace(@Param("parkingPlace") ParkingPlace parkingPlace);

    void updatePlaceCompany(@Param("parkingPlace") ParkingPlace parkingPlace);

    void deletePlace(@Param("ids") String[] ids);

    void updatePlaces(@Param("ids") String[] placeids, @Param("newCompanyid") Integer newCompanyid, @Param("oldCompanyid") Integer oldCompanyid);

    List findFloorByMapid(@Param("map") Integer map);

    /*
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
                                    @Param("instanceid") String instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, String[] maps);

    /*
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
                                      @Param("instanceid") Integer instanceid, @Param("pageIndex") Integer pageIndex, @Param("pageSize") Integer pageSize, String floorName, @Param("maps") String[] maps,String fid);

    /*
     * 查找违停信息
     * */
    List<ShangJiaType> findByAllShangjiaType(@Param("id") Integer id,
                                             @Param("instanceid") Integer instanceid);

    /*
     * 查找地图车位以及名称
     * */
    List<ParkingPlace> findPlaceByMapAndName(@Param("map") Integer map,
                                             @Param("name") String name,
                                             @Param("fid") String fid
    );

    Integer addShangjia(@Param("shangJia") ShangJia shangJia);

    void updateShangjia(@Param("ShangJia") ShangJia shangJia);

    void delShangjia(@Param("ids") String[] ids);

    CompletableFuture<List<MapPlace>> findPlaceCountGroupByMap(@Param("enable") Integer enable, @Param("type") Integer place_type, @Param("mapId") Integer mapId, Integer hasVIP);

    CompletableFuture<List<CompanyPlace>> findPlaceCountByMap(@Param("map") Integer map, @Param("enable") Integer enable, @Param("companyName") String name, String status, @Param("type")Integer place_type);

    String importLabelFromExcel(MultipartFile excelFile, String fmapID) throws Exception;

    int importExitFromExcel(MultipartFile excelFile, String fmapID) throws Exception;

    ParkingCompany findCompanyByPhone(@Param("phone") String phone);

    int addLicensePos(@Param("licensePos") LicensePos licensePos);

    void updateLicensePos(@Param("licensePos") LicensePos licensePos);

    LicensePos findLicensePosByLicenseAndMap(@Param("map") Integer map, @Param("license") String license, @Param("userid") Integer userid);

    List<Object> getPlaceUseRecord(Integer map, int day, String content);

    List<Object> getPlaceChargeRecord(Integer map, int day, String content);

    List<Object> getPlaceMapFeeAndFlow(Integer map, int day, String content);

    /* public List<Object> getPlaceUseRecordRange(String start, String end,Integer map,String content);*/

    boolean delViolate(String id);

    List<ParkingCompany> getComByName(String companyName);

    List<ParkingCompany> getComByNameId(String companyName, Integer companyId,Integer map);

    List<Infrared> getPlaceByName(Integer map, String PlaceName);

    List<ShangJia> findShangjiaPhone(String phone, Integer id);

    List<ShangJia> findShangjiaMapName(Integer id,String name, Integer mapId);

    List<ShangJia> findShangjiaMapName1(String name, Integer mapId, Integer id);

    List<ShangJia> getShangjiaMap(Integer id);

    ShangJia getShangJiaById(String id);

    List<ParkingPlace> getPlaceByComId(Integer comId);

    void updatePlaceByComId(ParkingPlace parkingPlace);

    ParkingCompany getComById(String id);

    List<Object> getExclusiveAndFreePlaces(Integer map,Integer companyId,Integer preferenceCarBit,Integer isVip,String placeName);
    List<Object> getOrdinaryPlaces(Integer map, Integer companyId, Integer preferenceCarBit, String[] placeId, Integer placeType);

    ParkingPlace getPlaceByPlaceId(Integer placeId,String desc,String name,Integer map);
    List<ParkingPlace> getPlaceListByPlaceId(Integer placeId,String desc,String name,Integer map);

    List<ParkingPlace> getCurrentPlacesBindCompany(Integer mapId,String placeName,String companyId);

    ParkingPlace getPlaceByPlaceNames(Integer mapId,String placeName,String fid,String companyId);

    List<ParkingPlace> getPlaceByPlaceNameList(Integer mapId, List<String> nameList);

    CompletableFuture<Object> addLicensePos(Double lng, Double lat, String key, Double minDis, Integer place_type, Integer mapId);

    List<Infrared> getInfraredByPlaceId(String num,Integer placeId);

    Integer batchUpdateUsers(List<PlaceVo> users);

    <T,U,R> int batchUpdateOrInsert(List<T> data, Class<U> mapperClass, BiFunction<T, U, R> function);
    void processPlaceRecord(ParkingPlace place, Infrared infrared,String timeStr, LocalDateTime dateTime, List<ParkingPlace> places) throws ParseException;
    void addOrUpdatePlaceRecord(PlaceVo v);

    List<ExcelDataVo> getParkingPlaceList(Integer mapId);

    List<ParkingPlace> findByIds(List<Long> ids);

    void updatePlaceTests(String map,LocalDateTime now);
}
