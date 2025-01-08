package com.tgy.rtls.data.service.park.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.park.ParkingElevatorBinding;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.park.ParkingElevatorBindingMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.park.ParkingElevatorBindingService;
import com.tgy.rtls.data.tool.IpUtil;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2023-09-12 09:36
*@Description: TODO
*@Version: 1.0
*/
@Service
public class ParkingElevatorBindingServiceImpl  extends ServiceImpl<ParkingElevatorBindingMapper,ParkingElevatorBinding> implements ParkingElevatorBindingService{
    @Resource
    private ParkingElevatorBindingMapper parkingElevatorBindingMapper;
    @Autowired
    private ParkMapper parkMapper;
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private OperationlogService operationlogService;

    @Override
    public List<ParkingElevatorBinding> getByConditions(String name, Integer map, String building, Integer floor, String placeName, String desc, String floorName, String objectType, String[] maps, String fid)
    {
        return parkingElevatorBindingMapper.getByConditions(name,map,building,floor,placeName,desc,floorName,objectType,maps,fid);
    }

    @Override
    public Boolean addParkingElevatorBinding(ParkingElevatorBinding peb) {
        return parkingElevatorBindingMapper.addParkingElevatorBinding(peb);
    }

    @Override
    public boolean updateParkingElevatorBinding(ParkingElevatorBinding peb) {
        return parkingElevatorBindingMapper.updateParkingElevatorBinding(peb);
    }

    @Override
    public List<ParkingElevatorBinding> getParkingElevatorBindingById(String id) {
        return parkingElevatorBindingMapper.getParkingElevatorBindingById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delParkingElevatorBinding(String[] split, HttpServletRequest request, String[] placeId) {
        LocalDateTime now = LocalDateTime.now();
        Member member = (Member) SecurityUtils.getSubject().getPrincipal();
        for (String id : split) {
            if(parkingElevatorBindingMapper.deleteByPrimaryKey(id)){

                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                // operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.GUIDE_SCREEN_CONFIG)), now);
            }
        }
        if(null!=placeId&&!NullUtils.isEmpty(placeId)){
            for (String place : placeId) {
                ParkingPlace parkingPlace = new ParkingPlace();
                parkingPlace.setId(Integer.valueOf(place));
                parkingPlace.setElevatorId(null);
                parkMapper.updatePlaceCompany(parkingPlace);
            }
        }

        return true;
    }

}
