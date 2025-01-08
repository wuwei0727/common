package com.tgy.rtls.data.common;

import com.tgy.rtls.data.entity.park.PlaceUseRecord;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
*@Author: wuwei
*@CreateTime: 2024/6/13 11:30
*/
@Slf4j
public class ParkingUtil {
//    public static Map<String, Integer> processRecords(List<PlaceUseRecord> records, int placeTotal) {
//        Map<String, Set<Long>> hourPlaceMap = new HashMap<>(); // 使用Map来存储每个小时段对应的车位ID集合
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");
//
//        // 获取当前时间和24小时前的时间
//        Calendar currentCalendar = Calendar.getInstance();
//        Date currentTime = currentCalendar.getTime();
//        currentCalendar.add(Calendar.HOUR_OF_DAY, -24);
//        Date startTime = currentCalendar.getTime();
//        List<Long> placeList = new ArrayList<>();
//
//        // 遍历记录列表
//        for (PlaceUseRecord record : records) {
//            try {
//                if (record.getStart() == null) {
//                    // 如果开始时间为空，跳过这条记录
//                    continue;
//                }
//                // 解析开始时间
//                Date startDate = dateFormat.parse(record.getStart());
//                Date endDate = record.getEnd() == null ? currentTime : dateFormat.parse(record.getEnd());
//
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(startDate);
//
//                // 遍历每个小时段
//                while (!calendar.getTime().after(endDate)) {
//                    String hourKey = hourFormat.format(calendar.getTime());
//                    Set<Long> hourPlaces = hourPlaceMap.computeIfAbsent(hourKey, k -> new HashSet<>());
//                    placeList.addAll(hourPlaces);
//                    hourPlaceMap.computeIfAbsent(hourKey, k -> new HashSet<>()).add(Long.valueOf(record.getPlace()));
//                    calendar.add(Calendar.HOUR_OF_DAY, 1);
//                }
//
//                List<Long> duplicatePlaces = placeList.stream()
//                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
//                        .entrySet().stream()
//                        .filter(entry -> entry.getValue() > 1)
//                        .map(Map.Entry::getKey)
//                        .collect(Collectors.toList());
//
//                System.out.println("重复的place: " + duplicatePlaces);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//        }
//        // 计算每个小时段的实际使用数量
//        Map<String, Integer> hourUsageCounts = new HashMap<>();
//        for (Map.Entry<String, Set<Long>> entry : hourPlaceMap.entrySet()) {
//            String hourKey = entry.getKey();
//            int uniquePlaces = entry.getValue().size();
//            System.out.println("hourKey:"+hourKey+"——uniquePlaces = " + uniquePlaces);// 当前小时段内不同车位的数量
//            hourUsageCounts.put(hourKey, placeTotal - uniquePlaces);
//            if (uniquePlaces > 1150) {
//                List<Long> duplicatePlaces = entry.getValue().stream()
//                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
//                        .entrySet().stream()
//                        .filter(e -> e.getValue() > 1)
//                        .map(Map.Entry::getKey)
//                        .collect(Collectors.toList());
//                System.out.println("重复的place: " + duplicatePlaces);
//            }
//        }
//
//        // 过滤和排序时间段，并调整计数基于placeTotal
//        return hourUsageCounts.entrySet().stream()
//                .filter(entry -> {
//                    try {
//                        Date hourDate = hourFormat.parse(entry.getKey());
//                        // 过滤出在 startTime 和 currentTime 之间的时间段
//                        return !hourDate.before(startTime) && !hourDate.after(currentTime);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        return false;
//                    }
//                })
//                .sorted(Map.Entry.comparingByKey())
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (e1, e2) -> e1,
//                        LinkedHashMap::new
//                ));
//    }

