package com.tgy.rtls.data.service.map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.entity.map.FeedbackType;
import com.tgy.rtls.data.mapper.map.FeedbackTypeMapper;
import com.tgy.rtls.data.service.map.impl.FeedbackTypeService;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.map
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
@Service
public class FeedbackTypeServiceImpl extends ServiceImpl<FeedbackTypeMapper, FeedbackType> implements FeedbackTypeService{

}
