package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip
 * @date 2020/12/23
 */
public interface GatewayService {
    /*
     * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
     * */
    List<Gateway_uwb> findByAll(Integer instanceid,Integer map, Integer connect,String name,Integer relevance);

    /*
     * 查询网关详情
     * */
    Gateway_uwb findById(Integer id);

    /*
     * 网关ip重名判断
     * */
    Gateway_uwb findByIp(String ip);

    /*
     * 新增网关
     * */
    boolean addGateway(Gateway_uwb gateway);

    /*
     * 修改网关
     * */
    boolean updateGateway(Gateway_uwb gateway);

    /*
     * 修改网关连接状态
     * */
    void updateGatewayConnect(Integer id,Integer connect);

    /*
     * 删除网关
     * */
    boolean delGateway(String ids);

    /*
     * 删除gateway
     * */
    int delGatewayByInstance(@Param("instanceid")Integer instanceid);

    List<Gateway_lora> findGatewayByNum(String num,@Param("map")String map);


    /*
     * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
     * */
    List<Gateway_lora> findByAllGatewayLora(Integer instanceid,Integer map,
                                            Integer networkstate,String num,Integer relevance,String name,String desc,String mapName);

    boolean addWxGateway(Gateway_lora gatewayLora);

    void updateWxGateway(Gateway_lora gatewayLora);

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
    void updateGatewayState(Integer id, Integer state);
}
