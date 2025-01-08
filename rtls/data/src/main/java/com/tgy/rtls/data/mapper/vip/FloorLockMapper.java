package com.tgy.rtls.data.mapper.vip;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.excel.FloorLockVo;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.vip.FloorLock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FloorLockMapper extends BaseMapper<FloorLock> {
    List<FloorLock> getFloorLockInfo(@Param("map") Long map, @Param("deviceNum") String deviceNum, @Param("parkingName") String parkingName, @Param("placeState") Integer placeState, @Param("desc") String desc, @Param("floorName") String floorName, @Param("networkstate") Integer networkstate, @Param("floorState") Integer floorState, @Param("state") Integer state, @Param("mapids") String[] mapids);

    boolean addFloorLockInfo(FloorLock floorLock);

    void editFloorLockInfo(FloorLock floorLock);

    FloorLock getFloorLockInfoInfoById(Integer id);

    void delFloorLockInfo(String id);

    List<FloorLock> getConditionData(@Param("deviceNum") String deviceNum, @Param("place") Integer place, @Param("id") Integer id, @Param("map") Long map);
    List<FloorLock> getConditionDataById(@Param("deviceNum") String deviceNum, @Param("place") Integer place, @Param("id") Integer id, @Param("map") Long map);

    @Select("select\n" +
            "    f.device_num deviceNum, f.parking_name parkingName,f.start_time startTime, f.end_time endTime,concat(f.power,'%')as `power`,f.offline_time offlineTime,\n" +
            "    (select name from map_2d m where m.id = f.map) as mapName,\n" +
            "    (select mrf.name from map_relevance_floor mrf where mrf.map=pp.map and mrf.level=pp.floor) as floorName,\n" +
            "    CASE WHEN f.model = 2 THEN '正常模式' WHEN f.model = 3 THEN '升锁模式' WHEN f.model = 4 THEN '降锁模式' ELSE '未知状态' END AS model,\n" +
            "    CASE WHEN pp.state = 0 THEN '空闲' WHEN pp.state=1 THEN '已停' ELSE '未知状态' END as state,\n" +
            "    CASE WHEN f.networkstate = 0 THEN '离线' WHEN f.networkstate = 1 THEN '在线' WHEN f.networkstate = 2 THEN '低电量' ELSE '未知状态' END AS networkName,\n" +
            "    CASE WHEN f.floor_lock_state = 1 THEN '空闲' WHEN f.floor_lock_state = 0 THEN '已停' ELSE '位置异常' END AS detectionState,\n" +
            "    CASE WHEN f.floor_lock_state = 0 THEN '降锁' WHEN f.floor_lock_state = 1 THEN '升锁' WHEN floor_lock_state IN (3, 4) THEN '位置异常状态' ELSE '未知状态' END AS floorLockState\n" +
            "from floor_lock f\n" +
            "left join parking_place pp on f.place = pp.id\n" +
            "where f.map =#{mapId}")
    List<FloorLockVo> getFloorsLockInfo(String mapId);

    @Select({
            "<script>",
            "SELECT device_num,place, parking_name, floor_lock_state",
            "FROM floor_lock",
            "WHERE map = #{map}",
            "AND place IN",
            "<foreach item='placeId' collection='placeIds' open='(' separator=',' close=')'>",
            "#{placeId}",
            "</foreach>",
            "</script>"
    })
    List<FloorLock> getFloorLocksByMapAndPlaceIds(@Param("map") Long map, @Param("placeIds") List<Integer> placeIds);
    @Select({ "SELECT device_num,place, parking_name, floor_lock_state FROM floor_lock WHERE map = #{map} AND place =#{placeId}"})
    List<FloorLock> getFloorLocksByMapAndPlaceIds2(@Param("map") Long map, @Param("placeId") Long placeId);

    List<ParkingCompanyVo> getAllPlaceNameByMapId(@Param("mapIds") String[] mapIds, @Param("type") String type);
}