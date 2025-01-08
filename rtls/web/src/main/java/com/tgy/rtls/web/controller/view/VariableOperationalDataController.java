package com.tgy.rtls.web.controller.view;

import cn.hutool.core.convert.NumberChineseFormatter;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.UserHotData;
import com.tgy.rtls.data.entity.view.VariableOperationalData;
import com.tgy.rtls.data.entity.view.ViewVo2;
import com.tgy.rtls.data.entity.vo.*;
import com.tgy.rtls.data.mapper.view.VariableOperationalDataMapper;
import com.tgy.rtls.data.service.park.MapHotspotDataRedisService;
import com.tgy.rtls.data.service.view.VariableOperationalDataService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/variable_operational_data")
public class VariableOperationalDataController {
    @Resource
    private VariableOperationalDataService variableOperationalDataService;
    @Resource
    private VariableOperationalDataMapper variableOperationalDataMapper;
    @Resource
    private MapHotspotDataRedisService mapHotspotDataRedisService;

    @GetMapping("getVariableOperationalData")
    public CommonResult<Object> getVariableOperationalData(Integer id) {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),
                variableOperationalDataService.list(new LambdaQueryWrapper<VariableOperationalData>().eq(!NullUtils.isEmpty(id),VariableOperationalData::getId, id)));
    }

    @PostMapping("editVariableOperationalData")
    public CommonResult<Object> editVariableOperationalData(@RequestBody VariableOperationalData var) {
        variableOperationalDataService.updateById(var);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
    }



    @GetMapping("getAllUsers")
    public CommonResult<Object> getAllUsers() {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),variableOperationalDataMapper.getAllUsers());
    }

    @GetMapping("getAllMaps")
    public CommonResult<Object> getAllMaps() {
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),variableOperationalDataMapper.getAllMaps());
    }


    @GetMapping("getVariableOperationalData2")
    public CommonResult<Object> getVariableOperationalData2(Integer type,Integer map,String mapId,Integer pageIndex,Integer pageSize) {
        try {
            if(type==10){
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),mapHotspotDataRedisService.getKeysAndMembersByPattern(mapId));
            }
            if (pageSize != null && pageSize != -1) {
                PageHelper.startPage(pageIndex, pageSize);
            }

            List<ViewVo2> list = variableOperationalDataMapper.getVariableOperationalData(type,map);
            PageInfo<ViewVo2> pageInfo=new PageInfo<>(list);
            Map<String, Object> mapData = new HashMap<>();
            mapData.put("list", pageInfo.getList());
            mapData.put("pageIndex", pageIndex);
            mapData.put("total", pageInfo.getTotal());
            mapData.put("pages", pageInfo.getPages());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),mapData);
        } catch (Exception e) {

            return new CommonResult<>(400, LocalUtil.get(e.toString()));
        }
    }
    @PostMapping("addVariableOperationalData")
    public CommonResult<Object> addVariableOperationalData(Integer type, Integer year, Integer month, Integer numDays,Integer numbers, Integer map,Integer userId,  Integer businessId,  Integer visitCount, Integer placeId,
                                                           String placeName, String license, String reservationPerson, String phone,Integer hourDifference) {
        int result; int mapCount;
        switch (type) {
            case 1:
                variableOperationalDataMapper.insertUsersTotal(year, month, numDays, numbers,map,visitCount);
                break;
            case 2:
                mapCount = variableOperationalDataMapper.getUserSearchTotal(map);
                result = mapCount+numbers;
                variableOperationalDataMapper.insertUserSearchTotal(year, month, numDays, numbers,map,result,secureRandom12DigitNumber());
                break;
            case 3:
                variableOperationalDataMapper.insertTop10Business(year, month, numDays, numbers,map,businessId,secureRandom12DigitNumber());
                break;
            case 4:
                mapCount = variableOperationalDataMapper.getLocationShareTotal(map);
                result = mapCount+numbers;
                variableOperationalDataMapper.insertLocationShareTotal(year, month, numDays, numbers,map,result,secureRandom12DigitNumber());
                break;
            case 5:
                variableOperationalDataMapper.insertPlaceUseTotal(year, month, numDays, numbers,hourDifference,map,placeId);
                break;
            case 6:
                variableOperationalDataMapper.insertPlaceNavigationTotal(year, month, numDays, numbers,map,placeId,placeName,secureRandom12DigitNumber());
                break;
            case 7:
                variableOperationalDataMapper.insertPlaceNavigationUseRate(year, month, numDays, numbers,map,placeId,placeName,secureRandom12DigitNumber());
                break;
            case 8:
                variableOperationalDataMapper.insertReservationTotal(year, month, numDays, numbers,map,placeId,placeName,license,reservationPerson,phone,secureRandom12DigitNumber());
                break;
            case 9:
                variableOperationalDataMapper.insertReverseCarSearchTotal(year, month, numDays, numbers,map,placeId,placeName,userId,secureRandom12DigitNumber());
                break;
            case 10:
                mapHotspotDataRedisService.storeHotSearch(new UserHotData(),numbers);
                break;
            case 11:
                variableOperationalDataMapper.insertUserVisitCount(year, month, numDays, numbers, map, userId);
                break;
            case 12:
                break;
            case 13:
                System.out.println("Type is 13");
                break;
            case 14:
                System.out.println("Type is 14");
                break;
            default:
                return new CommonResult<>(400, LocalUtil.get("Type is not between 1 and 11"));
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
    }

    @PostMapping("addVariableOperationalData1")
    public CommonResult<Object> addVariableOperationalData1(Integer type,Integer numbers,@RequestBody UserHotData userHotData) {
        if (type == 10) {
            mapHotspotDataRedisService.storeHotSearch(userHotData, numbers);
        } else {
            return new CommonResult<>(400, LocalUtil.get("Type is not 10"));
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS));
    }
    private static String secureRandom12DigitNumber() {
        SecureRandom secureRandom = new SecureRandom();
        return IntStream.range(0, 12)
                .mapToObj(i -> String.valueOf(secureRandom.nextInt(10)))
                .collect(Collectors.joining());

    }

    @GetMapping("/exportMultiSheet/park")
    public void exportMultiSheet(HttpServletResponse response,Integer map,String time,String mapName,String start,String end) throws IOException {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            // 构建文件名
            String fileName;
            if (time != null) {
                fileName = mapName + "近" + time + "个月数据统计";
            } else if (start != null && end != null) {
                // 格式化日期，只显示年月日
                String startDate = start.substring(0, 10);  // 截取 yyyy-MM-dd
                String endDate = end.substring(0, 10);      // 截取 yyyy-MM-dd
                fileName = mapName + startDate + "至" + endDate + "数据统计";
            } else {
                fileName = mapName + "数据统计";  // 默认文件名
            }

            // 设置响应头
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");
            if (time != null && (start != null || end != null)) {
                return;
            }
            String validTime = (time != null && time.trim().isEmpty()) ? null : time;
            List<UserStatisticsVo> sheet1Data = variableOperationalDataService.getAllUserData(map,validTime,start,end);
            List<HotLocationVo> sheet2Data = variableOperationalDataService.getHotLocationData(map,validTime,start,end);
            List<DevicesVo> sheet3Data = variableOperationalDataService.getDevicesData(map,validTime,start,end);
            List<ParkingReservationVo> sheet4Data = variableOperationalDataService.getParkingReservationData(map,validTime,start,end);
            List<ReverseCarSearchVo> sheet5Data = variableOperationalDataService.getReverseCarSearchData(map,validTime,start,end);
            List<ParkingDataVo> sheet6Data = variableOperationalDataService.getParkingData(map,validTime,start,end);
            List<ParkingUsageVo> sheet7Data = variableOperationalDataService.getParkingUsageData(map,validTime, validTime != null ? Integer.parseInt(validTime) * 30 * 24 * 60 * 60 : null,start,end);
            try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {

                WriteSheet sheet1 = EasyExcel.writerSheet(0, "用户数据统计").head(UserStatisticsVo.class).build();
                excelWriter.write(sheet1Data, sheet1);

                WriteSheet sheet2 = EasyExcel.writerSheet(1, "热门地点数据统计").head(HotLocationVo.class).build();
                excelWriter.write(sheet2Data, sheet2);

                WriteSheet sheet3 = EasyExcel.writerSheet(2, "设备数据统计").head(DevicesVo.class).build();
                excelWriter.write(sheet3Data, sheet3);

                WriteSheet sheet4 = EasyExcel.writerSheet(3, "车位预约数据统计").head(ParkingReservationVo.class).build();
                excelWriter.write(sheet4Data, sheet4);

                WriteSheet sheet5 = EasyExcel.writerSheet(4, "车位查找数据统计").head(ReverseCarSearchVo.class).build();
                excelWriter.write(sheet5Data, sheet5);

                WriteSheet sheet6 = EasyExcel.writerSheet(5, "车位数据统计").head(ParkingDataVo.class).build();
                excelWriter.write(sheet6Data, sheet6);

                WriteSheet sheet7 = EasyExcel.writerSheet(6, "车位使用数据统计").head(ParkingUsageVo.class).build();
                excelWriter.write(sheet7Data, sheet7);
            }
        } catch (Exception e) {
            // 如果发生异常，则不导出文件并返回相应的错误信息
            response.reset();  // 清除已经设置的响应头
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("导出文件时发生错误：" + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().flush();
        }
    }

