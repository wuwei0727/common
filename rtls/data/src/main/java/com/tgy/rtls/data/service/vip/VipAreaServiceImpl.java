package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.VipArea;
import com.tgy.rtls.data.mapper.vip.VipAreaMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VipAreaServiceImpl extends ServiceImpl<VipAreaMapper, VipArea> implements VipAreaService {
    @Autowired
    private VipAreaMapper vipAreaMapper;
    @Override
    public List<VipArea> getVipAreaInfo(String license, Long map, String phone, String barrierGateNum, String desc, String floorName, String[] mapids) {
        return vipAreaMapper.getVipAreaInfo(license, map, phone, barrierGateNum,desc,floorName,mapids);
    }

    @Override
    public String getBarrierGateInfo() {
        return vipAreaMapper.getBarrierGateInfo();
    }

    @Override
    public void addVipAreaInfo(VipArea vipArea) {
        vipAreaMapper.addVipAreaInfo(vipArea);
    }

    @Override
    public void editVipAreaInfo(VipArea vipArea) {
        vipAreaMapper.editVipAreaInfo(vipArea);
    }

    @Override
    public void delVipAreaInfo(String[] ids) {
        for (String id : ids) {
            vipAreaMapper.delVipAreaInfo(id);
        }
    }

    @Override
    public VipArea getVipAreaInfoInfoById(Integer id) {
        return vipAreaMapper.getVipAreaInfoInfoById(id);
    }

    @Override
    public VipArea gateLicense(String license, String barrierGateNum) {
        return vipAreaMapper.gateLicense(license,barrierGateNum);
    }
    @Override
    public List<VipArea> gateCmd(String barrierGateNum, Long map) {
        return vipAreaMapper.gateCmd(barrierGateNum,map);
    }

    @Override
    public VipArea gateCmdWhetherOvertime(String barrierGateId, Long map) {
        return vipAreaMapper.gateCmdWhetherOvertime(barrierGateId,map);
    }

    @Override
    public List<ParkingCompanyVo> getMapIdByAllAreaName(String[] mapids) {
        return vipAreaMapper.getMapIdByAllAreaName(mapids);
    }

    @Override
    public VipArea getByIds(Integer areaId) {
        return vipAreaMapper.getByIds(areaId);
    }

}
