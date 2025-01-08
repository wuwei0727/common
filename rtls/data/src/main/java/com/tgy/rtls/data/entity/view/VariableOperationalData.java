package com.tgy.rtls.data.entity.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@author wuwei
 *@date 2024/3/25 - 12:03
 */

/**
 * 可变运营数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "variable_operational_data")
public class VariableOperationalData {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "PlaceNavigationTotal")
    private String placeNavigationTotal;

    @TableField(value = "PlaceNavigationUseRate")
    private String placeNavigationUseRate;

    @TableField(value = "PlatformPlaceUtilizationRate")
    private String platformPlaceUtilizationRate;

    @TableField(value = "ReservationTotal")
    private String reservationTotal;

    @TableField(value = "ReverseCarSearchTotal")
    private String reverseCarSearchTotal;

    @TableField(value = "PlaceAvailabilityRate")
    private String placeAvailabilityRate;

    @TableField(value = "PerHourNullPlaceNumber")
    private Integer perHourNullPlaceNumber;

    @TableField(value = "PlaceUseTotal")
    private String placeUseTotal;

    @TableField(value = "PlaceIdleTotalDuration")
    private String placeIdleTotalDuration;

    @TableField(value = "MapPlaceUtilizationRate")
    private String mapPlaceUtilizationRate;

    @TableField(value = "IdlePlaceNumber")
    private Integer idlePlaceNumber;

    @TableField(value = "UserTotal")
    private String userTotal;

    @TableField(value = "UseFrequency")
    private String useFrequency;

    @TableField(value = "ActiveUserTotal")
    private String activeUserTotal;

    @TableField(value = "NewUsersNumber")
    private Integer newUsersNumber;

    @TableField(value = "UserSearchTotal")
    private String userSearchTotal;

    @TableField(value = "LocationShareTotal")
    private String locationShareTotal;

    @TableField(value = "DetectorCount")
    private String detectorCount;

    @TableField(value = "SubCount")
    private String subCount;

    @TableField(value = "GatewayCount")
    private String gatewayCount;

    @TableField(value = "MonthlyActiveUsers")
    private String monthlyActiveUsers;

    @TableField(value = "Top10Business")
    private String top10Business;
}