package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.vip.VipParking;

import java.util.List;

public interface VipParkingService extends IService<VipParking> {
    /**
     * 获取vip车位信息
     *
     * @param parkingName 车位名称
     * @param license     车牌号
     * @param map         地图
     * @param phone       手机号
     * @param state       状态
     * @param type        类型
     * @param desc        排序
     * @param status
     * @param mapids      拥有的地图
     * @return
     */
    List<VipParking> getVipParkingSpaceInfo(String parkingName, String license, Long map, String phone, Short state, Short type, String desc, String floorName, Integer status, String[] mapids);
    List<ParkingPlace> getInfoByMapAndName(Long map, String name,Integer state,Integer type);
    void addVipParingSpaceInfo(VipParking vipParking);
    void delVipParingSpaceInfo(String[] split);
    void editVipParingSpaceInfo(VipParking vipParking);
    VipParking getVipParingSpaceInfoById(Integer id);
    VipParking getVipParingSpaceInfoBySomeTimePart(Long id, String name, String license, Long map, String startTime, String endTime);
    List<ParkingCompanyVo> getAllPlaceNameByMapId(String[] mapIds,String type);

    List<VipParking> getVipParkingPlaceTimeoutParking();
    List<VipParking> getVipParkingOccupyReminderUser();

    VipParking getByIds(Integer id);

    List<VipParking> selectBookPlaceByLicenseAndTime(Integer map, String license, String startTime, String endTime);

}

