package com.tgy.rtls.data.service.equip.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.mapper.common.RecordMapper;
import com.tgy.rtls.data.mapper.equip.SubMapper;
import com.tgy.rtls.data.mapper.map.BsConfigMapper;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.equip.SubService;
import com.tgy.rtls.data.service.map.BsConfigService;
import com.tgy.rtls.data.service.video.impl.DeviceAlarmsService;
import com.tgy.rtls.data.websocket.WebSocketLocation;
import net.sf.json.JSONObject;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2020/10/19
 */
@Service
//@Transactional
public class SubServiceImpl implements SubService {
    private Logger logger = LoggerFactory.getLogger(TagServiceImpl.class);
    @Autowired(required = false)
    private SubMapper subMapper;
    @Autowired(required = false)
    private BsConfigMapper bsConfigMapper;
    @Autowired(required = false)
    private BsConfigService bsConfigService;
    @Autowired(required = false)
    private RecordMapper recordMapper;
    @Autowired(required = false)
    private WarnRecordMapper warnRecordMapper;
    @Autowired(required = false)
    private WebSocketLocation webSocketLocation;
    @Autowired
    private KafkaTemplate kafkaTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    LocalUtil localUtil;
    @Autowired
    private DeviceAlarmsService deviceAlarmsService;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    /**
     * 结束指定设备的所有未结束报警
     * @param deviceIds 设备ID
     * @param equipmentType 设备类型
     */
    @Override
    public void endAllAlarms(List<String> deviceIds, Integer equipmentType) {
        List<DeviceAlarms> alarmsList = deviceAlarmsService.lambdaQuery()
                .eq(!NullUtils.isEmpty(equipmentType),DeviceAlarms::getEquipmentType, equipmentType)
                .eq(DeviceAlarms::getState, 0)
                .in(DeviceAlarms::getDeviceId, deviceIds)
                .isNull(DeviceAlarms::getEndTime)
                .list();// 结束时间为空的记录

        // 更新记录
        LocalDateTime now = LocalDateTime.now();
        alarmsList.forEach(alarm -> {
            alarm.setState(1);
            alarm.setEndTime(now);
        });

        // 执行更新
        deviceAlarmsService.updateBatchById(alarmsList, 100);

        logger.debug("已结束设备{}的所有未结束报警", deviceIds);
    }

    @Override
    public int updateLifetimeByMap(Integer map, Integer lifetimeMonths,Integer type) {
        return subMapper.updateLifetimeByMap(map, lifetimeMonths,type);
    }

    @Override
    public void updateAddTime(Integer map, String num, LocalDateTime now) {
        subMapper.updateAddTime(map,num, now);
    }

    @Override
    public List<BsSyn> findByAll(String num, Integer type, Integer networkstate, Integer powerstate, Integer relevance, Integer map, Integer error, String desc, String start, String end, Integer instanceid) {
        return subMapper.findByAll(num,type,networkstate,powerstate,relevance,map,error,desc,start,end,instanceid,localUtil.getLocale());
    }

    @Override
    public List<BsSyn> findByAll2(String num, Integer type,String typeName, Integer networkstate, Integer powerstate,Integer power, Integer relevance, Integer map,Integer error, String desc, String start, String end, Integer instanceid,String floorName,String[] maps) {
        return subMapper.findByAll2(num,type,typeName,networkstate,powerstate,power,relevance,map,error,desc,start,end,instanceid,localUtil.getLocale(),floorName,maps);
    }

    @Override
    public List<DeviceVo> getSubAll() {
        return subMapper.getSubAll();
    }

    @Override
    public String findByNameId(String ids) {
        String[] split=ids.split(",");
        return subMapper.findByNameId(split);
    }

    @Override
    public Substation findById(Integer id) {
        return subMapper.findById(id,localUtil.getLocale());
    }

    @Override
//  @Cacheable(value = "subnum",key = "#num")
    public Substation findByNum(String num) {
        return subMapper.findByNum(num,localUtil.getLocale());
    }

    @Override
    @Cacheable(value = "subnum",key = "#num")
    public SubSyn findByMaxnum(String num) {
        return subMapper.findByMaxnum(num,localUtil.getLocale());
    }

