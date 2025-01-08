package com.tgy.rtls.data.service.map;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.map.Feedback;
import com.tgy.rtls.data.mapper.map.FeedbackMapper;
import com.tgy.rtls.data.service.map.impl.FeedbackService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.map
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService{

    @Override
    public List<Feedback> getFeedbackInfo(Long map, String placeName,String contactInfo, String feedbackType, String content, String desc, String[] mapids) {
        return baseMapper.getFeedbackInfo(map, placeName,contactInfo, feedbackType, content, desc, mapids);
    }
}
