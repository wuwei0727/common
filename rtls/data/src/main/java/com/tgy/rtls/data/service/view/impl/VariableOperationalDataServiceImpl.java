package com.tgy.rtls.data.service.view.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.entity.view.UserVo;
import com.tgy.rtls.data.entity.view.VariableOperationalData;
import com.tgy.rtls.data.entity.vo.*;
import com.tgy.rtls.data.mapper.view.VariableOperationalDataMapper;
import com.tgy.rtls.data.mapper.view.ViewMapper;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import com.tgy.rtls.data.service.view.VariableOperationalDataService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *@author wuwei
 *@date 2024/3/25 - 12:02
 */
@Service
public class VariableOperationalDataServiceImpl extends ServiceImpl<VariableOperationalDataMapper, VariableOperationalData> implements VariableOperationalDataService{
    @Resource
    private VariableOperationalDataMapper var;
    @Resource
    private MapHotspotDataRedisService mapHot;
    @Resource
    private ViewMapper viewMapper;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    @Override
    public List<UserStatisticsVo> getAllUserData(Integer map,String time,String start,String end) {
        List<UserStatisticsVo> userDataList = var.getUserStatistics(map,time,start,end);
        List<UserStatisticsVo> activeUserCount = var.getActiveUserCount(map,time,start,end);
        Integer totalUserCount = var.getTotalUserCount(map,time,start,end);
        List<UserVo> newUserCount = this.viewMapper.getUserTotal(String.valueOf(map), null);
        Integer totalVisitCount = var.getTotalVisitCount(map,time,start,end);
        Integer totalActiveUserCount = var.getTotalActiveUserCount(map,time,start,end);
        Long userTotal = countNewUsers(newUserCount, String.valueOf(map), time,start,end);

        IntStream.range(0, userDataList.size()).forEach(i -> {
            UserStatisticsVo userData = userDataList.get(i);
            if (i < activeUserCount.size()) {
                UserStatisticsVo activeUserData = activeUserCount.get(i);
                userData.setTime(activeUserData.getTime());
                userData.setActiveUserCount(activeUserData.getActiveUserCount());
            }
            if (i == 0) {
                userData.setTotalUserCount(totalUserCount);
                userData.setNewUserCount(Math.toIntExact(userTotal));
                userData.setTotalVisitCount(totalVisitCount);
                userData.setTotalActiveUserCount(totalActiveUserCount);
            }
        });

        return userDataList;
    }