    @Override
    @CacheEvict(value = "subnum",key = "#sub.num")
    public Boolean addSub(Substation sub,String http) {
        //新增分站时将分站参数也配置好
        if (subMapper.addSub(sub)>0){
            //添加分站背景图
            if (!NullUtils.isEmpty(sub.getBackground())){
                backgroundurl(http+sub.getBackground(),sub.getNum());
            }
            //添加公司文字
            if (!NullUtils.isEmpty(sub.getWord())){
                word(sub.getWord(),sub.getNum());
            }
            //添加基站网络配置
            if (!NullUtils.isEmpty(sub.getIpAddress())){
                net(sub.getIpType(),sub.getIpAddress(),sub.getSubnetMask(),sub.getNetworkSegment(),sub.getGatewayAddress(),sub.getNum());
            }
            //添加基站定位位置文字
            if (!NullUtils.isEmpty(sub.getLocationword())){
                locationword(sub.getLocationword(),sub.getNum());
            }
            //添加基站定位位置文字
           // bsConfigMapper.addBsConfig(sub.getId(),sub.getDisfix(),sub.getAntennadelay());
            //传输地图分站信息给前端
            if (!NullUtils.isEmpty(sub.getMap())){
                sendSubCount((sub.getMap()));
            }


            return true;
        }
        return false;
    }

    @Override
    public Boolean addDisparkSub(BsConfig bsConfig, String num) {
        Substation substation=new Substation();
        substation.setNum(num);
        if (subMapper.addSubNum(substation)>0){
            bsConfig.setBsid(substation.getId());
            bsConfigMapper.addDisparkBsConfig(bsConfig);
            return true;
        }
        return false;
    }

    @Override

    @Caching(
            evict = {
                    @CacheEvict(value = "subnum",key = "#sub.num"),
                    @CacheEvict(value = "bsconfignum",key = "#sub.num")
            }
    )
    public Boolean updateSub(Substation sub, String http) throws InterruptedException {
        if (subMapper.updateSub(sub)>0){

            //添加分站背景图

            //添加公司文字
            if (!NullUtils.isEmpty(sub.getWord())){
                word(sub.getWord(),sub.getNum());
            }
            Thread.sleep(200);
            //添加基站网络配置
         //   if (!NullUtils.isEmpty(sub.getIpAddress())){
                    //  }
            //添加基站定位位置文字
            if (!NullUtils.isEmpty(sub.getLocationword())){
                locationword(sub.getLocationword(),sub.getNum());
            }

            //传输地图分站信息给前端
            if (!NullUtils.isEmpty(sub.getMap())){
                sendSubCount((sub.getMap()));
            }
            //bsConfigMapper.updateSubBsConfig(sub.getId(),sub.getDisfix(),sub.getAntennadelay());
            Thread.sleep(1000);
            if (!NullUtils.isEmpty(sub.getBackground())){
                backgroundurl(http+sub.getBackground(),sub.getNum());
            }
            net(sub.getIpType(),sub.getIpAddress(),sub.getSubnetMask(),sub.getNetworkSegment(),sub.getGatewayAddress(),sub.getNum());
            //修改基站人数上限时触发分站超员报警判断
            if(!NullUtils.isEmpty(sub.getMaxnum())){
                JSONObject object=new JSONObject();
                object.put("map",sub.getMap());
                object.put("num",sub.getNum());
                kafkaTemplate.send("subWarn",object.toString());
            }
            return true;
        }
        return false;
    }

    @Override
    @CacheEvict(value = "bsConfigs*",allEntries = true)
    public Boolean delSub(String ids) {
        String[] split=ids.split(",");
        //删除分站参数信息
        bsConfigMapper.delBsConfigBsid(split);
        //删除分站进出记录信息
        recordMapper.delInsub(split);
        //删除分站相关的报警记录
        warnRecordMapper.delWarnRecordSub(split);
        HashSet idSet = new HashSet();
        HashSet nameSet = new HashSet();
        for (String id:split) {
            Substation bsinf = subMapper.findById(Integer.valueOf(id), localUtil.getLocale());
            idSet.add(bsinf.getMap());
            nameSet.add(bsinf.getNum());
        }




        //Integer map=bsinf.getMap();
        if (subMapper.delSub(split)>0){
            for (Object num:idSet){
                sendSubCount((((String)num)));
            }
            for (Object num:nameSet){
                deleteSub((String) num);
                logger.info("num========"+num.toString());
                redisService.remove("bsconfignum::" + (String) num);
            }
            return true;
        }

        return false;
    }

    @Override
    @CacheEvict(value = "bsConfigs*",allEntries = true)
    public void updateSubNetworkstate(String num, Integer state) {
        subMapper.updateSubNetworkstate(num, state);
    }

    @Override
    @CacheEvict(value = "subnum",key = "#num")
    public void updateSubPowerstate(String num, Integer state) {
        subMapper.updateSubPowerstate(num,state);
    }

