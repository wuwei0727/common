package com.tgy.rtls.data.service.video;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.video.VideoPlaceStatus;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.video.impl
*@Author: wuwei
*@CreateTime: 2023-12-13 11:01
*@Description: TODO
*@Version: 1.0
*/
public interface VideoPlaceStatusService extends IService<VideoPlaceStatus> {

    VideoPlaceStatus getPlaceByTime(Integer time,Integer place);
}
