package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.mapper.equip.GatewayMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.GatewayService;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2020/12/23
 */
@Service
@Transactional
public class GatewayServiceImpl implements GatewayService {
    @Autowired(required = false)
    private GatewayMapper gatewayMapper;
    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired(required = false)
    private RedisService redisService;

    @Override
    public List<Gateway_uwb> findByAll(Integer instanceid, Integer map, Integer connect, String name, Integer relevance) {
        return gatewayMapper.findByAll(instanceid, map, connect, name, relevance);
    }

    @Override
    @Cacheable(value = "gatewayId", key = "#id")
    public Gateway_uwb findById(Integer id) {
        return gatewayMapper.findById(id);
    }

    @Override
    public Gateway_uwb findByIp(String ip) {
        return gatewayMapper.findByIp(ip);
    }

    @Override
    public boolean addGateway(Gateway_uwb gateway) {
        return gatewayMapper.addGateway(gateway) > 0;
    }

    @Override
    @CacheEvict(value = "gatewayId", key = "#gateway.id")
    public boolean updateGateway(Gateway_uwb gateway) {
        return gatewayMapper.updateGateway(gateway) > 0;
    }

    @Override
    @CacheEvict(value = "gatewayId", key = "#id")
    public void updateGatewayConnect(Integer id, Integer connect) {
        gatewayMapper.updateGatewayConnect(id, connect);
    }

    @Override
    public boolean delGateway(String ids) {
        String[] split = ids.split(",");
        for (String id : split) {
            JSONObject object = new JSONObject();
            object.put("id", id);//网关id
            object.put("type", 0);//1连接 0断开连接
            redisService.remove("gatewayId::" + id);
            kafkaTemplate.send(KafukaTopics.CONNECT_GATEWAY, null, object.toString());
        }
        return gatewayMapper.delGateway(split) > 0;
    }

    @Override
    public int delGatewayByInstance(Integer instanceid) {
        return gatewayMapper.delGatewayByInstance(instanceid);
    }

    @Override
    public List<Gateway_lora> findGatewayByNum(String num,String map) {
        return gatewayMapper.findGatewayByNum(num,map);
    }

    @Override
    public List<Gateway_lora> findByAllGatewayLora(Integer instanceid, Integer map, Integer networkstate, String num, Integer relevance, String name, String desc, String mapName) {
        return gatewayMapper.findByAllGatewayLora(instanceid, map, networkstate, num, relevance, name, desc, mapName);
    }

    @Override
    public boolean addWxGateway(Gateway_lora gatewayLora) {
        return gatewayMapper.addWxGateway(gatewayLora) > 0;
    }

    @Override
    public void updateWxGateway(Gateway_lora gatewayLora) {
        gatewayMapper.updateWxGateway(gatewayLora);
    }

    @Override
    public List<Gateway_lora> findAllGateway(Integer id, Integer map, String num) {
        return gatewayMapper.findAllGateway(id, map, num);
    }

    @Override
    //@DS("slave")
    public void addInfraredOrigin(InfraredOrigin infraredOrigin) {
        gatewayMapper.addInfraredOrigin(infraredOrigin);

    }

    @Override
    //@DS("slave")
    public List<InfraredOrigin> findInfraredOrigin(String start, String end, Integer gatewaynum, Integer infrarednum, Integer state, Integer count) {
        return gatewayMapper.findInfraredOrigin(start,end,gatewaynum,infrarednum,state,count);
    }

    @Override
    //@DS("slave")
    public List<Integer> findInfraredCount(String start, String end, Integer infrarednum) {
        return gatewayMapper.findInfraredCount(start,end,infrarednum);
    }

    @Override
    //@DS("slave")
    public List<Integer> findAllInfrared(String start, String end,Integer gate,Integer infraredd) {
        return gatewayMapper.findAllInfrared(start,end,gate,infraredd);
    }

    @Override
    //@DS("slave")
    public void addGateWayState(GateWayState gateWayState) {
        gatewayMapper.addGateWayState(gateWayState);
    }

    @Override
    //@DS("slave")
    public void addInfraredState(InfraredState infraredState) {
         gatewayMapper.addInfraredState(infraredState);
    }
    @Override
    //@DS("slave")
    public List<GateWayState> findGateWayState(@Param("start")String start, @Param("end")String end,
                                        @Param("gatewaynum")String gatewaynum,
                                        @Param("state")Integer state){
      return  gatewayMapper.findGateWayState(start,end,gatewaynum,state);
    }

    @Override
    //@DS("slave")
    public  List<InfraredState> findInfraredState(@Param("start")String start,@Param("end")String end,
                                          @Param("infrarednum")String infrarednum,
                                          @Param("state")String state){
        return  gatewayMapper.findInfraredState(start,end,infrarednum,state);
    }

    @Override
    public List<DeviceVo> gatewayBatteryTimeWarningLevelsQuery() {
        return gatewayMapper.gatewayBatteryTimeWarningLevelsQuery();
    }

    @Override
    public void updateGatewayState(Integer id, Integer state) {
        gatewayMapper.updateGatewayState(id,state);
    }
}
