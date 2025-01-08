package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.vip.VipParking;
import com.tgy.rtls.data.mapper.vip.VipParkingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VipParkingServiceImpl extends ServiceImpl<VipParkingMapper, VipParking> implements VipParkingService {

    @Autowired
    private VipParkingMapper vipParkingMapper;

    @Override
    public List<VipParking> getVipParkingSpaceInfo(String parkingName, String license, Long map, String phone, Short state, Short type, String desc, String floorName, Integer status, String[] mapids) {
        return vipParkingMapper.getVipParkingSpaceInfo(parkingName, license, map, phone, state, type, desc, floorName,status,mapids);
    }

    @Override
    public List<ParkingPlace> getInfoByMapAndName(Long map, String name,Integer state,Integer type) {
        return vipParkingMapper.getInfoByMapAndName(map, name,state,type);
    }

    @Override
    public void addVipParingSpaceInfo(VipParking vipParking) {
        vipParkingMapper.addVipParingSpaceInfo(vipParking);
    }

    @Override
    public void delVipParingSpaceInfo(String[] split) {
        for (String id : split) {
            vipParkingMapper.delVipParingSpaceInfo(id);
        }
    }

    @Override
    public void editVipParingSpaceInfo(VipParking vipParking) {
        vipParkingMapper.editVipParingSpaceInfo(vipParking);
    }

    @Override
    public VipParking getVipParingSpaceInfoById(Integer id) {
        return vipParkingMapper.getVipParingSpaceInfoById(id);
    }
    @Override
    public VipParking getVipParingSpaceInfoBySomeTimePart(Long id, String name, String license, Long map, String startTime, String endTime) {
        return vipParkingMapper.getVipParingSpaceInfoBySomeTimePart(id,name,license,map,startTime,endTime);
    }
    @Override
    public List<ParkingCompanyVo> getAllPlaceNameByMapId(String[] mapIds,String type){
        return vipParkingMapper.getAllPlaceNameByMapId(mapIds,type);
    }

    @Override
    public List<VipParking> getVipParkingPlaceTimeoutParking() {
        return vipParkingMapper.getVipParkingPlaceTimeoutParking();
    }

    @Override
    public List<VipParking> getVipParkingOccupyReminderUser() {
        return vipParkingMapper.getVipParkingOccupyReminderUser();
    }

    @Override
    public List<VipParking> selectBookPlaceByLicenseAndTime(Integer map, String license, String startTime, String endTime) {
        return vipParkingMapper.selectBookPlaceByLicenseAndTime(map,license, startTime, endTime);
    }

    @Override
    public VipParking getByIds(Integer id) {
        return vipParkingMapper.getByIds(id);
    }
}

