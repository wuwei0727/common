package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.VipArea;

import java.util.List;

public interface VipAreaService extends IService<VipArea>{

    List<VipArea> getVipAreaInfo(String license, Long map, String phone, String barrierGateNum, String desc, String floorName, String[] mapids);
    String getBarrierGateInfo();

    void addVipAreaInfo(VipArea vipArea);

    void editVipAreaInfo(VipArea vipArea);

    void delVipAreaInfo(String[] ids);

    VipArea getVipAreaInfoInfoById(Integer id);
    VipArea gateLicense(String license,String num);

    List<VipArea> gateCmd(String num, Long map);

    VipArea gateCmdWhetherOvertime(String barrierGateId, Long map);

    List<ParkingCompanyVo> getMapIdByAllAreaName(String[] mapids);

    VipArea getByIds(Integer areaId);
}
