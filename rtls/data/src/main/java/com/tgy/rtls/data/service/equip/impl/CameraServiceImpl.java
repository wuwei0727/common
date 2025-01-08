package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.entity.equip.Camera;
import com.tgy.rtls.data.mapper.equip.CameraMapper;
import com.tgy.rtls.data.service.equip.CameraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2020/12/23
 */
@Service
@Transactional
public class CameraServiceImpl implements CameraService {
    @Autowired(required = false)
    private CameraMapper cameraMapper;


    @Override
    public List<Camera> findByAll(Integer instanceid, Integer map, String name) {
        return cameraMapper.findByAll(instanceid,map,name);
    }

    @Override
    public Camera findById(Integer id) {
        return cameraMapper.findById(id);
    }

    @Override
    public Camera findByName(String name,Integer instanceid) {
        return cameraMapper.findByName(name,instanceid);
    }

    @Override
    public boolean addCamera(Camera camera) {
        return cameraMapper.addCamera(camera);
    }

    @Override
    public boolean updateCamera(Camera camera) {
        return cameraMapper.updateCamera(camera);
    }

    @Override
    public boolean delCamera(String[] ids) {
        return cameraMapper.delCamera(ids);
    }

    @Override
    public int delCameraByInstance(Integer instanceid) {
        return cameraMapper.delCameraByInstance(instanceid);
    }
}