//    public static void main(String[] args) {
//        int number1 = 2;
//        int number3 = 12;
//
//        String chinese1 = NumberChineseFormatter.format(number1, false);
//        String chinese3 = NumberChineseFormatter.format(number3, false);
//
//        System.out.println(number1 + " 转换为中文: " + chinese1); // 输出: 1 转换为中文: 一
//        System.out.println(number3 + " 转换为中文: " + chinese3); // 输出: 3 转换为中文: 三
//    }


    public static void main(String[] args) {
        System.out.println(generateChinesePeriod(12)); // 输出: 一年
        System.out.println(generateChinesePeriod(3));  // 输出: 三个月
        System.out.println(generateChinesePeriod(6));  // 输出: 六个月
        System.out.println(generateChinesePeriod(1));  // 输出: 一个月
    }

    public static String generateChinesePeriod(int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("Months should be greater than 0");
        }

        String chineseNumber = NumberChineseFormatter.format(months, false);
        String period;

        switch (months) {
            case 12:
                period = "一年";
                break;
            case 1:
                period = "一个月";
                break;
            default:
                period = chineseNumber + "个月";
                break;
        }

        return period;
    }

    @Scheduled(fixedRate = 30000)//5
    @GetMapping("/deleteOldRecords")
    public void deleteOldRecords() {
        int minutes = 5;
        variableOperationalDataService.deleteRecordsOlderThan(minutes);
    }
}
