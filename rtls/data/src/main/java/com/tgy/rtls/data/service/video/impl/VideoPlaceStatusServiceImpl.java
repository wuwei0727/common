package com.tgy.rtls.data.service.video.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.video.VideoPlaceStatus;
import com.tgy.rtls.data.mapper.video.VideoPlaceStatusMapper;
import com.tgy.rtls.data.service.video.VideoPlaceStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.video
*@Author: wuwei
*@CreateTime: 2023-12-13 11:01
*@Description: TODO
*@Version: 1.0
*/
@Service
public class VideoPlaceStatusServiceImpl extends ServiceImpl<VideoPlaceStatusMapper,VideoPlaceStatus>implements VideoPlaceStatusService{
    @Autowired
    private VideoPlaceStatusMapper videoPlaceStatusMapper;

    @Override
    public VideoPlaceStatus getPlaceByTime(Integer time,Integer place) {
        return videoPlaceStatusMapper.getPlaceByTime(time,place);
    }
}
