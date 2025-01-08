package com.tgy.rtls.web.controller.map;

import com.tgy.rtls.data.service.map.impl.FeedbackTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.map
 * @Author: wuwei
 * @CreateTime: 2024-12-05 17:15
 * @Description: TODO
 * @Version: 1.0
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping(value = "/map")
public class FeedbackTypeController {

    @Autowired
    private FeedbackTypeService feedbackTypeService;


}