    @Override
    @CacheEvict(value = "subnum",key = "#num")
    public void updateSubBattery(String num, String batteryVolt, String batteryTime,Short power) {
        subMapper.updateSubBattery(num,batteryVolt,batteryTime,power);
    }

    //@Override
    //@CacheEvict(value = "subnum",key = "#num")
    //public void updateSubIp(String num, String ip) {
    //    subMapper.updateSubIp(num,ip);
    //}

    @Override
    public int findBySubCount(Integer map) {
        return subMapper.findBySubCount(map);
    }

    @Override
    public void upgradeSub(Integer type, String num, String path) {
        String[] split=num.split(",");
        for (String s:split){
            JSONObject object=new JSONObject();
            object.put("fileType",type);//0 ARM板固件 1 音频文件 3 UWB模块固件
            object.put("bsid",s);
            object.put("target",0);
            object.put("direction",1);
            object.put("url",path);
            kafkaTemplate.send(KafukaTopics.File_REQ,object.toString());
        }
    }

    @Override
    public void debugSub(SubFirmware subFirmware) {
        //根据不同keyOrder走不同分支
        //1蜂鸣器控制
        if ("beep".equals(subFirmware.getKeyOrder())){
            beep(subFirmware.getBeepInterval(),subFirmware.getBeepState(),subFirmware.getBsid());
        }else if("backgroundurl".equals(subFirmware.getKeyOrder())) {//2.背景图片修改
            backgroundurl(subFirmware.getBackgroundUrl(),subFirmware.getBsid());
        }else if ("word".equals(subFirmware.getKeyOrder())){//3.公司文字修改
            word(subFirmware.getWord(),subFirmware.getBsid());
        }else if ("warning".equals(subFirmware.getKeyOrder())){//4.基站继电器
            warning(subFirmware.getWarningState(),subFirmware.getBsid());
        }else if ("power".equals(subFirmware.getKeyOrder())){//5.基站功率
            power(subFirmware.getPowerLevel(), subFirmware.getBsid());
        }else if ("locationword".equals(subFirmware.getKeyOrder())){//6.基站位置信息
            locationword(subFirmware.getLocationword(),subFirmware.getBsid());
        }
    }

    @Override
    @CacheEvict(value = "subnum",key = "#num")
    public void deleteSub(String num) {

    }

    @Override
    public void updateSubError(String num, Integer error) {
        subMapper.updateSubError(num,error);
    }

    @Override
    public void delSubInstance(Integer instanceid) {
        subMapper.delSubInstance(instanceid);
    }

    @Override

    public List<BsSyn> findBeaconByMap(Integer map) {
        return subMapper.findBeaconByMap(map);
    }

    @Override
    public Substation getCurrentSubMapName(String num,Integer map) {
        return subMapper.getCurrentSubMapName(num,map);
    }

    @Override
    public List<DeviceVo> getSubMoreThan30Days(String map) {
        return subMapper.getSubMoreThan30Days(map);
    }

    @Override
    public List<DeviceVo> getSubMoreThan60Days(String map) {
        return subMapper.getSubMoreThan60Days(map);
    }
    @Override
    public List<DeviceVo> substationBatteryTimeWarningLevelsQuery() {
        return subMapper.substationBatteryTimeWarningLevelsQuery();
    }

    @Override
    public void updateSubForOffline(Integer id) {
        subMapper.updateSubForOffline(id);
    }

    @Override
    public void updateSubLessThanBatterySub(Integer id) {
        subMapper.updateSubLessThanBatterySub(id);
    }

    @Override
    public List<Substation> timeNotUpdateBetweenFetche(String map) {
        return subMapper.timeNotUpdateBetweenFetche(map);
    }

