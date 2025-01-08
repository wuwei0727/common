package com.tgy.rtls.data.mapper.vip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.BarrierGate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BarrierGateMapper extends BaseMapper<BarrierGate> {
    List<BarrierGate> getBarrierGateInfo(@Param("map") Long map, @Param("vipArea") String vipArea, @Param("barrierGateNum") String barrierGateNum, @Param("state") Integer state, @Param("desc") String desc,@Param("floorName") String floorName, @Param("mapids") String[] mapids);

    void addBarrierGateInfo(BarrierGate barrierGate);

    void editBarrierGateInfo(BarrierGate barrierGate);

    void delBarrierGateInfo(String id);

    BarrierGate getBarrierGateInfoInfoById(Integer id);

    BarrierGate getBarrierGateInfoInfoByNum(String num);
    List<ParkingCompanyVo> getAllAreaNameByMapId(@Param("mapIds") String[] mapIds);

    List<BarrierGate> getConditionData(@Param("deviceNum") String deviceNum, @Param("bingingArea") String bingingArea, @Param("update") String update);
}