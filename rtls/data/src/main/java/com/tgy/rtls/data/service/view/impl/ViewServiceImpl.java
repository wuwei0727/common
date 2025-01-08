package com.tgy.rtls.data.service.view.impl;

import com.tgy.rtls.data.entity.view.ViewVo;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.view.ViewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
public class ViewServiceImpl implements ViewService{
    @Autowired
    private ViewMapper viewMapper;
    @Override
    public Map<String,Object> getAllUserInfo(String month) {
        List<ViewVo> userAllInfo = viewMapper.getAllUserInfo(null,null,null);
        List<ViewVo> userAllArea = viewMapper.getAllUserArea();
        List<ViewVo> parkStateInfo = viewMapper.getParkingCountAndStateInfo();
        List<ViewVo> useFrequency = viewMapper.getCumulativeUseFrequency(null,null,null);
        List<ViewVo> subInfo = viewMapper.getSubCountAndStateInfo();
        List<ViewVo> gatewayInfo = viewMapper.getGatewayCountAndStateInfo();
        List<ViewVo> monthActiveUser = viewMapper.getMonthActiveUser(month);
        List<ViewVo> userTotalNumByMonth = viewMapper.getAllUserTotalNumByMonth();
        Map<String,Object> map = new HashMap<>();
        map.put("UserAllInfo",userAllInfo);
        map.put("UserAllArea",userAllArea);
        map.put("parkStateInfo",parkStateInfo);
        map.put("subInfo",subInfo);
        map.put("gatewayInfo",gatewayInfo);
        map.put("monthActiveUser",monthActiveUser);
        map.put("userTotalNumByMonth",userTotalNumByMonth);
        map.put("useFrequency",useFrequency);
        return map;
    }

    @Override
    @Async(value = "SubAsyncExecutor")
    public CompletableFuture<List<ViewVo>> getUseCarFrequency(){
        return CompletableFuture.completedFuture(viewMapper.getUseCarFrequency());
    }
}
