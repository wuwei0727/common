package com.tgy.rtls.docking.service.park.impl;


import com.tgy.rtls.docking.mapper.PlaceMapper;
import com.tgy.rtls.docking.service.park.DockingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockingServiceImpl implements DockingService {
    @Autowired
    private PlaceMapper placeMapper;
    @Override
    public Integer updatePlaceById(Integer placeId, Integer state, String license) {
        return placeMapper.updatePlaceById(placeId, state,license);
    }

}
