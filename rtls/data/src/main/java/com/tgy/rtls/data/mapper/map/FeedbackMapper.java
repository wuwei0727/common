package com.tgy.rtls.data.mapper.map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.map.Feedback;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.mapper.map
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
public interface FeedbackMapper extends BaseMapper<Feedback> {
    List<Feedback> getFeedbackInfo(@Param("map") Long map, @Param("placeName") String placeName,@Param("contactInfo") String contactInfo, @Param("feedbackType") String feedbackType,@Param("content") String content, @Param("desc") String desc, @Param("mapids") String[] mapids);
}