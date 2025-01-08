package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.Camera;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip
 * @date 2020/12/23
 */
public interface CameraService {
    /*
     * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
     * */
    List<Camera> findByAll(Integer instanceid, Integer map, String name);

    /*
     * 查询网关详情
     * */
    Camera findById(Integer id);

    /*
     * 网关ip重名判断
     * */
    Camera findByName(String name, Integer instanceid);

    /*
     * 新增摄像头
     * */
    boolean addCamera(Camera camera);

    /*
     * 修改网关
     * */
    boolean updateCamera(Camera camera);

    /*
     * 修改网关连接状态
     * */
/*
    void updateGatewayConnect(Integer id, Integer connect);
*/

    /*
     * 删除摄像头
     * */
    boolean delCamera(String[] ids);

    /*
     * 删除camera
     * */
    int delCameraByInstance(@Param("instanceid") Integer instanceid);
}
