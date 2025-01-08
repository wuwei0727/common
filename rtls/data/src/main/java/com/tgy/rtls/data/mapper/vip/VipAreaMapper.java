package com.tgy.rtls.data.mapper.vip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.VipArea;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface VipAreaMapper extends BaseMapper<VipArea> {

    List<VipArea> getVipAreaInfo(@Param("license") String license, @Param("map") Long map, @Param("phone") String phone, @Param("barrierGateNum") String barrierGateNum, @Param("desc") String desc,@Param("floorName") String floorName, @Param("mapids") String[] mapids);

    void addVipAreaInfo(VipArea vipArea);
    void editVipAreaInfo(VipArea vipArea);

    void delVipAreaInfo(String id);
    VipArea getVipAreaInfoInfoById(Integer id);

    VipArea gateLicense(@Param("license") String license, @Param("barrierGateNum") String barrierGateNum);

    List<VipArea> gateCmd(@Param("barrierGateNum") String barrierGateNum, @Param("map") Long map);

    String getBarrierGateInfo();

    VipArea gateCmdWhetherOvertime(@Param("barrierGateId") String barrierGateId, @Param("map") Long map);

    List<ParkingCompanyVo> getMapIdByAllAreaName(@Param("mapIds") String[] mapIds);

    @Select("select v.`map` mapId,v.start_time,v.end_time, bg.id as barrierGateId,bg.id as barrierGateId,bg.binding_area as barrierGateArea,bg.floor as floor,bg.fid as fid,bg.floor,bg.x,bg.y " +
            "from vip_area v " +
            "left join map_2d m2d on m2d.id = v.map\n" +
            "    left join barrier_gate bg on v.barrier_gate_num = bg.id " +
            "where v.id=#{areaId}")
    VipArea getByIds(Integer areaId);
}