    @Override
    public String importSubLocationExcel(String fileName,MultipartFile file) throws Exception{
        //String res = "未添加任何数据";
        boolean notNull = false;
        List<Substation> subList = new ArrayList<Substation>();
        List<BsConfig> bsConfigList = new ArrayList<BsConfig>();
        if (!fileName.matches("^.+\\.(?i)(xls)$") && !fileName.matches("^.+\\.(?i)(xlsx)$")) {
            return  "上传文件格式不正确";
        }
        boolean isExcel2003 = true;
        if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
            isExcel2003 = false;
        }
        InputStream is = file.getInputStream();
        Workbook wb = null;
        /**
         * Excel2003以前的版本，扩展名是.xls,使用HSSFWorkbook()
         * Excel2007之后的版本，扩展名是.xlsx,使用XSSFWorkbook()
         */
        if (isExcel2003) {
            wb = new HSSFWorkbook(is);
        } else {
            wb = new XSSFWorkbook(is);
        }
        //工作表对象
        Sheet sheet = wb.getSheetAt(0);
        //总行数
        int rowLength = sheet.getLastRowNum();
        logger.info("总行数有多少行" + rowLength);
        //工作表的列
        Row row = sheet.getRow(0);
        //总列数
        int colLength = row.getLastCellNum();
        if(sheet!=null){
            notNull = true;
        }
        Substation substation = new Substation();
        BsConfig bsConfig = new BsConfig();
        // 循环行数
        for (int r = 1; r <= rowLength ; r++) {
            row = sheet.getRow(r);
            // 获取sheet的第r行的数据
            if (row == null) {
                continue;
            }
            row.getCell(0).setCellType(CellType.STRING);
            row.getCell(1).setCellType(CellType.STRING);
            row.getCell(2).setCellType(CellType.STRING);
            row.getCell(3).setCellType(CellType.STRING);
            row.getCell(4).setCellType(CellType.STRING);
            row.getCell(5).setCellType(CellType.STRING);
            String num = row.getCell(0).getStringCellValue();
            String mapId = row.getCell(1).getStringCellValue();
            String x = row.getCell(2).getStringCellValue();
            String y = row.getCell(3).getStringCellValue();
            String z = row.getCell(4).getStringCellValue();
            String floor = row.getCell(5).getStringCellValue();

            if(num == null || num.isEmpty()){
                return "导入失败(第"+(r+1)+"行,编号未填写)";
            }
            if(mapId == null || mapId.isEmpty()){
                return "导入失败(第"+(r+1)+"行,地图id未填写)";
            }

            if(x == null || x.isEmpty()){
                return "导入失败(第"+(r+1)+"行,X轴未填写)";
            }

            if(y == null || y.isEmpty()){
                return "导入失败(第"+(r+1)+"行,Y轴未填写)";
            }

            if(z == null || z.isEmpty()){
                return "导入失败(第"+(r+1)+"行,Z轴未填写)";
            }

            if(floor == null || floor.isEmpty()){
                return "导入失败(第"+(r+1)+"行,楼层未填写)";
            }
            substation = new Substation();
            bsConfig = new BsConfig();
            substation.setNum(num);
            substation.setMap(mapId);
            substation.setAddTime(LocalDateTime.now());
            substation.setX(Double.valueOf(x));
            substation.setY(Double.valueOf(y));
            substation.setZ(Double.valueOf(z));
            substation.setFloor(Short.valueOf(floor));
            subList.add(substation);
        }

        for (Substation sub : subList) {
            //首先判断该姓名是否已经存在
            String Num = sub.getNum();
            List<Substation> list = subMapper.getBsconfig(Num, null);
            if(list.size()==0) {
                subMapper.addSub1(sub);
                bsConfig.setBsid(sub.getId());
                bsConfig.setX(sub.getX());
                bsConfig.setY(sub.getY());
                bsConfig.setZ(sub.getZ());
                bsConfig.setFloor(sub.getFloor());
                subMapper.addBsconfig(bsConfig);
            }
            else {//如果有先删除，再添加
                //有数据先更新主表然后根据主表id删除字表的所有数据，然后循环插入字表数据
                //没有数据插入然后拿到主表id去插入字表
                subMapper.updateSub2(sub);
                subMapper.delBsconfig(list.get(0).getBsid());
                subMapper.delSub1(list.get(0).getId());
                subMapper.addSub1(sub);
                bsConfig.setBsid(substation.getId());
                bsConfig.setBsid(sub.getId());
                bsConfig.setX(sub.getX());
                bsConfig.setY(sub.getY());
                bsConfig.setZ(sub.getZ());
                bsConfig.setFloor(sub.getFloor());
                subMapper.addBsconfig(bsConfig);
            }
        }


