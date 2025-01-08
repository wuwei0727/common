package com.tgy.rtls.data.entity.park;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class FeeCalcul {
 private float   fee;	//当天停车费收入
 private int totalCount;//	当天总的车辆数
 private float   averageStaytime;//	单次平均时长
 private int    monthlyRentCount;//	月租车辆数
 private int    nonMonthlyRentCount;//	非月租用户数量




}
