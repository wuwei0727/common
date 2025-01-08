package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.Camera;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2020/12/23
 */
public interface CameraMapper {
    /*
    * 查询项目下的所有网关信息 map-->地图id  connect-->连接状态 0否 1是
    * */
    List<Camera> findByAll(@Param("instanceid") Integer instanceid, @Param("map") Integer map,
                           @Param("name") String name);

    /*
     * 查询网关详情
     * */
    Camera findById(@Param("id") Integer id);

    /*
    * 重名判断
    * */
    Camera findByName(String name,@Param("instanceid") Integer instanceid);

    /*
    * 新增摄像头
    * */
    boolean addCamera(@Param("camera") Camera camera);

    /*
     * 修改摄像头
     * */
    boolean updateCamera(@Param("camera") Camera camera);


    /*
     * 删除摄像头
     * */
    boolean delCamera(@Param("ids") String[] ids);

    /*
     * 删除摄像头
     * */
    int delCameraByInstance(@Param("instanceid") Integer instanceid);

}