        return null;
    }

    @Override
    public void exportSubLocationExcel(String mapId, HttpServletResponse response) throws Exception {
        //创建HSSFWorkbook对象,  excel的文档对象
        HSSFWorkbook workbook = new HSSFWorkbook();
        //excel的表单
        HSSFSheet sheet = workbook.createSheet("信标设备信息表");

        List<Substation> substationList = subMapper.getBsconfig(null,mapId);
        //设置要导出的文件的名字
        String fileName = substationList.get(0).getMapName()+"信标设备信息"  + ".xlsx";
        //新增数据行，并且设置单元格数据
        int rowNum = 1;
        //标题集合
        String[] headers = {"类型","设备编号","关联地图","楼层","网络状态","电量","电池电压","电压检测时间","创建时间","更新时间"};
        //headers表示excel表中第一行的表头
        HSSFRow row = sheet.createRow(0);
        //在excel表中添加表头
        for(int i=0;i<headers.length;i++){
            HSSFCell cell = row.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(headers[i]);
            cell.setCellValue(text);
        }

        //在表中存放查询到的数据放入对应的列
        for (Substation sub : substationList) {
            HSSFRow row1 = sheet.createRow(rowNum);
            row1.createCell(0).setCellValue(!NullUtils.isEmpty(sub.getTypeName()) ? sub.getTypeName() : "");
            row1.createCell(1).setCellValue(!NullUtils.isEmpty(sub.getNum()) ? sub.getNum() : "");
            row1.createCell(2).setCellValue(!NullUtils.isEmpty(sub.getMapName()) ? sub.getMapName() : "");
            row1.createCell(3).setCellValue(!NullUtils.isEmpty(sub.getFloorName()) ? sub.getFloorName() : "");
            row1.createCell(4).setCellValue(!NullUtils.isEmpty(sub.getNetworkName()) ? sub.getNetworkName() : "");

            row1.createCell(5).setCellValue(!NullUtils.isEmpty(sub.getPower1()) ? sub.getPower1().toString() : "");

            row1.createCell(6).setCellValue(!NullUtils.isEmpty(sub.getBatteryVolt()) ? sub.getBatteryVolt() : "");
            row1.createCell(7).setCellValue(!NullUtils.isEmpty(sub.getBatteryTime1()) ? sub.getBatteryTime1().format(formatter) : "");
            row1.createCell(8).setCellValue(!NullUtils.isEmpty(sub.getAddTime1()) ? sub.getAddTime1().format(formatter) : "");
            row1.createCell(9).setCellValue(!NullUtils.isEmpty(sub.getUpdateTime1()) ? sub.getUpdateTime1().format(formatter) : "");

            rowNum++;
        }
        response.reset(); // 非常重要
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",String.format("attachment; filename=\"%s\"",
                URLEncoder.encode(fileName, "UTF-8")));
        response.setContentType("application/vnd.ms-excel;charset=UTF-8");
        response.flushBuffer();
        workbook.write(response.getOutputStream());
    }

    //传输地图分站信息给前端
    private void sendSubCount(String map){
        JSONObject objectdata = new JSONObject();
        objectdata.put("counttype",4);
        objectdata.put("count",subMapper.findBySubCount(map.trim().isEmpty()?null: Integer.valueOf(map)));
        JSONObject objectCount = new JSONObject();
        objectCount.put("data", objectdata);
        objectCount.put("type", 3);
        objectCount.put("map",map);
        webSocketLocation.sendAll(objectCount.toString());
    }

    //蜂鸣器控制 beepState 状态 0关闭 1打开
    private void beep(Integer beepInterval,Integer beepState,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","beep");
        object.put("beepInterval",beepInterval);//蜂鸣器鸣叫间隔
        object.put("beepState",beepState);//蜂鸣器状态 0关闭 1打开
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送蜂鸣器控制命令："+object);
    }
    //基站背景图修改
    private void backgroundurl(String backgroundUrl,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","backgroundurl");
        object.put("backgroundUrl",backgroundUrl);//背景图
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送背景图修改命令："+object);
    }
    //公司文字修改
    private void word(String word,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","word");
        object.put("word",word);//公司文字
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送公司文字修改命令："+object);
    }

    //基站位置信息
    private void locationword(String locationword,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","locationword");
        object.put("word",locationword);//公司文字
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送基站位置信息修改命令："+object);
    }
    //基站继电器
    private void warning(Integer waringState,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","warning");
        object.put("waringState",waringState);//0：关 1：开
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送继电器开启命令："+object);
    }

    //基站功率
    private void power(String powerLevel, String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","power");
        object.put("type", 1);//0：读功率 1：写功率 当type为0时，powerLevel可为空
        object.put("powerLevel",powerLevel);//功率
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送功率修改命令："+object);
    }
    //基站网络配置
    private void net(int ip_type,String address ,String netmask,String network,String gateway,String bsid){
        JSONObject object=new JSONObject();
        object.put("keyOrder","net");
        object.put("ip_type",ip_type);
        object.put("address", address);//ip地址
        object.put("netmask",netmask);//子网掩码
        object.put("network",network);//网段
        object.put("gateway",gateway);//网关地址
        object.put("bsid",bsid);
        kafkaTemplate.send(KafukaTopics.BS_CONTROLREQ,object.toString());
        logger.info(bsid+"基站发送网络配置命令："+object);
    }
}
