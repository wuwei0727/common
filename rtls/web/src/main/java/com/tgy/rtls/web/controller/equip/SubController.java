package com.tgy.rtls.web.controller.equip;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.equip.DeviceAlarms;
import com.tgy.rtls.data.entity.equip.SubFirmware;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.eventserver.DeviceDeleteEvent;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.park.BeaconCount;
import com.tgy.rtls.data.entity.park.BeaconVolt;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.tool.DateUtils;
import com.tgy.rtls.data.tool.IpUtil;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import com.tgy.rtls.web.aspect.MyPermission;
import com.tgy.rtls.web.util.FileUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.dreamlu.mica.ip2region.core.Ip2regionSearcher;
import net.sf.json.JSONObject;
import org.apache.commons.io.FilenameUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.web.controller.equip
 * @date 2020/10/19
 */
@RestController
@RequestMapping(value = "/sub")
@CrossOrigin
/**
 * 蓝牙信标
 */
public class SubController {
    @Autowired
    private SubService subService;
    @Autowired
    private BsConfigService bsConfigService;
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private FastFileStorageClient fastFileStorageClient;
    @Autowired(required = false)
    private WebSocketLocation webSocketLocation;
    @Autowired
    LocalUtil localUtil;
    //上传真实地址
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    @Value("${file.url}")
    private String url;
    @Value("${coal.ip}")
    private String ip;
    @Value("${server.port}")
    private String port;
    @Value("${fdfs.url}")
    private String fdfsUrl;//分布式文件系统地址
    @Autowired
    private Ip2regionSearcher ip2regionSearcher;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @MyPermission
    @RequestMapping(value = "/getSubSel")
    @ApiOperation(value = "分站查询接口", notes = "无")
    public CommonResult<Object> getSubSel(String num, Integer type, Integer networkstate,Integer power,String typeName, Integer powerstate, Integer relevance,
                                          Integer map, Integer error, Integer day,
                                          @RequestParam(value = "desc", defaultValue = "addTime desc") String desc,
                                          @RequestParam(value = "pageIndex", defaultValue = "1") Integer pageIndex,String floorName,
                                          @RequestParam(value = "pageSize", defaultValue = "1") Integer pageSize, String maps) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String[] mapids = null;
            if (!NullUtils.isEmpty(maps)) {
                mapids = maps.split(",");
            }
            String start = null;
            String end = null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (day != null && day > 0) {
                Date current = new Date();
                Calendar c2 = Calendar.getInstance();
                start = DateUtils.printCalendar(DateUtils.getBeforeN_Day(c2, day));
                end = dateFormat.format(current.getTime());
            }
            //按条件查询
            //pageSize<0时查询所有
            if (pageSize < 0) {
                List<BsSyn> substations = subService.findByAll2(num, type,typeName, networkstate, powerstate,power, relevance, map, error, desc, start, end, null, floorName,mapids);
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), substations);
            }
            /*
             * 分页 total-->总数量
             * */
            int total = subService.findByAll2(num, type,typeName, networkstate, powerstate,power, relevance, map, error, desc, start, end, null, floorName,mapids).size();
            if (pageIndex > total / pageSize) {
                if (total % pageSize == 0) {
                    pageIndex = total / pageSize;
                } else {
                    pageIndex = total / pageSize + 1;
                }
            }
            PageHelper.startPage(pageIndex, pageSize);
            List<BsSyn> substations = subService.findByAll2(num, type,typeName, networkstate, powerstate, power,relevance, map, error, desc, start, end, null, floorName,mapids);
            PageInfo<BsSyn> pageInfo = new PageInfo<>(substations);
            Map<String, Object> result = new HashMap<>();
            result.put("list", pageInfo.getList());
            result.put("pageIndex", pageIndex);
            result.put("total", pageInfo.getTotal());
            result.put("pages", pageInfo.getPages());
            //生成操作日志-->查询分站数据
            operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.QUERY_SUB));
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), result);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions(value = {"sub:see","sub:edit"},logical = Logical.OR)
    @RequestMapping(value = "/getSubId/{id}")
    @ApiOperation(value = "分站详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "分站id", required = true, dataType = "int")
    public CommonResult<Substation> getSubId(@PathVariable("id") Integer id) {
        try {
            Substation substation = subService.findById(id);
            FileUtils.pullRemoteFileToLocal(substation.getBackground());
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), substation);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequestMapping(value = "/getSubNum/{num}")
    @ApiOperation(value = "分站详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "num", value = "分站", required = true, dataType = "String")
    public CommonResult<Object> getSubNum(@PathVariable("num") String num) {
        try {
            Substation substation = subService.findByNum(num);
            BsConfig bsInf1 = null;
            if (substation != null) {
                bsInf1 = bsConfigService.findByNum(num);
                if (bsInf1 != null) {
                    bsInf1.setMap(Integer.valueOf(substation.getMap()));
                }
            }

            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), bsInf1);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }

    @RequiresPermissions("sub:add")
    @RequestMapping(value = "/addSub")
    @ApiOperation(value = "分站新增接口", notes = "分站信息")
    public CommonResult<Object> addSub(Substation sub, MultipartFile file) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            Substation substation1 = subMapper.findByNum(sub.getNum(), localUtil.getLocale());
            if (substation1 != null) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.SUB_INUSE));
            }
            //实例
            sub.setInstanceid(instanceid);
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                //限制图片的宽高 480*272
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                if (bufferedImage != null) {
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    if (width > 480 || height > 272) {
                        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PICSIZE_ERROR));
                    }
                }
