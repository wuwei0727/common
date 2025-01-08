package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.BarrierGate;
import com.tgy.rtls.data.mapper.vip.BarrierGateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BarrierGateServiceImpl extends ServiceImpl<BarrierGateMapper, BarrierGate> implements BarrierGateService {
    @Autowired
    private BarrierGateMapper barrierGateMapper;

    @Override
    public List<BarrierGate> getBarrierGateInfo(Long map, String vipArea, String barrierGateNum, Integer state, String desc, String floorName, String[] mapids) {
        return barrierGateMapper.getBarrierGateInfo(map, vipArea, barrierGateNum, state, desc, floorName,mapids);
    }

    @Override
    public void addBarrierGateInfo(BarrierGate barrierGate) {
        barrierGateMapper.addBarrierGateInfo(barrierGate);

    }

    @Override
    public void editBarrierGateInfo(BarrierGate barrierGate) {
        barrierGateMapper.editBarrierGateInfo(barrierGate);
    }

    @Override
    public BarrierGate getBarrierGateInfoInfoById(Integer id) {
        return barrierGateMapper.getBarrierGateInfoInfoById(id);
    }

    @Override
    public BarrierGate getBarrierGateInfoInfoByNum(String num) {
        return barrierGateMapper.getBarrierGateInfoInfoByNum(num);
    }

    @Override
    public void delBarrierGateInfo(String[] split) {
        for (String id : split) {
            barrierGateMapper.delBarrierGateInfo(id);
        }
    }

    @Override
    public List<ParkingCompanyVo> getMapIdByAllAreaName(String[] mapIds) {
        return barrierGateMapper.getAllAreaNameByMapId(mapIds);
    }

    @Override
    public List<BarrierGate> getConditionData(String deviceNum, String bingingArea, String update) {
        return barrierGateMapper.getConditionData(deviceNum, bingingArea, update);

    }
}
