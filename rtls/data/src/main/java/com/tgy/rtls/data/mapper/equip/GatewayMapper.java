package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.excel.GatewayVo;
import com.tgy.rtls.data.entity.park.BeaconCount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2020/12/23
 */
@Mapper
public interface GatewayMapper {
    /*
    * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
    * */
    List<Gateway_uwb> findByAll(@Param("instanceid")Integer instanceid,@Param("map")Integer map,
                                @Param("connect")Integer connect,@Param("name")String name,@Param("relevance") Integer relevance);

    /*
     * 查询网关详情
     * */
    Gateway_uwb findById(@Param("id")Integer id);

    /*
    * 网关ip重名判断
    * */
    Gateway_uwb findByIp(@Param("ip")String ip);

    /*
    * 新增网关
    * */
    int addGateway(@Param("gateway")Gateway_uwb gateway);

    /*
     * 修改网关
     * */
    int updateGateway(@Param("gateway")Gateway_uwb gateway);

    /*
    * 修改网关连接状态
    * */
    int updateGatewayConnect(@Param("id")Integer id,@Param("connect")Integer connect);

    /*
     * 删除网关
     * */
    int delGateway(@Param("ids")String[] ids);

    /*
     * 删除gateway
     * */
    int delGatewayByInstance(@Param("instanceid")Integer instanceid);





    /*
     * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
     * */
    List<Gateway_lora> findByAllGatewayLora(@Param("instanceid")Integer instanceid,@Param("map")Integer map,
                                @Param("networkstate")Integer networkstate,@Param("num")String num,@Param("relevance") Integer relevance,@Param("name") String name,@Param("desc")String desc,@Param("mapName")String mapName);

    List<Gateway_lora> findByAllGatewayLora2(@Param("instanceid")Integer instanceid,@Param("map")Integer map,
                                @Param("networkstate")Integer networkstate,@Param("num")String num,@Param("relevance") Integer relevance,@Param("name") String name,@Param("desc")String desc,@Param("mapName")String mapName,@Param("floorName") String floorName,@Param("maps")String[] maps);


    /*
     * 查询网关详情
     * */
    List<Gateway_lora> findGateway_loraByIdAndName(@Param("id")Integer id,@Param("map")Integer map,@Param("num")String num);


    /*
     * 查询网关详情
     * */
    List<Gateway_lora> findGateway_loraByNum(@Param("num")String num);



    /*
     * 新增443Mhz网关
     * */
    int addGatewayLora(@Param("gateway") Gateway_lora gateway);

    /*
     * 修改网关
     * */
    int updateGateway_Lora(@Param("gateway")Gateway_lora gateway);

    int updateGatewayState(@Param("state") Integer state, @Param("id") Integer id);


    /*
     * 删除网关
     * */
    int delGateway_lora(@Param("ids")String[] ids);


    /*
     * 获取网关状态统计数据
     * */
    BeaconCount getGateway_loraAcount(Integer map);


    List<Gateway_lora> findGatewayByNum(@Param("num")String num,@Param("map")String map);

    /*
     * 新增网关
     * */
    int addWxGateway(@Param("gateway")Gateway_lora gateway);

    int updateWxGateway(@Param("gateway")Gateway_lora gateway);

    List<Gateway_lora> findAllGateway(@Param("id") Integer id, @Param("map") Integer map, @Param("num") String num);
    void addInfraredOrigin(@Param("infraredOrigin") InfraredOrigin infraredOrigin);

    List<InfraredOrigin> findInfraredOrigin(@Param("start")String start,@Param("end")String end,
                                            @Param("gatewaynum")Integer gatewaynum,@Param("infrarednum")Integer infrarednum,
                                            @Param("state")Integer state,@Param("count")Integer count);


    List<Integer> findInfraredCount(@Param("start")String start, @Param("end")String end,
                                                @Param("infrarednum")Integer infrarednum);




    List<Integer> findAllInfrared(@Param("start")String start, @Param("end")String end,@Param("gate")Integer gate,@Param("infraredd")Integer infraredd);

    void addGateWayState(@Param("gateWayState") GateWayState gateWayState);

   void addInfraredState(@Param("infraredState") InfraredState infraredState);


    List<GateWayState> findGateWayState(@Param("start")String start,@Param("end")String end,
                                            @Param("gatewaynum")String gatewaynum,
                                            @Param("state")Integer state);


    List<InfraredState> findInfraredState(@Param("start")String start,@Param("end")String end,
                                          @Param("infrarednum")String infrarednum,
                                            @Param("state")String state);

    List<DeviceVo> gatewayBatteryTimeWarningLevelsQuery();

    @Select("select\n" +
            "    g.num,g.ip,g.floor,g.batteryTime,g.addTime,g.offline_time as `offlineTime`," +
            "    (select name from map_2d m where m.id = g.map) as mapName," +
            "    (select name from map_2d m where m.id = g.map) as mapName," +
            "    (select name from map_relevance_floor mrf where mrf.map=g.map and mrf.level=g.floor)floorName," +
            "    CASE WHEN networkstate = 0 THEN '离线' WHEN networkstate = 1 THEN '在线' ELSE '未知状态'" +
            "    END AS networkName\n" +
            "from gateway_lora g\n" +
            "where g.map =#{mapId} and g.map is not null")
    List<GatewayVo> getGatewayInfo(String mapId);
}
