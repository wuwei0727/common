package com.tgy.rtls.data.service.park;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import com.tgy.rtls.data.entity.park.PlaceVideoDetection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.park
 * @Author: wuwei
 * @CreateTime: 2023-05-31 16:11
 * @Description: TODO
 * @Version: 1.0
 */
public interface PlaceVideoDetectionService  extends IService<PlaceVideoDetection> {

    List<PlaceVideoDetection> getAllGuideScreenDeviceOrConditionQuery(String map, String ip, String placeInquireAddress, String licenseInquireAddress, String desc,String status, String[] mapids);

    void addPlaceVideoDetection(PlaceVideoDetection placeVideoDetection);

    void updatePlaceVideoDetection(PlaceVideoDetection placeVideoDetection);

    void delPlaceVideoDetection(String[] split);

    List<PlaceVideoDetection> getPlaceVideoDetectionById(Integer id);

    List<PlaceVideoDetection> getPlaceVideoDetectiontByMap(String map,@Param("id") String id,String update);

    List<ParkingPlace> getAllPlaceByMap(String maps,String placeId);
    List<PlaceVideoDetection> getAllGuideScreenDeviceTest(String map);
    List<ParkingPlace> getAllPlaceByMapTest(String maps);
    ParkingPlace getPlaceByPlaceNamesTest(String mapId,String placeName);

    void updateLastUploadTime(String map);

    Long getLastUploadTime();

    void updateStatusToError(String map);}
