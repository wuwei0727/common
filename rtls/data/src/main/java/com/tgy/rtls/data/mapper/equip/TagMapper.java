package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import com.tgy.rtls.data.entity.park.BeaconCount;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2020/10/16
 */
@Mapper
public interface TagMapper {
    /*
     * 实例下定位卡信息查询 num-->卡号 instanceid-->实例id
     * */
    List<Tag> findByAll(@Param("num") String num, @Param("binding") Integer binding, @Param("desc") String desc, @Param("instanceid") Integer instanceid, String name);

    /*
     * 查询地图上在线的标签
     * */
    List<Tag> findByTagOnLine(@Param("map") Integer map);

    /*
     * 根据定位卡id获取定位卡编号
     * */
    String findByNameId(@Param("ids") String[] ids);

    /*
     * 实例下定位卡详情查询 id-->定位卡id
     * */
    Tag findById(@Param("id") Integer id, String name);

    /*
     * 实例下定位卡卡号重名判断 num-->卡号
     * */
    Tag findByNum(@Param("num") String num);

    //车位检测器重名判断
    List<Infrared> findInfraredByNum(@Param("num") String num);

    /*
     * 实例下新增定位卡
     * */
    int addTag(@Param("tag") Tag tag);

    /*
     * 实例下修改定位卡
     * */
    int updateTag(@Param("tag") Tag tag);

    /*
     * 删除定位卡 ids-->定位卡id集
     * */
    int delTag(@Param("ids") String[] ids);

    /*
     * 删除实例下的定位卡 instanceid-->实例id
     * */
    int delTagInstance(@Param("instanceid") Integer instanceid);

    /*
     * 标签在线离线状态转换 num-->定位卡标签  status-->状态 0离线 1在线
     * */
    int updateTagStatus(@Param("num") String num, @Param("status") Integer status);

    /*
     * 标签电压信息修改
     * */
    int updateTagBatteryTime(@Param("num") String num, @Param("batteryVolt") Double batteryVolt, @Param("batteryTime") String batteryTime);

    /*
     * 标签频率修改
     * */
    int updateLocaInval(@Param("tagid") String tagid, @Param("loca_inval") Integer loca_inval);

    /*
     * 通过识别码和标签编号找到对应的标签信息 code1-->识别码1 code2-->识别码2 num-->标签编号
     * */
    Tag findByCodeNum(@Param("code1") String code1, @Param("code2") String code2, @Param("num") String num, String name);

    /**
     * 存储电压曲线
     *
     * @param tagVolt
     */
    void addSub1GVolt(@Param("tag") TagVolt tagVolt);

    void addBleRssi(@Param("tag") TagBeacon tagBeacon);

    /*
     * 查询定位卡的校正参数
     * */
    TagPara findByTagid(@Param("tagid") String tagid);

    /*
     * 查询定位卡的校正参数
     * */
    TagPara findByTagMac(@Param("mac") String mac);

    /*
     * 查询nb设备
     * */
    List findByNbMac(@Param("mac") String mac, @Param("license") String license, @Param("map") Integer map);

    List findByNbMacName(@Param("mac") String mac, @Param("license") String license, @Param("map") Integer map);

    Nb_device findByNbId(@Param("id") Integer id);

    void addNb(@Param("nb_device") Nb_device nb_device);

    void updateNb(@Param("nb_device") Nb_device nb_device);

    /*
     * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
     * */
    List<Infrared> findByAllInfrared(@Param("num") String num, @Param("networkstate") Integer networkstate,
                                     @Param("relevance") Integer relevance, @Param("map") Integer map, @Param("desc") String desc, @Param("name") String name, @Param("mapName") String mapName);

    List<Infrared> findByAllInfrared2(@Param("num") String num, @Param("networkstate") Integer networkstate, @Param("power") Integer power,
                                      @Param("relevance") Integer relevance, @Param("map") Integer map, @Param("desc") String desc, @Param("name") String name, @Param("mapName") String mapName, @Param("placeName") String placeName, @Param("status") Integer status, @Param("infraredName") String infraredName, @Param("floorName") String floorName, @Param("maps") String[] maps);

    List<Infrared> findIredByIdAndName(@Param("id") Integer id, @Param("map") Integer map, @Param("num") String num);
    List<InfraredMessage> findIredByIdAndName1(@Param("num") Integer num);

    void addInfrared(@Param("infrared") Infrared infrared);

    void updateInfrared(@Param("infrared") Infrared infrared);
    void addInfrared1(@Param("infrared") InfraredMessage infrared);
    void updateInfrared1(@Param("infrared") InfraredMessage infrared);

    List<ParkingPlace> getPlace(String num);

    int delInfrared(@Param("ids") String[] ids);

    int delInfraredApp(@Param("id") Integer ids);

    BeaconCount getInfraredAcount(Integer map);

    /**
     * 查询车位检测器下的车位
     *
     * @param ids 车位检测器
     * @return
     */
    String[] getInfraredPlace(@Param("ids") String[] ids);

    Integer updatePlace(@Param("ids") String[] ids, @Param("placeState") Integer placeState);

    List<Infrared> findInfraredId(@Param("placeId") Integer placeId, @Param("map") Integer map, @Param("num") String num);

    List<Infrared> getAllInfraredLowPower();

    void updateInfraredStateBecomesLowPower(@Param("id") Integer id, @Param("state") Integer state, @Param("placeState") Integer placeState);

    void deleteInfraredMapIsNull();

    List<InfraredVo> getDetectorByMap(String map);

    String getMapName(String map);

    List<DeviceVo> infraredBatteryTimeWarningLevelsQuery();

    List<Infrared> queryInfraredOfflineUpdatePlaceStateTask();

    void addMag(@Param("mag") Mag mag);

    void addMagdiff(@Param("magdiff") MagDiff magdiff);

    int updateLifetimeByMap(@Param("map") Integer map, @Param("lifetimeMonths") Integer lifetimeMonths);
}

