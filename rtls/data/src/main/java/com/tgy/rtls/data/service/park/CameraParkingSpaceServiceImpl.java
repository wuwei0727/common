package com.tgy.rtls.data.service.park;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.mapper.Camera.CameraParkingSpaceMapper;
import com.tgy.rtls.data.entity.Camera.CameraParkingSpace;
import com.tgy.rtls.data.service.park.impl.CameraParkingSpaceService;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2024-11-22 10:39
*@Description: TODO
*@Version: 1.0
*/
@Service
public class CameraParkingSpaceServiceImpl extends ServiceImpl<CameraParkingSpaceMapper, CameraParkingSpace> implements CameraParkingSpaceService{

}
