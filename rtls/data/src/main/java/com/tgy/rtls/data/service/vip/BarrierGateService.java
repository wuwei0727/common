package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.BarrierGate;

import java.util.List;
public interface BarrierGateService extends IService<BarrierGate>{


    List<BarrierGate> getBarrierGateInfo(Long map, String vipArea, String barrierGateNum, Integer state, String desc, String floorName, String[] mapids);

    void addBarrierGateInfo(BarrierGate barrierGate);

    void editBarrierGateInfo(BarrierGate barrierGate);

    BarrierGate getBarrierGateInfoInfoById(Integer id);
    BarrierGate getBarrierGateInfoInfoByNum(String num);

    void delBarrierGateInfo(String[] split);

    List<ParkingCompanyVo> getMapIdByAllAreaName(String[] mapIds);

    List<BarrierGate> getConditionData(String deviceNum, String bingingArea,String update);

}