/*                    CommonResult<Object> commonResult= FileUtils.uploadFile(uploadFolder,file);
                //如果状态码不是200 直接return
                if (commonResult.getCode()!=200){
                    return commonResult;
                }
                //添加图片路径
                sub.setBackground(url+commonResult.getData());*/

                /*
                 * 将图片传输到文件服务器fdfs上
                 * */
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                sub.setBackground(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                sub.setBackgroundlocal(url + commonResult.getData());
            }
            if (subService.addSub(sub, fdfsUrl)) {
                //生成操作日志-->添加分站数据
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.ADD_SUB) + sub.getNum());
                return new CommonResult<>(200, LocalUtil.get(KafukaTopics.ADD_SUCCESS), sub.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.ADD_FAIL));
    }

    @PostMapping("/updateLifetime")
    @RequiresPermissions(value = "sub:ubl")
    @ApiOperation(value = "更新地图下所有信标的使用年限", notes = "根据地图ID更新所有信标的使用年限（月）")
    public CommonResult<Object> updateBeaconLifetime(@ApiParam(value = "地图ID", required = true) @RequestParam Integer map,
                                                     @ApiParam(value = "使用年限（月）", required = true) @RequestParam Integer lifetimeMonths,@RequestParam Integer type) {
        try {
            // 参数验证
            if (map == null || lifetimeMonths == null) {
                return new CommonResult<>(400, "参数不能为空");
            }

            if (lifetimeMonths <= 0) {
                return new CommonResult<>(400, "使用年限必须大于0");
            }

            return new CommonResult<>(200, "更新成功",subService.updateLifetimeByMap(map, lifetimeMonths,type));
        } catch (Exception e) {
            return new CommonResult<>(500, "更新失败：" + e.getMessage());
        }
    }


    @RequiresPermissions(value = {"sub:update","sub:see"},logical = Logical.OR)
    @RequestMapping(value = "/updateSub")
    @ApiOperation(value = "分站修改接口", notes = "分站信息")
    public CommonResult updateSub(Substation sub, MultipartFile file) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            int instanceid = Integer.parseInt(redisService.get("instance" + uid));
            Substation substation1 = subMapper.findById(sub.getId(), localUtil.getLocale());
            Substation substation2 = subMapper.findByNum(sub.getNum(), localUtil.getLocale());
            if (!substation1.getNum().equals(sub.getNum()) && substation2 != null) {
                return new CommonResult<>(400, LocalUtil.get(KafukaTopics.SUB_INUSE));
            }
            //实例
            sub.setInstanceid(instanceid);
            //修改时间
            sub.setUpdateTime(new Date());
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {

                //限制图片的宽高 480*272
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                if (bufferedImage != null) {
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    if (width > 480 || height > 272) {
                        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PICSIZE_ERROR));
                    }
                }
             /*   CommonResult<Object> commonResult= FileUtils.uploadFile(uploadFolder,file);
                //如果状态码不是200 直接return
                if (commonResult.getCode()!=200){
                    return commonResult;
                }
                //添加图片路径
                sub.setBackground(url+commonResult.getData());*/
                // sub.setLocationword(sub.getWord());
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                sub.setBackground(storePath.getFullPath());
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, storePath.getFullPath());
                sub.setBackgroundlocal(url + commonResult.getData());
            }
            if (subService.updateSub(sub, fdfsUrl)) {
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.UPDATE_SUB) + sub.getNum());
                if (substation1.getNetworkstate() == 0) {
                    return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SUBOFFLINE));
                } else {
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPDATE_SUCCESS), sub.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.UPDATE_FAIL));
    }

    @RequiresPermissions("sub:del")
    @RequestMapping(value = "/delSub/{ids}")
    @ApiOperation(value = "分站删除接口", notes = "分站id集")
    @ApiImplicitParam(paramType = "path", name = "ids", value = "分站id集", required = true, dataType = "String")
    public CommonResult delSub(@PathVariable("ids") String ids, HttpServletRequest request) {
        try {
            String uid = "12";
            LocalDateTime now = LocalDateTime.now();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            String num = subService.findByNameId(ids);
            if (subService.delSub(ids)) {
                eventPublisher.publishEvent(new DeviceDeleteEvent(this, ids, 1));
                String ip = IpUtil.getIpAddr(request);
                String address = ip2regionSearcher.getAddressAndIsp(ip);
                operationlogService.addUserOperationlog(member.getUid(),ip.concat((address == null ? "":address)), KafukaTopics.DELETE.concat(LocalUtil.get(KafukaTopics.BEACON_INFO)), now);
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/delSub/num/{num}")
    @ApiOperation(value = "分站删除接口", notes = "分站num")
    public CommonResult delSubByName(@PathVariable("num") String num) {
        try {
            String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }
            Substation sub = subService.findByNum(num);
            Integer id = sub.getId();
            String ids = id + "";
            if (subService.delSub(ids)) {
                operationlogService.addOperationlog(Integer.valueOf(uid), LocalUtil.get(KafukaTopics.DELETE_SUB) + num);
                return new CommonResult(200, LocalUtil.get(KafukaTopics.DELETE_SUCCESS));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
        return new CommonResult(400, LocalUtil.get(KafukaTopics.DELETE_FAIL));
    }

    @RequestMapping(value = "/startcheck")
    @ApiOperation(value = "巡检信标", notes = "")
    public CommonResult start(Integer map) {
        try {
            if (NullUtils.isEmpty(map)) {
                return new CommonResult(400, "请选择你要置为离线的地图");
            }
            List<BsSyn> list = subService.findByAll(null, null, null, null, null, map, null, null, null, null, null);
            for (BsSyn bs : list) {
                subService.updateSubNetworkstate(bs.getNum(), 0);
            }
        } catch (Exception e) {
            return new CommonResult(400, LocalUtil.get(KafukaTopics.OPEN_FAIL));
        }
        return new CommonResult(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
    }

    //这是一个main方法，程序的入口
    public static void main(String[] args) {
        // 定义固定的当前时间为 2024-11-13
        LocalDateTime fixedNow = LocalDateTime.now();

        // 定义使用寿命为 60 个月
        int lifetimeMonths = 60;

        // 创建三个不同的 Substation 实例，分别对应不同的使用时间和电量百分比
        Substation beacon1 = new Substation();
        beacon1.setAddTime(fixedNow.minusMonths(55));

        Substation beacon2 = new Substation();
        beacon2.setAddTime(fixedNow.minusMonths(45));

        Substation beacon3 = new Substation();
        beacon3.setAddTime(fixedNow.minusMonths(25) // AddTime: 2022-06-13
        );
        // 计算并打印每个信标的电量百分比和预期报警级别
        calculateAndPrintBatteryPercentage(beacon1, fixedNow, lifetimeMonths);
        calculateAndPrintBatteryPercentage(beacon2, fixedNow, lifetimeMonths);
        calculateAndPrintBatteryPercentage(beacon3, fixedNow, lifetimeMonths);
    }
    /**
     * 计算电量百分比并打印报警级别。
     *
     * @param sub             Substation 实例
     * @param now             当前时间
     * @param lifetimeMonths  使用寿命（月）
     */
    private static void calculateAndPrintBatteryPercentage(Substation sub, LocalDateTime now, int lifetimeMonths) {
        short batteryPercentage = 100; // 默认电量百分比为 100%
        Integer t1 = lifetimeMonths;

        if (t1 != null && t1 > 0) {
            LocalDateTime installTime = sub.getAddTime();
            long t2 = ChronoUnit.MONTHS.between(installTime, now); // 计算已使用时间（月）

            // 计算电量百分比
            double percentage = (1 - ((double) t2 / t1)) * 100;
            percentage = Math.round(percentage);
            batteryPercentage = (short) Math.max(0, Math.min(100, percentage));
        }

        // 确定报警级别
        String alarmLevel;
        if (batteryPercentage < 10) {
            alarmLevel = "高级别报警";
        } else if (batteryPercentage >= 10 && batteryPercentage <= 30) {
            alarmLevel = "中级别报警";
        } else if (batteryPercentage > 50) {
            alarmLevel = "解除报警";
        } else {
            alarmLevel = "无报警";
        }

        // 打印结果
        System.out.println("信标编号: " + sub.getNum()
                + ", 电量百分比: " + batteryPercentage + "%"
                + ", 预期报警级别: " + alarmLevel);
    }

    @RequestMapping(value = "/beacondata")
    @ApiOperation(value = "巡检信标1", notes = "")
    public CommonResult<Object> beacondata(@RequestBody List<BeaconVolt> list) {
        try {
            for (BeaconVolt beaconVolt : list) {
                System.out.println("信标编号: "+beaconVolt.getNum()+"信标电量: "+beaconVolt.getVolt());
                Substation sub = subService.findByNum(beaconVolt.getNum());

                // 计算电量百分比
                short batteryPercentage = 100;
                if(sub!=null){
                    float voltage = Float.parseFloat(beaconVolt.getVolt());
                    Integer power = sub.getPower();

                    if (voltage > 3.3f && power != null && power < 50) {
                        subService.updateAddTime(beaconVolt.getMap(), beaconVolt.getNum(), LocalDateTime.now());
                        sub = subService.findByNum(beaconVolt.getNum());
                    }

                    // 获取使用寿命(月)
                    Integer lifetimeMonths = sub.getLifetimeMonths();  // t1
                    if (lifetimeMonths != null && lifetimeMonths > 0) {
                        // 计算已使用时间(月)
                        LocalDateTime installTime = sub.getAddTime();
                        LocalDateTime now = LocalDateTime.now();
                        long monthsBetween = ChronoUnit.MONTHS.between(installTime, now);  // t2

                        // 计算电量百分比 (1 - t2/t1) * 100
                        double percentage = (1 - ((double)monthsBetween / lifetimeMonths)) * 100;
                        percentage = Math.round(percentage);
                        batteryPercentage = (short) (percentage <= 0 ? 0 : Math.min(100, percentage));                    }
                }
                if (sub != null && sub.getNetworkstate() == 0) {
                    JSONObject jsonArea = new JSONObject();
                    jsonArea.put("type", 5);
                    jsonArea.put("data", sub);
                    jsonArea.put("map", sub.getMap());
                    webSocketLocation.sendAll(jsonArea.toString());
                    if (sub.getMap() != null) {
                        BeaconCount data = subMapper.findCalcuuByMap(Integer.valueOf(sub.getMap()));
                        jsonArea.put("type", 12);
                        jsonArea.put("data", data);
                        jsonArea.put("map", sub.getMap());
                        jsonArea.put("Content-Type", "application/json; charset=utf-8");
                        webSocketLocation.sendAll(jsonArea.toString());
                    }

                }

                subService.updateSubBattery(beaconVolt.getNum(), beaconVolt.getVolt(), new Timestamp(System.currentTimeMillis()).toString(), batteryPercentage);
                subService.updateSubNetworkstate(beaconVolt.getNum(), 1);
                subMapper.addBeaconVolt(beaconVolt.getNum(), Float.parseFloat(beaconVolt.getVolt()));
                DeviceAlarms deviceAlarms = new DeviceAlarms();
                deviceAlarms.setEquipmentType(1);
                deviceAlarms.setAlarmType(2);
                deviceAlarms.setState(0);
                deviceAlarms.setNum(Integer.valueOf(beaconVolt.getNum()));
                if (sub != null) {
                    deviceAlarms.setDeviceId(sub.getId());
                }
                deviceAlarms.setMap(beaconVolt.getMap());
                DeviceAlarms existingDevice  = deviceAlarmsService.getOne(new QueryWrapper<DeviceAlarms>()
                        .ne("state", 1)
                        .eq("device_id", deviceAlarms.getDeviceId())
                        .eq("equipment_type", 1));
                if (!NullUtils.isEmpty(existingDevice )){
                    deviceAlarms.setId(existingDevice .getId());
                }
                if(NullUtils.isEmpty(existingDevice )||NullUtils.isEmpty(existingDevice .getStartTime())){
                    deviceAlarms.setStartTime(LocalDateTime.now());
                }

                handleBatteryAlarm(deviceAlarms, batteryPercentage, existingDevice, beaconVolt.getNum());

            }
        } catch (Exception e) {
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.FAIL));
        }
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
    }

    /**
     * 处理电量百分比对应的报警逻辑
     */
    private void handleBatteryAlarm(DeviceAlarms deviceAlarms, short batteryPercentage, DeviceAlarms existingDevice, String beaconNum) {
        if (batteryPercentage < 10) {
            // 高级别报警
            activateAlarm(deviceAlarms, 1, existingDevice);
//            // 更新网络状态为异常
//            subService.updateSubNetworkstate(beaconNum, 2);
            System.out.println("高级别报警触发，信标编号: " + beaconNum + ", 电量百分比: " + batteryPercentage + "%");
        } else if (batteryPercentage >= 10 && batteryPercentage <= 30) {
            // 中级别报警
            activateAlarm(deviceAlarms, 2, existingDevice);
            System.out.println("中级别报警触发，信标编号: " + beaconNum + ", 电量百分比: " + batteryPercentage + "%");
        } else if (batteryPercentage > 50) {
            // 解除报警
            if (!NullUtils.isEmpty(existingDevice)) {
                deviceAlarms.setState(1); // 报警解除
                deviceAlarms.setEndTime(LocalDateTime.now());
                deviceAlarm(deviceAlarms, NullUtils.isEmpty(existingDevice) ? null : existingDevice.getPriority(), existingDevice);
                System.out.println("报警解除，信标编号: " + beaconNum + ", 电量百分比: " + batteryPercentage + "%");
            }
        }
    }

    /**
     * 激活报警的辅助方法
     */
    private void activateAlarm(DeviceAlarms deviceAlarms, int priority, DeviceAlarms existingDevice) {
        deviceAlarms.setPriority(priority);
        deviceAlarms.setState(0); // 报警激活
        deviceAlarm(deviceAlarms, NullUtils.isEmpty(existingDevice) ? null : existingDevice.getPriority(), existingDevice);
    }

    public void deviceAlarm(DeviceAlarms deviceAlarms,Integer lastTimePriority,DeviceAlarms lastTimeDeviceAlarms){
         if(!NullUtils.isEmpty(lastTimePriority)&&!NullUtils.isEmpty(deviceAlarms.getPriority())&&!deviceAlarms.getPriority().equals(lastTimePriority)&&deviceAlarms.getState()==0){
            deviceAlarms.setStartTime(LocalDateTime.now());
        }
         if(NullUtils.isEmpty(lastTimeDeviceAlarms)||!NullUtils.isEmpty(lastTimeDeviceAlarms.getEndTime())){
             deviceAlarms.setId(null);
             deviceAlarms.setStartTime(LocalDateTime.now());
             deviceAlarmsService.save(deviceAlarms);
         }else {
             deviceAlarmsService.saveOrUpdate(deviceAlarms);
         }
    }

    /*    @RequestMapping(value = "/beacondata")
        @ApiOperation(value = "巡检信标",notes = "")
        public CommonResult beacondata(String data){
            try {
               *//* String uid = "12";
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();
            if (!NullUtils.isEmpty(member)) {
                uid = String.valueOf(member.getUid());
            }*//*
            // Substation sub = subService.findByNum(num);
            JSONArray array=   JSONArray.fromObject(data);
            int i=0;
            BeaconVolt beaconVolt_1=null;
            for (Object jsonObject:array
            ) {
                i++;
                JSONObject object= JSONObject.fromObject(jsonObject);
                BeaconVolt beaconVolt=(BeaconVolt)JSONObject.toBean(object, BeaconVolt.class);
                if(i==1){
                    beaconVolt_1=beaconVolt;
                }
                subService.updateSubBattery(beaconVolt.getNum(),beaconVolt.getVolt(), new Timestamp(new Date().getTime()).toString());
                subService.updateSubNetworkstate(beaconVolt.getNum(), 1);
                subMapper.addBeaconVolt(beaconVolt.getNum(),Float.valueOf(beaconVolt.getVolt()));
            }
            if(beaconVolt_1!=null) {
                Substation sub = subService.findByNum(beaconVolt_1.getNum());
                JSONObject jsonArea = new JSONObject();
                JSONObject json = new JSONObject();
                jsonArea.put("type", 5);
                jsonArea.put("data", json);
                jsonArea.put("map", data);
                webSocketLocation.sendAll(jsonArea.toString());
            }

        }catch (Exception e){
            return new CommonResult(400,LocalUtil.get(KafukaTopics.FAIL));
        }
        return  new CommonResult(200,LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
    }*/
    @RequestMapping(value = "/beaconCount")
    @ApiOperation(value = "查看信标统计状态", notes = "")
    public CommonResult beaconCount(Integer map) {
        CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), null);
        try {

            BeaconCount data = subMapper.findCalcuuByMap(map);

            res.setData(data);

        } catch (Exception e) {
            return new CommonResult(400, LocalUtil.get(KafukaTopics.FAIL));
        }
        return res;
    }

    /*
     * 基站应用版本升级
     * */
    @RequestMapping(value = "/upgradeSub")
    @RequiresPermissions("developer:view")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "num", value = "分站编号集", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "type", value = "升级类型(0应用程序 3UWB)", required = false, dataType = "int")
    })
    public CommonResult<Object> upgradeSub(String num, Integer type, MultipartFile file) {
        try {
            if (!NullUtils.isEmpty(file)) {
                //判断升级包的正确
                //    CommonResult<Object> commonResult= FileUtils.upgradeFile(file,uploadFolder+"/sub");
                //   if (commonResult.getCode()==200)
                {
                    /*String http="http://"+ip+":"+port;
                    String path =http+url+"/sub/"+commonResult.getData();*/
                 /*   String newName= uploadFolder+"/sub/"+commonResult.getData();
                    FileItem fileItem = FileUtils.createFileItem(newName);
                    MultipartFile encodeFile = new CommonsMultipartFile(fileItem);*/

                    StorePath encodestorePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                    //解析后的文件路径
                    String kafkaFile = encodestorePath.getFullPath();
                    subService.upgradeSub(type, num, fdfsUrl + kafkaFile);
                    return new CommonResult<>(200, LocalUtil.get(KafukaTopics.UPGRADE_SUCCESS));
                }/* else {
                    return commonResult;
                }*/
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
        }
        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.UPLOADFILE_ERROR));
    }

    /*
     * 基站调试
     * */
    @RequestMapping(value = "/debugSub")
    @ApiOperation(value = "标签调试内容接口", notes = "无")
    @RequiresPermissions("developer:view")
    @ApiImplicitParams({@ApiImplicitParam(paramType = "query", name = "bsid", value = "基站编号", required = true, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "keyOrder", value = "下发命令判断(beep-->蜂鸣器控制 backgroundurl-->背景图修改 word--》" +
                    "公司文字修改 warning-->继电器报警 power-->基站功率 locationword-->基站定位信息修改)", required = true, dataType = "String "),
            @ApiImplicitParam(paramType = "query", name = "beepInterval", value = "蜂鸣器鸣叫间隔", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "beepState", value = "蜂鸣器状态 0关闭 1打开", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "backgroundUrl", value = "背景图", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "word", value = "公司文字", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "warningState", value = "继电器报警", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "powerLevel)", value = "功率", required = false, dataType = "String")
    })
    public CommonResult<Object> debugSub(SubFirmware subFirmware, MultipartFile file) {
        try {
            if (!NullUtils.isEmpty(file) && !file.isEmpty()) {
                //限制图片的宽高 480*272
                String fileExtName = file.getOriginalFilename();
                if (!"jpg".equals(fileExtName) && !"png".equals(fileExtName)) {
                    return new CommonResult<>(400, "文件格式不正确");
                }
                BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
                if (bufferedImage != null) {
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    if (width > 480 || height > 272) {
                        return new CommonResult<>(400, LocalUtil.get(KafukaTopics.PICSIZE_ERROR));
                    }
                }
                //   CommonResult<Object> commonResult= FileUtils.uploadFile(uploadFolder,file);
                StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(), file.getSize(), FilenameUtils.getExtension(file.getOriginalFilename()), null);
                String fullPath = storePath.getFullPath();
                CommonResult<Object> commonResult = FileUtils.uploadFileName(uploadFolder, file, fullPath);
                if (commonResult.getCode() == 200) {
                    subFirmware.setBackgroundUrllocal(url + String.valueOf(commonResult.getData()));
                } else {
                    return commonResult;
                }
                //如果状态码不是200 直接return
                if (commonResult.getCode() != 200) {
                    return commonResult;
                }
                String http = fdfsUrl + storePath.getFullPath();
                //添加图片路径
                subFirmware.setBackgroundUrl(http);
            }
            subService.debugSub(subFirmware);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.OPERATION_SUCCESS));
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_ERROR));
        }
    }

}
