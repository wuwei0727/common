package com.tgy.rtls.data.service.vip;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.park.ParkingCompanyVo;
import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.entity.vip.FloorLock;
import com.tgy.rtls.data.mapper.vip.FloorLockMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
public class FloorLockServiceImpl extends ServiceImpl<FloorLockMapper, FloorLock> implements FloorLockService {
    @Autowired
    private FloorLockMapper floorLockMapper;

    @Override
    public List<FloorLock> getFloorLockInfo(Long map, String deviceNum, String parkingName, Integer placeState, String desc, String floorName, Integer networkstate, Integer floorState, Integer state, String[] mapids) {
        return floorLockMapper.getFloorLockInfo(map,deviceNum,parkingName,placeState,desc, floorName,networkstate,floorState,state,mapids);
    }

    @Override
    public boolean addFloorLockInfo(FloorLock floorLock) {
        return floorLockMapper.addFloorLockInfo(floorLock);
    }

    @Override
    public void editFloorLockInfo(FloorLock floorLock) {
        floorLockMapper.editFloorLockInfo(floorLock);
    }

    @Override
    public FloorLock getFloorLockInfoInfoById(Integer id) {
        return floorLockMapper.getFloorLockInfoInfoById(id);
    }

    @Override
    public void delFloorLockInfo(String[] split) {
        for (String id : split) {
            floorLockMapper.delFloorLockInfo(id);
        }
    }

    @Override
    public List<FloorLock> getConditionData(String deviceNum, Integer place, Integer id, Long map) {
        return floorLockMapper.getConditionData( deviceNum,place,id,map);
    }
    @Override
    public List<FloorLock> getConditionDataById(String deviceNum, Integer place, Integer id, Long map) {
        return floorLockMapper.getConditionDataById( deviceNum,place,id,map);
    }

    @Override
    public List<ParkingCompanyVo> getAllPlaceNameByMapId(String[] mapids, String type) {
        return floorLockMapper.getAllPlaceNameByMapId(mapids,type);
    }

    public static void main(String[] args) throws ParseException {
        // 测试kafka 初始化示例数据
        ViewVo  realTimeInAndOutData = new ViewVo();
        realTimeInAndOutData.setTime(null); // 这里设为null来测试

        // 示例的DateFormat
        DateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 示例的batteryTime数据
        org.json.JSONObject data = new org.json.JSONObject();
        org.json.JSONObject batteryTime = new org.json.JSONObject();
        batteryTime.put("time", System.currentTimeMillis() - 1000 * 60 * 20); // 20分钟前的时间戳
        data.put("batteryTime", batteryTime);

        long realTimeTimestamp;
        if (realTimeInAndOutData.getTime() != null) {
            realTimeTimestamp = dateFormat.parse(realTimeInAndOutData.getTime()).getTime();
        } else {
            long batteryTimestamp = data.getJSONObject("batteryTime").getLong("time");
            long currentTime = System.currentTimeMillis();
            long thirtyMinutesAgo = currentTime - 1000 * 60 * 30;

            if (batteryTimestamp >= thirtyMinutesAgo) {
                realTimeTimestamp = batteryTimestamp;
            } else {
                realTimeTimestamp = currentTime;
            }
        }

        System.out.println("Real Time Timestamp: " + new Date(realTimeTimestamp));
    }
}
