package com.tgy.rtls.data.mapper.park;

import com.tgy.rtls.data.entity.park.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 车位预约
 */
public interface BookMapper {

    void  addBookPlace(@Param("bookPlace")BookPlace bookPlace);
    void  updateBookPlace(@Param("bookPlace")BookPlace bookPlace);
    void  delBookPlace(@Param("ids")String[] ids);
    void  delStorePlaceById(@Param("id")String id);
    void  delLicense(@Param("ids")String[] ids);
    void  addWechat(@Param("weChatUser") WeChatUser weChatUser);
    WeChatUser findWeChatUserByUserid(@Param("id") Integer id,@Param("userid") String userid);
    void  addWechatLicense(@Param("weChatLicense") WeChatLicense weChatLicense);
    List<WeChatLicense> findWeChatUserLicense(@Param("userid") Integer userid,@Param("license") String license);
    List<BookPlace> selectFeeByLicenseAndMap(@Param("license")String  license, @Param("map")Integer  map, @Param("charge")Integer  charge);

    List<BookPlace> selectBookPlaceConfix(@Param("place") Integer place);


    RealTimeData selectRealTimeData(Integer map);
    PlaceUseRecord selectPlaceUseRecordByPlaceid(@Param("place")Integer place);
    void UpdatePlaceUseRecordByid(@Param("placeUseRecord") PlaceUseRecord placeUseRecord);

    ChargeUseRecord selectPlaceChargeRecordByPlaceid(@Param("place")Integer place);
    void UpdatePlaceChargeRecordByid(@Param("chargeUseRecord")ChargeUseRecord chargeUseRecord);
    int addPlaceUseRecord(@Param("placeUseRecord") PlaceUseRecord placeUseRecord);
    int addPlaceChargeRecord(@Param("chargeUseRecord")ChargeUseRecord chargeUseRecord);
    List<BookPlace> findBookInfoByUserid(@Param("license") String license, @Param("userid") Integer userid,@Param("mapId") Integer mapId);


    List<StorePlace>  getStorePlace(@Param("userid")Integer userid,@Param("map")Integer map);
    void  addStorePlace(@Param("storePlace")StorePlace storePlace);
    void  delStorePlace(@Param("ids")String[] ids);

    List<PlaceUseRecord> selectPlaceUseRecordByPlaceidAndMapid(@Param("mapId") Integer map, @Param("placeId") Integer placeId,@Param("time") String time);

    void delPlaceUseRecord(@Param("map")Integer map, @Param("placeId") Integer placeId);

    List<StorePlace> getStorePlaceByName(@Param("comName") String comName);

    void updateStorePlace(@Param("storePlace") StorePlace storePlace);

    List<StorePlace> getStorePlaceByFid(String fid);
}