    @Override
    public List<HotLocationVo> getHotLocationData(Integer map,String time,String start,String end) {
        List<UserHotData> userHotDataList = mapHot.getHotSearchByMap(map);
        Integer totalUserSearchCount = var.getTotalUserSearchCount(map,time,start,end);
        Integer totalShareCount = var.getTotalShareCount(map,time,start,end);
        return IntStream.range(0, userHotDataList.size())
                .mapToObj(i -> {
                    UserHotData userHotData = userHotDataList.get(i);
                    HotLocationVo hotLocationVo = new HotLocationVo();
                    hotLocationVo.setLocationName(userHotData.getName());
                    hotLocationVo.setSearchCount(userHotData.getScore().intValue());
                    if("3".equals(userHotData.getType())&&!"8".equals(userHotData.getTypes())){
                        hotLocationVo.setIsBusiness("是");
                    }
                    if (i == 0) {
                        hotLocationVo.setTotalUserSearchCount(totalUserSearchCount);
                        hotLocationVo.setTotalShareCount(totalShareCount);
                    }

                    return hotLocationVo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<DevicesVo> getDevicesData(Integer map, String time,String start,String end) {
        List<DevicesVo> devices = var.getDevices(map,time);
        List<DevicesVo> devicesTotal = var.getDevicesTotal(map,time);
        if (!devices.isEmpty() && !devicesTotal.isEmpty()) {
            DevicesVo totalDevice = devicesTotal.get(0);
            devices.stream().findFirst().ifPresent(firstDevice -> {
                firstDevice.setTotalDetectors(totalDevice.getTotalDetectors());
                firstDevice.setOnlineDetectors(totalDevice.getOnlineDetectors());
                firstDevice.setOfflineDetectors(totalDevice.getOfflineDetectors());
                firstDevice.setTotalBeacons(totalDevice.getTotalBeacons());
                firstDevice.setOnlineBeacons(totalDevice.getOnlineBeacons());
                firstDevice.setOfflineBeacons(totalDevice.getOfflineBeacons());
                firstDevice.setTotalGateways(totalDevice.getTotalGateways());
                firstDevice.setOnlineGateways(totalDevice.getOnlineGateways());
                firstDevice.setOfflineGateways(totalDevice.getOfflineGateways());
            });
        }
        return devices;
    }

    @Override
    public List<ParkingReservationVo> getParkingReservationData(Integer map, String time,String start,String end) {
        List<ParkingReservationVo> reservationVoList = var.getParkingReservation(map, time,start,end);
        List<ParkingReservationVo> reservationTotal = var.getParkingReservationTotal(map, time,start,end);
        if(!reservationVoList.isEmpty() && !reservationTotal.isEmpty()){
            ParkingReservationVo totalReservation = reservationTotal.get(0);
            reservationVoList.stream().findFirst().ifPresent(fistReservation -> {
                fistReservation.setTotalReservations(totalReservation.getTotalReservations());
            });
        }
        return reservationVoList;
    }

    @Override
    public List<ReverseCarSearchVo> getReverseCarSearchData(Integer map, String time,String start,String end) {
        List<ReverseCarSearchVo> reverseCarSearchList = var.getReverseCarSearchData(map, time,start,end);
        List<ReverseCarSearchVo> reverseCarSearchTotal = var.getReverseCarSearchTotal(map, time,start,end);
        if(!reverseCarSearchList.isEmpty() && !reverseCarSearchTotal.isEmpty()){
            ReverseCarSearchVo reverseCarSearch= reverseCarSearchTotal.get(0);
            reverseCarSearchList.stream().findFirst().ifPresent(fistReservation -> {
                fistReservation.setTotalReverseCarSearches(reverseCarSearch.getTotalReverseCarSearches());
            });
        }
        return reverseCarSearchList;
    }

    @Override
    public List<ParkingDataVo> getParkingData(Integer map, String time,String start,String end) {
        List<ParkingDataVo> parkingData = var.getParkingData(map, time,start,end);
        List<ParkingDataVo> nullPlaceNumber = var.getIdleParkingNumbers(map);

        List<ParkingDataVo> parkingTotal = var.getParkingTotal(map, time,start,end);
        // 根据idleParkingNumbers的大小赋值
        IntStream.range(0, Math.min(parkingData.size(), nullPlaceNumber.size()))
                .forEach(i -> {
                    ParkingDataVo parkingDataVo = parkingData.get(i);
                    ParkingDataVo idleParkingNumber = nullPlaceNumber.get(i);
                    parkingDataVo.setHourStart(idleParkingNumber.getHourStart());
                    parkingDataVo.setHourEnd(idleParkingNumber.getHourEnd());
                    parkingDataVo.setAvailableParkingSpots(idleParkingNumber.getAvailableParkingSpots());
                });

        // 根据parkingTotal的大小赋值
        IntStream.range(0, Math.min(parkingData.size(), parkingTotal.size()))
                .forEach(i -> {
                    ParkingDataVo parkingDataVo = parkingData.get(i);
                    ParkingDataVo totalParking = parkingTotal.get(i);
                    parkingDataVo.setTotalParkingSpots(totalParking.getTotalParkingSpots());
                    parkingDataVo.setOccupiedParkingSpots(totalParking.getOccupiedParkingSpots());
                    parkingDataVo.setFreeParkingSpots(totalParking.getFreeParkingSpots());
                    parkingDataVo.setChargingParkingSpots(totalParking.getChargingParkingSpots());
                    parkingDataVo.setDedicatedParkingSpots(totalParking.getDedicatedParkingSpots());
                    parkingDataVo.setVipParkingSpots(totalParking.getVipParkingSpots());
                });

        return parkingData;
    }

    @Override
    public List<ParkingUsageVo> getParkingUsageData(Integer map, String time,Integer monthSecond,String start,String end) {
        try {
            Integer day = null;
            Integer hour = null;

            if (time != null) {
                // 使用time计算
                day = Integer.parseInt(time) * 30;
                hour = Integer.parseInt(time) * 30 * 24;
            } else if (start != null && end != null) {
                // 使用start和end计算
                LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // 计算天数差
                day = (int) ChronoUnit.DAYS.between(startTime, endTime);
                if (day == 0) day = 1; // 如果是同一天，设置为1

                // 计算小时差
                hour = (int) ChronoUnit.HOURS.between(startTime, endTime);
                if (hour == 0) {
                    hour = 1; // 如果小时差为0，设置为1
                }
            }
            List<ParkingUsageVo> usageVoList = var.getParkingUsageData(map, time,monthSecond,start,end);
            List<ParkingUsageVo> navigationTotal = var.getParkingNavigationTotal(map, time,start,end);
            List<ParkingUsageVo> useTotal = var.getParkingUseTotal(map, time,start,end);
            List<ParkingUsageVo> usageTotal = var.getParkingUsageTotal(map, time, day,hour,start,end);

            if (!navigationTotal.isEmpty()) {
                Map<String, ParkingUsageVo> navigationMap = navigationTotal.stream()
                        .filter(vo->vo.getParkingSpotNumber()!=null)
                        .collect(Collectors.toMap(ParkingUsageVo::getParkingSpotNumber, Function.identity()));
                Set<String> existingParkingSpotNumbers = usageVoList.stream()
                        .filter(Objects::nonNull)                  // 过滤掉 ParkingSpotNumber 为 null 的元素
                        .map(ParkingUsageVo::getParkingSpotNumber)
                        .collect(Collectors.toSet());

                for (ParkingUsageVo vo : usageVoList) {
                    if (vo != null && vo.getParkingSpotNumber() != null) {
                        if (navigationMap.containsKey(vo.getParkingSpotNumber())) {
                            vo.setNavigationCount(navigationMap.get(vo.getParkingSpotNumber()).getNavigationCount());
                            log.error("placeName:" + vo.getParkingSpotNumber() + "导航次数:" + vo.getNavigationCount());

                        } else {
                            // 如果navigationTotal中没有对应的parkingSpotNumber，可以考虑添加默认的navigationCount值
                            vo.setNavigationCount(0); // 或者默认值，根据实际情况设定
                        }
                    }
                }

                // 处理navigationTotal中不在usageVoList中的parkingSpotNumber
                navigationTotal.stream()
                        .filter(navigationVo -> navigationVo.getParkingSpotNumber() != null) // 过滤掉 getParkingSpotNumber 为 null 的元素
                        .filter(navigationVo -> !existingParkingSpotNumbers.contains(navigationVo.getParkingSpotNumber()))
                        .forEach(usageVoList::add);


                Map<String, ParkingUsageVo> useTotalMap = useTotal.stream()
                        .collect(Collectors.toMap(ParkingUsageVo::getParkingSpotNumber, Function.identity()));

                for (ParkingUsageVo vo : usageVoList) {
                    if (vo != null && vo.getParkingSpotNumber() != null) {
                        if (useTotalMap.containsKey(vo.getParkingSpotNumber())) {
                            vo.setUsageCount(useTotalMap.get(vo.getParkingSpotNumber()).getUsageCount());
                        } else {
                            vo.setUsageCount(0); // 或者默认值，根据实际情况设定
                        }
                    }
                }
            }

            if (!usageVoList.isEmpty() && !usageTotal.isEmpty()) {
                ParkingUsageVo firstUsageVo = usageVoList.get(0);
                ParkingUsageVo totalUsageVo = usageTotal.get(0);

                firstUsageVo.setTotalParkingNavigations(totalUsageVo.getTotalParkingNavigations());
                firstUsageVo.setTotalParkingUsages(totalUsageVo.getTotalParkingUsages());
                double parkingNavigationUsageRate = 0.00;
                double platformParkingUtilizationRate = 0.00;
                double parkingLotIdleRate = 0.00;
                // 使用 DecimalFormat 格式化数值
                DecimalFormat df = new DecimalFormat("#.##");

                // 处理停车导航使用率
                if (totalUsageVo.getParkingNavigationUsageRate() != null && !totalUsageVo.getParkingNavigationUsageRate().isEmpty()
                        && !"null".equalsIgnoreCase(totalUsageVo.getParkingNavigationUsageRate())) {
                    try {
                        parkingNavigationUsageRate = Double.parseDouble(totalUsageVo.getParkingNavigationUsageRate()) * 100;
                    } catch (NumberFormatException e) {
                        parkingNavigationUsageRate = 0.00;
                    }
                }

                // 处理平台停车利用率
                if (totalUsageVo.getMapPlatformUtilizationRate() != null && !totalUsageVo.getMapPlatformUtilizationRate().isEmpty()
                        && !"null".equalsIgnoreCase(totalUsageVo.getMapPlatformUtilizationRate())) {
                    try {
                        platformParkingUtilizationRate = Double.parseDouble(totalUsageVo.getMapPlatformUtilizationRate()) * 100;
                    } catch (NumberFormatException e) {
                        platformParkingUtilizationRate = 0.00;
                    }
                }

                // 处理停车场空置率
                String parkingLotIdleRateStr = totalUsageVo.getParkingLotIdleRate();
                if (parkingLotIdleRateStr != null && !parkingLotIdleRateStr.isEmpty()
                        && !"null".equalsIgnoreCase(parkingLotIdleRateStr)) {
                    try {
                        if (parkingLotIdleRateStr.contains(".")) {
                            String[] parts = parkingLotIdleRateStr.split("\\.");
                            if (parts.length > 1 && parts[1].length() > 4) {
                                parkingLotIdleRate = Double.parseDouble(parkingLotIdleRateStr) * 100;
                            } else {
                                parkingLotIdleRate = Double.parseDouble(parkingLotIdleRateStr);
                            }
                        } else {
                            parkingLotIdleRate = Double.parseDouble(parkingLotIdleRateStr);
                        }
                    } catch (NumberFormatException e) {
                        parkingLotIdleRate = 0.00;
                    }
                }

                // 设置格式化后的值
                firstUsageVo.setParkingNavigationUsageRate(df.format(parkingNavigationUsageRate) + "%");
                firstUsageVo.setMapPlatformUtilizationRate(df.format(platformParkingUtilizationRate) + "%");
                firstUsageVo.setParkingLotIdleRate(df.format(parkingLotIdleRate) + "%");
            }

            usageVoList = usageVoList.stream()
                    .filter(vo -> vo != null && vo.getParkingSpotNumber() != null) // 过滤掉 vo 或 ParkingSpotNumber 为 null 的元素
                    .peek(vo -> {
                        if (vo.getIdleDurationSeconds() == null) {
                            vo.setIdleDurationSeconds("0.0000");
                        } else {
                            try {
                                double idleDurationStr = Double.parseDouble(vo.getIdleDurationSeconds());
                                if (!NullUtils.isEmpty(idleDurationStr) && idleDurationStr <= 0.0) {
                                    // 计算指定月数前的时间
                                    LocalDateTime now = LocalDateTime.now();
                                    LocalDateTime startDate = now.minusMonths(Long.parseLong(time));

                                    // 计算时间范围的总秒数
//                                long totalSeconds = ChronoUnit.SECONDS.between(startDate, now);
                                    long totalHours = ChronoUnit.HOURS.between(startDate, now);

                                    vo.setIdleDurationSeconds(String.valueOf(totalHours));
                                }
                            } catch (NumberFormatException e) {
                                log.error("Failed to parse IdleDurationSeconds for ParkingSpotNumber: " + vo.getParkingSpotNumber(), e);
                                vo.setIdleDurationSeconds("0.0000"); // 如果解析失败，设置为 null
                            }
                        }
                    })
                    .collect(Collectors.toList()); // 将结果收集回列表



            return usageVoList;
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);

        }
    }

//这是一个main方法，程序的入口
public static void main(String[] args) {
    // 计算指定月数前的时间
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startDate = now.minusMonths(1);

    // 计算时间范围的总秒数
    long totalSeconds = ChronoUnit.SECONDS.between(startDate, now);
    long totalHours = ChronoUnit.HOURS.between(startDate, now);

    System.out.println("totalHours = " + totalHours);
    System.out.println("totalSeconds = " + totalSeconds);
}

    public List<PlaceUseRecord> getAllPlaceUseRecord() {
        return var.getAllPlaceUseRecord();
    }

    @Override
    public void deleteRecordsOlderThan(int minutes) {
        List<PlaceUseRecord> recordsToDelete = getAllPlaceUseRecord().stream()
                .filter(record -> {
                    if (record.getStart() != null && record.getEnd() != null) {
                        try {
                            LocalDateTime startTime = LocalDateTime.parse(record.getStart(), formatter);
                            LocalDateTime endTime = LocalDateTime.parse(record.getEnd(), formatter);
                            Duration duration = Duration.between(startTime, endTime);
                            return duration.toMinutes() <= minutes;
                        } catch (Exception e) {
                            // 解析失败，忽略该记录
                            return false;
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        if (!recordsToDelete.isEmpty()) {
            List<Long> idsToDelete = recordsToDelete.stream().map(PlaceUseRecord::getId).collect(Collectors.toList());
            log.error(idsToDelete.toString());

            var.deleteBatchByIds(idsToDelete);
        }
    }

    @Override
    public long countNewUsers(List<UserVo> userTotalList, String map, String time, String start, String end) {
        LocalDateTime startDateTime;
        LocalDateTime endDateTime  = LocalDateTime.now();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        if (start != null && end != null) {
            // 使用start和end参数
            startDateTime = LocalDateTime.parse(start, dateFormatter);
            endDateTime  = LocalDateTime.parse(end, dateFormatter);
        } else if (time != null) {
            // 使用time参数
            startDateTime = endDateTime .minusMonths(Long.parseLong(time));
        } else {
            // 如果既没有time也没有start和end，返回0或抛出异常
            return 0; // 或者抛出异常
        }

//            // 将开始和结束日期转换为LocalDateTime，并设置时间为一天的开始和结束
//            LocalDateTime startDateTime = startDate.atStartOfDay();
//            LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

            // 获取每个用户最早的登录时间
            Map<Long, LocalDateTime> userFirstLoginMap = userTotalList.stream()
                    .collect(Collectors.toMap(
                            UserVo::getUserIds,
                            vo -> LocalDateTime.parse(vo.getLoginTimes(), formatter),
                            (existing, replacement) -> existing.isBefore(replacement) ? existing : replacement
                    ));

            // 筛选在指定月份内首次登录的用户
        LocalDateTime finalEndDateTime = endDateTime;
        List<UserVo> newUsers = userFirstLoginMap.entrySet().stream()
                    .filter(entry -> {
                        LocalDateTime firstLoginDateTime = entry.getValue();
                        return !firstLoginDateTime.isBefore(startDateTime) && !firstLoginDateTime.isAfter(finalEndDateTime);
                    })
                    .map(entry -> new UserVo(entry.getKey(), entry.getValue().format(formatter)))
                    .collect(Collectors.toList());

            newUsers.sort(Comparator.comparingLong(UserVo::getUserIds));
            newUsers.forEach(user->{
                System.out.println(user.getUserIds());
            });
            // 返回新用户数量
            return newUsers.size();
    }

    //这是一个main方法，程序的入口
//    public static void main(String[] args){
//        String a1 = "0.009185185166666667";
//        String a2 = "1.74913043";
//
//        double num1 = Double.parseDouble(a1);
//        double num2 = Double.parseDouble(a2);
//
//        double result1 = (num1 - Math.floor(num1)) * 100;
//        double result2 = (num2 - Math.floor(num2)) * 100;
//
//        System.out.printf("a1提取小数点后四位并乘以100的结果为: %.4f\n", result1);
//        System.out.printf("a2提取小数点后四位并乘以100的结果为: %.4f\n", result2);
//
//    }
}
