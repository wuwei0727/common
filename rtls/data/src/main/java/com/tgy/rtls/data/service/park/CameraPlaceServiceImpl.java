package com.tgy.rtls.data.service.park;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.mapper.Camera.CameraPlaceMapper;
import com.tgy.rtls.data.entity.Camera.CameraPlace;
import com.tgy.rtls.data.service.park.impl.CameraPlaceService;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2024-11-25 17:52
*@Description: TODO
*@Version: 1.0
*/
@Service
public class CameraPlaceServiceImpl extends ServiceImpl<CameraPlaceMapper, CameraPlace> implements CameraPlaceService{

}
