package com.tgy.rtls.data.service.map.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.map.Feedback;

import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.map.impl
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
public interface FeedbackService extends IService<Feedback>{
    List<Feedback> getFeedbackInfo(Long map, String placeName,String contactInfo, String feedbackType, String content, String desc, String[] mapids);
}
