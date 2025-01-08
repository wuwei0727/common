package com.tgy.rtls.data.entity.view;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author wuwei
 * @date 2024/3/6 - 14:59
 */
@Data
@ApiModel(description = "大屏数据二次更新")
@Accessors(chain = true)
public class ViewVo2 {
    private String map;//地图id
    private String mapName;
    private String time;
    private String placeUseTotal;//车位使用总数
    private String total;//车位使用总数
    private String placeTotal;//车位总数
    private String placeName;//车位名
    private String placeSearchCount;//车位搜索总数
    private String businessName;//商家名
    private String businessSearchCount;//商家搜索总数
    private String totalVacantDuration;//车位空闲总时长
    private String reservationTotal;//预约总数
    private String platformUtilizationRate;//平台车位利用率
    private String mapPlatformUtilizationRate;//停车场车位利用率
    private String placeAvailabilityRate;//停车场车位空闲率
    private String placeNavigationTotal;//车位导航总数
    private String hour;
    private String nullPlaceNumber;//空车位数
    private String locationShareTotal;//位置分享总数
    private String userSearchTotal;//用户搜索总数
    private String reverseCarSearchTotal;//反向寻车总数
    private String idlePlaceNumber;//空闲车位数
    private String activeUsers;//活跃用户数
    private String placeNavigationUseRate;
    private String userTotal;
    private String perMonth;
    private Integer count;
    private String hourStart;
    private String hourEnd;
}
