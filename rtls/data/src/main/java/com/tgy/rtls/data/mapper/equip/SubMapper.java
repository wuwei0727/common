package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.equip.DeviceVo;
import com.tgy.rtls.data.entity.park.BeaconCount;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2020/10/19
 * 分站配置接口
 */
public interface SubMapper {
    /*
    * 实例下分站列表信息 num-->卡号 networkstate-->网络状态 powerstate-->供电状态 relevance-->是否关联地图 map-->地图id error-->故障信息 instanceid-->实例id
    * */
    List<BsSyn> findByAll(@Param("num")String num,@Param("type")Integer type,@Param("networkstate")Integer networkstate,@Param("powerstate")Integer powerstate,
                          @Param("relevance")Integer relevance,@Param("map")Integer map,@Param("error")Integer error,@Param("desc")String desc,@Param("start")String start,@Param("end")String end,@Param("instanceid")Integer instanceid,@Param("name")String name);

 List<BsSyn> findByAll2(@Param("num") String num, @Param("type") Integer type, @Param("typeName") String typeName, @Param("networkstate") Integer networkstate, @Param("powerstate") Integer powerstate, @Param("power") Integer power,
                        @Param("relevance") Integer relevance, @Param("map") Integer map, @Param("error") Integer error, @Param("desc") String desc, @Param("start") String start, @Param("end") String end, @Param("instanceid") Integer instanceid, @Param("name") String name, @Param("floorName") String floorName, @Param("maps") String[] maps);
   List<DeviceVo> getSubAll();

    /*
    * 根据分站id获取分站名
    * */
    String findByNameId(@Param("ids")String[] ids);

    /*
    * 分站信息详情
    * */
    Substation findById(@Param("id")Integer id,@Param("name")String name);

    Substation findByBsid(@Param("id")Integer id,@Param("name")String name);
    /*
    * 分站编号 重名判断
    * */
    Substation findByNum(@Param("num")String num,@Param("name")String name);

    SubSyn findByMaxnum(@Param("num")String num,@Param("name")String name);

    /*
    * 新增分站信息
    * */
    int addSub(@Param("sub")Substation sub);

    /*
    * 新增分站信息 只添加编号
    * */
    int addSubNum(@Param("sub")Substation sub);

    /*
    * 修改分站信息
    * */
    int updateSub(@Param("sub")Substation sub);

    /*
    * 删除分站信息
    * */
    int delSub(@Param("ids")String[] ids);

    /*
    * 删除实例下的所有分站
    * */
    int delSubInstance(@Param("instanceid")Integer instanceid);

    /*
    * 修改基站网络状态 0离线 1在线
    * */
    int updateSubNetworkstate(@Param("num")String num,@Param("state")Integer state);

    /*
     * 修改基站供电状态 0主电供电 1备用电源供电
     * */
    int updateSubPowerstate(@Param("num")String num,@Param("state")Integer state);

 /*
  * 修改基站电压相关
  * */
 int updateSubBattery(@Param("num") String num, @Param("batteryVolt") String batteryVolt, @Param("batteryTime") String batteryTime, @Param("power") Short power);

    /*
     * 更新基站故障
     * */
    int updateSubError(@Param("num")String num,Integer error);

    /*
    * 修改基站ip地址
    * */
    //int updateSubIp(@Param("num")String num,@Param("ip")String ip);

    /*
    * 查询地图上分站数 map-->地图ID
    * */
    int findBySubCount(@Param("map")Integer map);

    /*
    * 修改基站天线延时和校正系数
    * */
    //int updateAntennadelay(@Param("id")Integer id,@Param("antennadelay")String antennadelay,@Param("disfix")String disfix);



    List<BsSyn> findBeaconByMap(@Param("map")Integer map);


    BeaconCount findCalcuuByMap(@Param("map")Integer map);


    void addBeaconVolt(@Param("num")String num,@Param("volt")float volt);

   Substation getCurrentSubMapName(@Param("num")String num,@Param("map")Integer map);

 List<DeviceVo> getSubMoreThan30Days(@Param("map")String map);

 List<DeviceVo> getSubMoreThan60Days(@Param("map")String map);
 List<DeviceVo> substationBatteryTimeWarningLevelsQuery();

 void updateSubForOffline(@Param("id") Integer id);

 void updateSubLessThanBatterySub(@Param("id")Integer id);

 List<Substation> timeNotUpdateBetweenFetche(@Param("map")String map);

 int getNum(@Param("num") String num);

 List<Substation> getBsconfig(@Param("num") String num, @Param("mapId")String mapId);

 void addSub1(Substation sub);

 void addBsconfig(@Param("bs") BsConfig bsConfig1);

 void delSub1(@Param("id") Integer id);

 void delBsconfig(@Param("id") Integer id);

 void updateSub2(Substation substation);

 int updateLifetimeByMap(@Param("map") Integer map, @Param("lifetimeMonths") Integer lifetimeMonths, @Param("type") Integer type);

 void updateAddTime(@Param("map")Integer map, @Param("num") String num, @Param("now") LocalDateTime now);
}
