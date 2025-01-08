package com.tgy.rtls.data.mapper.video;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.PlaceVo;
import org.apache.ibatis.annotations.Param;

import com.tgy.rtls.data.entity.video.VideoPlaceStatus;

import java.util.Map;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.video
*@Author: wuwei
*@CreateTime: 2023-12-13 11:01
*@Description: TODO
*@Version: 1.0
*/
public interface VideoPlaceStatusMapper extends BaseMapper<VideoPlaceStatus> {
    void updateBatchById(@Param("map") Map<String, VideoPlaceStatus> map);

    VideoPlaceStatus getPlaceByTime(@Param("minute") Integer minute, @Param("place") Integer place);
}