    public static Map<String, Integer> processRecords(List<PlaceUseRecord> records, int placeTotal) {
        Map<String, Set<Long>> placeUsagePerHour = new HashMap<>(); // 用于跟踪每个时间点哪些车位已被使用
        Map<String, Integer> timeSlotCount = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");

        // 获取当前时间和24小时前的时间
        Calendar currentCalendar = Calendar.getInstance();
        Date currentTime = currentCalendar.getTime();
        currentCalendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = currentCalendar.getTime();

        for (PlaceUseRecord record : records) {
            try {
                if (record.getStart() == null || record.getPlace() == null) {
                    // 如果开始时间或车位ID为空，跳过这条记录
                    continue;
                }
                // 解析开始时间
                Date startDate = dateFormat.parse(record.getStart());
                Date endDate;
                if (record.getEnd() == null) {
                    // 如果结束时间为空，设置为当前时间
                    endDate = currentTime;
                } else {
                    // 解析结束时间
                    endDate = dateFormat.parse(record.getEnd());
                }

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                // 遍历每个小时段
                while (!calendar.getTime().after(endDate)) {
                    String timeSlot = hourFormat.format(calendar.getTime());
                    long placeId = record.getPlace();

                    // 只有当车位在该小时内未被使用时才增加计数
                    if (!placeUsagePerHour.containsKey(timeSlot) || !placeUsagePerHour.get(timeSlot).contains(placeId)) {
                        timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
                        placeUsagePerHour.computeIfAbsent(timeSlot, k -> new HashSet<>()).add(placeId);
                    }
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 过滤和排序时间段，并调整计数基于placeTotal
        return timeSlotCount.entrySet().stream()
                .filter(entry -> {
                    try {
                        Date timeSlotDate = hourFormat.parse(entry.getKey());
                        // 过滤出在 startTime 和 currentTime 之间的时间段
                        return !timeSlotDate.before(startTime) && !timeSlotDate.after(currentTime);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> placeTotal - entry.getValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public static Map<String, Integer> processRecords2(List<PlaceUseRecord> records, int placeTotal) {

        Map<String, Integer> timeSlotCount = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");

        // 获取当前时间和24小时前的时间
        Calendar currentCalendar = Calendar.getInstance();
        Date currentTime = currentCalendar.getTime();
        currentCalendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = currentCalendar.getTime();

        Map<Long, List<TimeInterval>> placeIntervals = new HashMap<>();

        for (PlaceUseRecord record : records) {
            try {
                if (record.getStart() == null) {
                    // 如果开始时间为空，跳过这条记录
                    continue;
                }
                // 解析开始时间
                Date startDate = dateFormat.parse(record.getStart());
                Date endDate;
                if (record.getEnd() == null) {
                    // 如果结束时间为空，设置为当前时间
                    endDate = currentTime;
                } else {
                    // 解析结束时间
                    endDate = dateFormat.parse(record.getEnd());
                }

                List<TimeInterval> intervals = placeIntervals.getOrDefault(record.getPlace(), new ArrayList<>());
                intervals.add(new TimeInterval(startDate, endDate));
                placeIntervals.put(Long.valueOf(record.getPlace()), intervals);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 合并时间间隔并计数
        for (Map.Entry<Long, List<TimeInterval>> entry : placeIntervals.entrySet()) {
            List<TimeInterval> mergedIntervals = mergeIntervals(entry.getValue());

            for (TimeInterval interval : mergedIntervals) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(interval.start);

                while (!calendar.getTime().after(interval.end)) {
                    String timeSlot = hourFormat.format(calendar.getTime());
                    timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
            }
        }

        // 过滤和排序时间段，并调整计数基于placeTotal
        return timeSlotCount.entrySet().stream()
                .filter(entry -> {
                    try {
                        Date timeSlotDate = hourFormat.parse(entry.getKey());
                        // 过滤出在 startTime 和 currentTime 之间的时间段
                        return !timeSlotDate.before(startTime) && !timeSlotDate.after(currentTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> placeTotal - entry.getValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    public static Map<String, Set<Long>> processRecords3(List<PlaceUseRecord> records, int placeTotal) {
        Map<String, Set<Long>> timeSlotPlaces = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");

        // 获取当前时间和24小时前的时间
        Calendar currentCalendar = Calendar.getInstance();
        Date currentTime = currentCalendar.getTime();
        currentCalendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = currentCalendar.getTime();

        Map<Long, List<TimeInterval>> placeIntervals = new HashMap<>();

        for (PlaceUseRecord record : records) {
            try {
                if (record.getStart() == null) {
                    // 如果开始时间为空，跳过这条记录
                    continue;
                }
                // 解析开始时间
                Date startDate = dateFormat.parse(record.getStart());
                Date endDate;
                if (record.getEnd() == null) {
                    // 如果结束时间为空，设置为当前时间
                    endDate = currentTime;
                } else {
                    // 解析结束时间
                    endDate = dateFormat.parse(record.getEnd());
                }

                List<TimeInterval> intervals = placeIntervals.getOrDefault(record.getPlace(), new ArrayList<>());
                intervals.add(new TimeInterval(startDate, endDate));
                placeIntervals.put(Long.valueOf(record.getPlace()), intervals);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 合并时间间隔并计数
        for (Map.Entry<Long, List<TimeInterval>> entry : placeIntervals.entrySet()) {
            List<TimeInterval> mergedIntervals = mergeIntervals(entry.getValue());

            for (TimeInterval interval : mergedIntervals) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(interval.start);

                while (!calendar.getTime().after(interval.end)) {
                    String timeSlot = hourFormat.format(calendar.getTime());
                    timeSlotPlaces.putIfAbsent(timeSlot, new HashSet<>());
                    timeSlotPlaces.get(timeSlot).add(entry.getKey());
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
            }
        }

        // 过滤和排序时间段
        return timeSlotPlaces.entrySet().stream()
                .filter(entry -> {
                    try {
                        Date timeSlotDate = hourFormat.parse(entry.getKey());
                        // 过滤出在 startTime 和 currentTime 之间的时间段
                        return !timeSlotDate.before(startTime) && !timeSlotDate.after(currentTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    // 合并时间间隔
    private static List<TimeInterval> mergeIntervals(List<TimeInterval> intervals) {
        if (intervals.isEmpty()) {
            return intervals;
        }

        intervals.sort(Comparator.comparing(interval -> interval.start));
        List<TimeInterval> merged = new ArrayList<>();
        TimeInterval current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            TimeInterval next = intervals.get(i);

            if (current.end.after(next.start)) {
                current.end = current.end.after(next.end) ? current.end : next.end;
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        return merged;
    }



    public static Map<String, Integer> processRecords5(List<PlaceUseRecord> records, int placeTotal) {
        Map<String, Integer> timeSlotCount = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat hourFormat = new SimpleDateFormat("yyyy-MM-dd HH");

        // 获取当前时间和24小时前的时间
        Calendar currentCalendar = Calendar.getInstance();
        Date currentTime = currentCalendar.getTime();
        currentCalendar.add(Calendar.HOUR_OF_DAY, -24);
        Date startTime = currentCalendar.getTime();

        Map<Long, Set<String>> placeTimeSlots = new HashMap<>();

        for (PlaceUseRecord record : records) {
            try {
                if (record.getStart() == null) {
                    // 如果开始时间为空，跳过这条记录
                    continue;
                }
                // 解析开始时间
                Date startDate = dateFormat.parse(record.getStart());
                Date endDate;
                if (record.getEnd() == null) {
                    // 如果结束时间为空，设置为当前时间
                    endDate = currentTime;
                } else {
                    // 解析结束时间
                    endDate = dateFormat.parse(record.getEnd());
                }

                Set<String> timeSlots = placeTimeSlots.getOrDefault(record.getPlace(), new HashSet<>());

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(startDate);

                while (!calendar.getTime().after(endDate)) {
                    String timeSlot = hourFormat.format(calendar.getTime());
                    if (!timeSlots.contains(timeSlot)) {
                        timeSlots.add(timeSlot);
                        timeSlotCount.put(timeSlot, timeSlotCount.getOrDefault(timeSlot, 0) + 1);
                    }
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }

                placeTimeSlots.put(Long.valueOf(record.getPlace()), timeSlots);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 过滤和排序时间段，并调整计数基于placeTotal
        return timeSlotCount.entrySet().stream()
                .filter(entry -> {
                    try {
                        Date timeSlotDate = hourFormat.parse(entry.getKey());
                        // 过滤出在 startTime 和 currentTime 之间的时间段
                        return !timeSlotDate.before(startTime) && !timeSlotDate.after(currentTime);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                })
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> placeTotal - entry.getValue(),
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }


    static class TimeInterval {
        Date start;
        Date end;

        TimeInterval(Date start, Date end) {
            this.start = start;
            this.end = end;
        }
    }

}