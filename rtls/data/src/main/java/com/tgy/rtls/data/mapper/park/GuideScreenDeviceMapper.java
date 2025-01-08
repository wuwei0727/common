package com.tgy.rtls.data.mapper.park;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.park.GuideScreenDevice;
import com.tgy.rtls.data.entity.park.ShowScreenConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.park
 * @Author: wuwei
 * @CreateTime: 2023-05-24 23:24
 * @Description: TODO
 * @Version: 1.0
 */
@Mapper
public interface GuideScreenDeviceMapper extends BaseMapper<GuideScreenDevice> {
    List<GuideScreenDevice> getAllGuideScreenDeviceOrConditionQuery(@Param("map") String map, @Param("deviceNum") String deviceNum, @Param("ip") String ip, @Param("locationName") String locationName, @Param("networkStatus") String networkStatus, @Param("desc") String desc,@Param("floorName") String floorName, @Param("mapids") String[] mapids);

    void addGuideScreenDevice(GuideScreenDevice guideScreenDevice);

    void delGuideScreenDevice(String id);
    void updateGuideScreenDevice(GuideScreenDevice guideScreenDevice);

    void updateByDeviceIdShowScreenConfigIsNull(String id);

    List<GuideScreenDevice> getGuideScreenDeviceById(@Param ("id") Integer id,@Param ("deviceId") String deviceId,@Param ("desc")String desc);
    List<Integer> selectAreaIdByPlaceId(Integer placeId);
    List<Integer> selectPlaceBindAreaId(Integer areaId);
    ShowScreenConfig selectScreenDeviceByAreaId(Integer areaId);

    List<GuideScreenDevice> getAllGuideScreenDeviceByNum( @Param("deviceNum") String deviceNum);

    Integer get4GDeviceNameByScreenName(@Param("ScreenName") String ScreenName,@Param("map") Integer map);
}