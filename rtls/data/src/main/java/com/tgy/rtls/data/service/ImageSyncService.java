package com.tgy.rtls.data.service;

import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.ShangJia;
import com.tgy.rtls.data.mapper.ImageSyncMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ImageSyncService {
    @Autowired
    private ImageSyncMapper imageSyncMapper;
    public List<ShangJia> findAllShangJia(){
        return imageSyncMapper.findAllShangJia();
    }

    public List<Map_2d> findAllMap2d(){
        return imageSyncMapper.findAllMap2d();
    }

}