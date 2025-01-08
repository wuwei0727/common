package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.algorithm.PercentToPosition;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.config.SpringContextHolder2;
import com.tgy.rtls.data.entity.Camera.CameraConfig;
import com.tgy.rtls.data.entity.Camera.CameraConfigResponse;
import com.tgy.rtls.data.entity.Camera.CameraPlace;
import com.tgy.rtls.data.entity.Camera.CameraVehicleCapture;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.excel.ExcelDataVo;
import com.tgy.rtls.data.entity.map.MapBuildCommon;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.park.*;
import com.tgy.rtls.data.enums.IconType;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.map.Map2dMapper;
import com.tgy.rtls.data.mapper.park.BookMapper;
import com.tgy.rtls.data.mapper.park.ParkMapper;
import com.tgy.rtls.data.mapper.park.ParkingRecordMapper;
import com.tgy.rtls.data.service.Camera.CameraConfigService;
import com.tgy.rtls.data.service.Camera.impl.CameraVehicleCaptureService;
import com.tgy.rtls.data.service.map.MapBuildCommonService;
import com.tgy.rtls.data.service.park.ParkingElevatorBindingService;
import com.tgy.rtls.data.service.park.ParkingService;
import com.tgy.rtls.data.tool.Gps_xy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.mybatis.spring.SqlSessionTemplate;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParkingServiceImpl implements ParkingService {

    @Autowired(required = false)
    private ParkMapper parkMapper;
    @Autowired(required = false)
    Map2dMapper map2dMapper;
    @Autowired(required = false)
    ParkingRecordMapper parkingRecordMapper;
    @Autowired(required = false)
    private LocalUtil localUtil;
    @Autowired
    private ParkingElevatorBindingService parkingElevatorBindingService;
    @Autowired
    private CrossLevelCorridorService crossLevelCorridorService;
    @Autowired
    private MapBuildCommonService mapBuildCommonService;
    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;
    @Autowired(required = false)
    BookMapper bookMapper;
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private CameraConfigService cameraConfigService;
    @Autowired
    private CameraVehicleCaptureService cameraVehicleCaptureService;
    @Autowired
    private CameraPlaceService cameraPlaceService;

    @Value("${radius:30}")
    private double radius;
    @Value("${intervalValue:3}")
    private Integer intervalValue;

    @Value("${web.lang}")
    private String lang;
    private static final String ORDINARY_CAR_BIT_ID = "200401";//普通车位
    private static final String EXCLUSIVE_CAR_BIT_ID = "340862";//专属车位
    private static final String CHARGING_PILE_CAR_BIT_ID = "340860";//充电桩车位
    private static final String BARRIER_FREE_CAR_BIT_ID = "340859";//无障碍车位
    private static final String ULTRA_WIDE_CAR_BIT_ID = "340861";//超宽车位
    private static final String CHILD_AND_MOTHER_CAR_BIT = "340863";//子母车位
    private static final String SMALL_CAR_BIT = "340864";//小型车位
    private static final String AREA_A_CAR_BIT = "340878";//A区车位
    private static final String AREA_B_CAR_BIT = "340879";//B区车位
    private static final String CROSS_FLOOR_ID = "340891";//跨层通道
    private static final String EXIT_ID = "200001";//出入口
    private static final String ENTRANCE_ID = "340889";//入口
    private static final String CHUKOU_ID = "340890";//出口
    private static final String BASEMENT_CHURUKOU_ID = "340892";//地库出入口

    @Override
    public List<ParkingCompany> findByAllCompany(Integer id, String name, Integer map, String instanceid, Integer pageIndex, Integer pageSize) {
        return parkMapper.findByAllCompany(id, name, map, instanceid, pageIndex, pageSize);
    }

    @Override
    @Transactional
    public List<ParkingCompany> findByAllCompany2(Integer id, String name, Integer map, String instanceid, Integer pageIndex, Integer pageSize, String floorName, String[] maps) {
        return parkMapper.findByAllCompany2(id, name, map, instanceid, pageIndex, pageSize,floorName, maps);
    }

    @Override
    public Map_2d findByCompanyMap(Integer companyId) {
        return parkMapper.findByCompanyMap(companyId);
    }

    @Override
    public List<ParkingPlace> findByAllPlace(Integer id, String name, String companyName, Integer mapid, String license, Short state, Integer companyid, String floor, Short charge, Short type, String instanceid, String fid, Integer pageIndex, Integer pageSize, String phone) {
        return parkMapper.findByAllPlace(id, name, companyName, mapid, license, state, companyid, floor, charge, type, instanceid, fid, pageIndex, pageSize, phone);
    }

    @Override
    public List<ParkingPlace> findByAllPlace2(Integer id, String name, String companyName, Integer mapid, String license, String carbitType, Short state, Integer companyid, String floor, Short charge, Short type, String instanceid, String fid, String configWay, Integer pageIndex, Integer pageSize, String desc, String floorName, String isReservable, String[] maps) {
        return parkMapper.findByAllPlace2(id, name, companyName, mapid, license,carbitType, state, companyid, floor, charge, type, instanceid, fid,configWay, pageIndex, pageSize, desc, floorName,isReservable,maps);
    }

    @Override
    public List<ParkingPlace> findByAllCompanyName(Integer id, String name, String companyName, Integer mapid, String license, Short state, Integer companyid, String floor, Short charge, Short type, String instanceid, String fid, Integer pageIndex, Integer pageSize) {
        return parkMapper.findByAllCompanyName(id, name, companyName, mapid, license, state, companyid, floor, charge, type, instanceid, fid, pageIndex, pageSize);
    }

    /*
     * 根据关键字搜索车位
     * */
    @Override
    public List<ParkingPlace> wechatFindByAllCompanyName(String companyName,
                                                         Integer mapid,
                                                         String company,
                                                         String instanceid,
                                                         String floor
    ) {
        return parkMapper.wechatFindByAllCompanyName(companyName, mapid, instanceid, company,
                floor);
    }

    @Override
    public Integer addCompany(ParkingCompany parkingCompany) {
        return parkMapper.addCompany(parkingCompany);
    }

    @Override
    public void updateCompany(ParkingCompany parkingCompany) {
        parkMapper.updateCompany(parkingCompany);
    }

    @Override
    public void deleteCompany(String[] ids1) {
        for (String id : ids1) {
            parkMapper.updatePlaces(null,null, Integer.valueOf(id));
        }

        parkMapper.deleteCompany(ids1);
    }

    @Override
    public Integer addPlace(ParkingPlace parkingPlace) {
        return parkMapper.addPlace(parkingPlace);
    }

    @Override
    public void updatePlace(ParkingPlace parkingPlace) {
        parkMapper.updatePlace(parkingPlace);
    }

    @Override
    public void updatePlaceCompany(ParkingPlace parkingPlace) {
        parkMapper.updatePlaceCompany(parkingPlace);
    }

    @Override
    public void deletePlace(String[] ids) {
        parkMapper.deletePlace(ids);
    }

    @Override
    public void updatePlaces(String[] placeids, Integer newCompanyid, Integer oldCompanyid) {
        parkMapper.updatePlaces(placeids, newCompanyid, oldCompanyid);
    }

    @Override
    public List findFloorByMapid(Integer map) {
        return parkMapper.findFloorByMapid(map);
    }

    @Override
    public List<WeiTing> findByAllWeiTing(Integer id, String license, String start, String end, Integer map, Integer state, String instanceid, Integer pageIndex, Integer pageSize) {
        return parkMapper.findByAllWeiTing(id, license, start, end, map, state, instanceid, pageIndex, pageSize);
    }

    @Override
    public List<WeiTing> findByAllWeiTing2(Integer id, String license, String start, String end, Integer map, Integer state, String instanceid, Integer pageIndex, Integer pageSize, String[] maps) {
        return parkMapper.findByAllWeiTing2(id, license, start, end, map, state, instanceid, pageIndex, pageSize, maps);
    }

    @Override
    public List<ShangJia> findByAllShangjia(Integer id, Integer map, Integer type, String name, Integer instanceid, Integer pageIndex, Integer pageSize) {
        return parkMapper.findByAllShangjia(id, map, type, name, instanceid, pageIndex, pageSize);
    }

    @Override
    public List<ShangJia> findByAllShangjia2(Integer id, Integer map, Integer type, String name, Integer instanceid, Integer pageIndex, Integer pageSize, String floorName, String[] maps,String fid) {
        return parkMapper.findByAllShangjia2(id, map, type, name, instanceid, pageIndex, pageSize,floorName, maps,fid);
    }

    @Override
    public List<ShangJiaType> findByAllShangjiaType(Integer id, Integer instanceid) {
        return parkMapper.findByAllShangjiaType(id, instanceid);
    }

    @Override
    public List<ParkingPlace> findPlaceByMapAndName(Integer map, String name, String fid) {
        return parkMapper.findPlaceByMapAndName(map, name, fid);
    }

    @Override
    public Integer addShangjia(ShangJia shangJia) {
        return parkMapper.addShangjia(shangJia);
    }

    @Override
    public void updateShangjia(ShangJia shangJia) {
        parkMapper.updateShangjia(shangJia);
    }

    @Override
    public void delShangjia(String[] ids) {
        parkMapper.delShangjia(ids);
    }

    @Override
    @Async(value = "SubAsyncExecutor")
    public CompletableFuture<List<MapPlace>> findPlaceCountGroupByMap(Integer enable, Integer place_type, Integer mapId, Integer hasVIP) {
        return CompletableFuture.completedFuture(parkMapper.findPlaceCountGroupByMap(enable, place_type, mapId, hasVIP));
    }

    @Override
    @Async(value = "SubAsyncExecutor")
    public CompletableFuture<List<CompanyPlace>> findPlaceCountByMap(Integer map, Integer enable, String name, String status, Integer place_type) {
        return CompletableFuture.completedFuture(parkMapper.findPlaceCountByCompany(map, enable, name, status, place_type));
    }

    @Override
    public String importLabelFromExcel(MultipartFile excelFile, String fmapID) throws Exception {
        String res = "未添加任何数据";
        HSSFWorkbook book = null;
        Map_2d map_2d = map2dMapper.findByfmapID(fmapID);
        Integer mapid = null;
        if (map_2d != null) {
            mapid = map_2d.getId();
        }
        try {
            //使用poi解析Excel文件
            book = new HSSFWorkbook(excelFile.getInputStream());
            int sheets = book.getNumberOfSheets();

            int addParkNum = 0, updatePark = 0, addCrossFloor = 0, updateCrossFloor = 0, addExit = 0, updateExit = 0,
            addExclusiveCar = 0,addChargingPileCar = 0, addBarrierFreeCar = 0,addUltraWideCar = 0,addChildAndMotherCar = 0,addSmallCar = 0,
            updateExclusiveCar = 0,updateChargingPileCar = 0, updateBarrierFreeCar = 0,updateUltraWideCar = 0,updateChildAndMotherCar = 0,updateSmallCar = 0;

            for (int m = 0; m < sheets; m++) {
                //根据名称获得指定Sheet对象
                HSSFSheet hssfSheet = book.getSheetAt(m);

                if (hssfSheet != null) {
                    List<HSSFPictureData> pictures = book.getAllPictures();
                    Map<Integer, HSSFPictureData> picDataMap = new HashMap<>();
                    if (hssfSheet.getDrawingPatriarch() != null) {
                        for (HSSFShape shape : hssfSheet.getDrawingPatriarch().getChildren()) {
                            HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                            if (shape instanceof HSSFPicture) {
                                HSSFPicture pic = (HSSFPicture) shape;
                                int row = anchor.getRow1();
                                int pictureIndex = pic.getPictureIndex() - 1;
                                try {
                                    HSSFPictureData picData = pictures.get(pictureIndex);
                                    picDataMap.put(row, picData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    int rows = hssfSheet.getPhysicalNumberOfRows();
                    if (rows < 2) {
                        return "格式不对";
                    }
                    String[] titles = {"sid", "类型id", "高度", "中文名称", "英文名称", "x坐标", "y坐标"};
                    HSSFRow headerRow = hssfSheet.getRow(0);
                    int cells = headerRow.getPhysicalNumberOfCells();
                    List<String> cellList = new LinkedList<>();
                    for (int i = 0; i < cells; i++) {
                        String cellName = headerRow.getCell(i).getStringCellValue();
                        cellList.add(cellName);
                    }
                    if (cellList.size() < titles.length) {
                        throw new ImportUsersException("导入数据模板格式错误");
                    }
                    for (int i = 1; i < rows; i++) {
                        HSSFRow row = hssfSheet.getRow(i);
                        if (row != null) {
                            for (int j = 0; j < cells; j++) {
                                if (row.getCell(j) != null) {
                                    row.getCell(j).setCellType(CellType.STRING);
                                }
                            }
                            String[] val = new String[12];
                            for (int j = 0; j < cells; j++) {
                                HSSFCell cell = row.getCell(j);
                                if (cell != null) {
                                    switch (cell.getCellTypeEnum()) {
                                        case FORMULA:
                                            break;
                                        case NUMERIC:
                                            val[j] = String.valueOf(cell.getNumericCellValue());
                                            break;
                                        case STRING:
                                            val[j] = cell.getStringCellValue();
                                            break;
                                        default:
                                            val[j] = "";
                                    }
                                }
                            }
                            try {
                                if (val[0].isEmpty() || val[3].isEmpty()) {
                                    continue;
                                }
                                ParkingPlace place = new ParkingPlace();
                                place.setMap(mapid);
                                place.setState((short) 3);
                                if (val == null || val[1] == null || val[1].trim().isEmpty() || val[3].trim().isEmpty()) {
                                    continue;
                                }

                                //判断卡号重名 ---车位
                                if (!NullUtils.isEmpty(val[0]) &&
                                        (ORDINARY_CAR_BIT_ID.equals(val[1].trim()) ||
                                                EXCLUSIVE_CAR_BIT_ID.equals(val[1].trim()) || CHARGING_PILE_CAR_BIT_ID.equals(val[1].trim()) ||
                                                BARRIER_FREE_CAR_BIT_ID.equals(val[1].trim()) || ULTRA_WIDE_CAR_BIT_ID.equals(val[1].trim()) ||
                                                CHILD_AND_MOTHER_CAR_BIT.equals(val[1].trim()) || SMALL_CAR_BIT.equals(val[1].trim())) ||
                                                AREA_A_CAR_BIT.equals(val[1].trim()) || AREA_B_CAR_BIT.equals(val[1].trim())
                                ) {
                                    //List<ParkingPlace> parking_place = parkMapper.findPlace(ByteUtils.replaceBlank(val[0]), ByteUtils.replaceBlank(val[3]));
                                    try {
                                        //ParkingPlace placeByFid = parkMapper.getPlaceByFid(val[0].trim());
                                        //根据车位名称查车位
                                        ParkingPlace placeByPlaceNames = parkMapper.getPlaceByPlaceNames(mapid, val[3].trim(), null,null);
                                        ParkingPlace parkingPlace = new ParkingPlace();
                                        if (!NullUtils.isEmpty(placeByPlaceNames)) {
                                            if (!NullUtils.isEmpty(placeByPlaceNames) && placeByPlaceNames.getName().equals(val[3].trim())) {
                                                if (!NullUtils.isEmpty(placeByPlaceNames)) {
                                                    parkingPlace.setId(placeByPlaceNames.getId());
                                                    parkingPlace.setFid(val[0].trim());
                                                    parkingPlace.setX(val[5].trim());
                                                    parkingPlace.setY(val[6].trim());
                                                    parkingPlace.setMap(mapid);
                                                    parkingPlace.setState((short) 3);
                                                    if (val[4] != null && !val[4].trim().isEmpty()) {
                                                        parkingPlace.setFloor(val[4].trim());
                                                    }
                                                    switch (val[1].trim()) {
                                                        case EXCLUSIVE_CAR_BIT_ID:
                                                            parkingPlace.setType((short) 2);
                                                            updateExclusiveCar++;
                                                            break;
                                                        case CHARGING_PILE_CAR_BIT_ID:
                                                            parkingPlace.setType((short) 1);
                                                            updateChargingPileCar++;
                                                            break;
                                                        case BARRIER_FREE_CAR_BIT_ID:
                                                            parkingPlace.setType((short) 3);
                                                            updateBarrierFreeCar++;
                                                            break;
                                                        case ULTRA_WIDE_CAR_BIT_ID:
                                                            parkingPlace.setType((short) 4);
                                                            updateUltraWideCar++;
                                                            break;
                                                        case CHILD_AND_MOTHER_CAR_BIT:
                                                            parkingPlace.setType((short) 5);
                                                            updateChildAndMotherCar++;
                                                            break;
                                                        case SMALL_CAR_BIT:
                                                            parkingPlace.setType((short) 6);
                                                            updateSmallCar++;
                                                            break;
                                                        default:
                                                            parkingPlace.setType((short) 0);
                                                            place.setCarbittype("0");
                                                    }
                                                    parkMapper.updatePlace(parkingPlace);
                                                    updatePark++;
                                                    continue;
                                                }
                                            }
                                        } else {
                                            place.setFid(val[0].trim());
                                            place.setName(val[3].trim());
                                            place.setX(val[5].trim());
                                            place.setY(val[6].trim());
                                            if (val[4] != null && !val[4].trim().isEmpty()) {
                                                place.setFloor(val[4].trim());
                                            }
                                            switch (val[1].trim()) {
                                                case EXCLUSIVE_CAR_BIT_ID:
                                                    place.setType((short) 2);
                                                    addExclusiveCar++;
                                                    break;
                                                case CHARGING_PILE_CAR_BIT_ID:
                                                    place.setType((short) 1);
                                                    addChargingPileCar++;
                                                    break;
                                                case BARRIER_FREE_CAR_BIT_ID:
                                                    place.setType((short) 3);
                                                    addBarrierFreeCar++;
                                                    break;
                                                case ULTRA_WIDE_CAR_BIT_ID:
                                                    place.setType((short) 4);
                                                    addUltraWideCar++;
                                                    break;
                                                case CHILD_AND_MOTHER_CAR_BIT:
                                                    place.setType((short) 5);
                                                    addChildAndMotherCar++;
                                                    break;
                                                case SMALL_CAR_BIT:
                                                    place.setType((short) 6);
                                                    addSmallCar++;
                                                    break;
                                                default:
                                                    place.setType((short) 0);
                                                    place.setCarbittype("0");
                                            }
                                            if (parkMapper.addPlace(place) > 0) {
                                                addParkNum++;
                                            }
                                        }
                                    } catch (Exception e) {
                                        continue;
                                    }
                                } else if (!NullUtils.isEmpty(val[0]) && CROSS_FLOOR_ID.equals(val[1].trim())) {
                                    CrossLevelCorridor crossFloor = parkMapper.findCrossFloor(val[0], mapid, val[3]);
                                    try {
                                        if (!NullUtils.isEmpty(crossFloor)) {
                                            crossFloor.setId(crossFloor.getId());
                                            crossFloor.setFid(val[0].trim());
                                            crossFloor.setX(val[5].trim());
                                            crossFloor.setFloor(val[4].trim());
                                            crossFloor.setY(val[6].trim());
                                            crossLevelCorridorService.updateByPrimaryKeySelective(crossFloor);
                                            updateCrossFloor++;
                                            continue;
                                        } else {
                                            crossFloor = new CrossLevelCorridor();
                                            crossFloor.setFid(val[0].trim());
                                            crossFloor.setName(val[3].trim());
                                            crossFloor.setX(val[5].trim());
                                            crossFloor.setFloor(val[4].trim());
                                            crossFloor.setY(val[6].trim());
                                            crossFloor.setMap(mapid);
                                            if (crossLevelCorridorService.insertSelective(crossFloor) > 0) {
                                                addCrossFloor++;
                                            }
                                        }
                                    } catch (Exception e) {
                                        continue;
                                    }
                                    //---出入口
                                } else if (!NullUtils.isEmpty(val[0]) &&
                                        (EXIT_ID.equals(val[1].trim()) || ENTRANCE_ID.equals(val[1].trim()) ||
                                        CHUKOU_ID.equals(val[1].trim()) || BASEMENT_CHURUKOU_ID.equals(val[1].trim())) && (val[3].contains("口") || val[3].contains("地库"))) {
                                    ParkingExit exit = new ParkingExit();
                                    exit.setMap(mapid);
                                    //判断卡号重名
                                    List<ParkingExit> exit1s = parkMapper.getExit(mapid, val[3].trim(),val[0].trim());
                                    try {
                                        Short type_default = 2;
                                        if (val[3].contains("出口")) {
                                            type_default = 1;
                                        } else if (val[3].contains("出入口")&&!val[3].contains("地库")) {
                                            type_default = 2;
                                        }else if (val[3].contains("地库出入口")) {
                                            type_default = 3;
                                        } else {
                                            type_default = 0;
                                        }
                                        if (!NullUtils.isEmpty(exit1s) && exit1s.size() == 1) {
                                            ParkingExit exit1 = exit1s.get(0);
                                            exit1.setId(exit1.getId());
                                            exit1.setFid(val[0].trim());
                                            exit1.setX(val[5].trim());
                                            exit1.setY(val[6].trim());
                                            exit1.setFloor(val[4].trim());
                                            exit1.setType(type_default);
                                            exit1.setAccessStatus(1);
                                            parkMapper.updateExit(exit1);
                                            updateExit++;
                                            continue;
                                        } else {
                                            exit.setFid(val[0].trim());
                                            exit.setName(val[3].trim());
                                            exit.setX(val[5].trim());
                                            exit.setY(val[6].trim());
                                            exit.setType(type_default);
                                            exit.setFloor(val[4].trim());
                                            exit.setAccessStatus(1);
                                            if (parkMapper.addExit(exit) > 0) {
                                                addExit++;
                                            }
                                        }
                                    } catch (Exception e) {
                                        continue;
                                    }

                                } else if(!NullUtils.isEmpty(val[0]) && (IconType.STAIRS.getCode().equals(val[1].trim()) || IconType.ESCALATOR.getCode().equals(val[1].trim()) ||
                                        IconType.ELEVATOR_LOBBY.getCode().equals(val[1].trim()) || IconType.ELEVATOR.getCode().equals(val[1].trim()))){
                                    ParkingElevatorBinding peb = new ParkingElevatorBinding();
                                    peb.setMap(mapid);
                                    List<ParkingElevatorBinding> pedList = parkingElevatorBindingService.getByConditions(val[3].trim(), mapid, null, null, null, null, null, null, null,val[0].trim());
                                    if (!NullUtils.isEmpty(pedList) && pedList.size() == 1) {
                                        ParkingElevatorBinding peb1 = pedList.get(0);
                                        peb.setId(peb1.getId());
                                        peb.setName(val[3].trim());
                                        peb.setFid(val[0].trim());
                                        peb.setX(val[5].trim());
                                        peb.setY(val[6].trim());
                                        peb.setFloor(Integer.valueOf(val[4].trim()));
                                        peb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        peb.setIconType(matchedType.getIconType());
                                        parkingElevatorBindingService.updateParkingElevatorBinding(peb);
                                    }else {
                                        peb.setFmapID(val[0].trim());
                                        peb.setName(val[3].trim());
                                        peb.setFid(val[0].trim());
                                        peb.setX(val[5].trim());
                                        peb.setY(val[6].trim());
                                        peb.setFloor(Integer.valueOf(val[4].trim()));
                                        peb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        peb.setIconType(matchedType.getIconType());
                                        parkingElevatorBindingService.addParkingElevatorBinding(peb);
                                    }
                                }else if(IconType.MAN_LOO.getCode().equals(val[1].trim()) || IconType.GIRL_LOO.getCode().equals(val[1].trim()) ||
                                        IconType.EMERGENCY_EXIT.getCode().equals(val[1].trim())){
                                    MapBuildCommon mb = new MapBuildCommon();
                                    mb.setMap(mapid);
                                    List<MapBuildCommon> mapBuildCommonsList = mapBuildCommonService.getByConditions(val[3].trim(), mapid, null, null, null,null, null,val[0].trim());
                                    if (!NullUtils.isEmpty(mapBuildCommonsList) && mapBuildCommonsList.size() == 1) {
                                        MapBuildCommon mbList = mapBuildCommonsList.get(0);
                                        mb.setId(mbList.getId());
                                        mb.setName(val[3].trim());
                                        mb.setFid(val[0].trim());
                                        mb.setX(val[5].trim());
                                        mb.setY(val[6].trim());
                                        mb.setFloor(val[4].trim());
                                        mb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        mb.setIconType(matchedType.getIconType());
                                        mapBuildCommonService.updateMapBuild(mb);
                                    }else {
                                        mb.setFid(val[0].trim());
                                        mb.setName(val[3].trim());
                                        mb.setX(val[5].trim());
                                        mb.setY(val[6].trim());
                                        mb.setFloor(val[4].trim());
                                        mb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        mb.setIconType(matchedType.getIconType());
                                        mapBuildCommonService.addMapBuild(mb);                                    }
                                }else if(IconType.CLASS_I_BUILD.getCode().equals(val[1].trim()) || IconType.CLASS_II_BUILD.getCode().equals(val[1].trim()) ||
                                        IconType.CLASS_III_BUILD.getCode().equals(val[1].trim()) || IconType.ROAD.getCode().equals(val[1].trim())){
                                    MapBuildCommon mb = new MapBuildCommon();
                                    mb.setMap(mapid);
                                    List<MapBuildCommon> mapBuildCommonsList = mapBuildCommonService.getByConditions2(val[3].trim(), mapid, null, null,null, null, null,val[0].trim());
                                    if (!NullUtils.isEmpty(mapBuildCommonsList) && mapBuildCommonsList.size() == 1) {
                                        MapBuildCommon mbList = mapBuildCommonsList.get(0);
                                        mb.setId(mbList.getId());
                                        mb.setName(val[3].trim());
                                        mb.setFid(val[0].trim());
                                        mb.setX(val[5].trim());
                                        mb.setY(val[6].trim());
                                        mb.setFloor(val[4].trim());
                                        mb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        mb.setIconType(matchedType.getIconType());
                                        mapBuildCommonService.updateMapBuild2(mb);
                                    }else {
                                        mb.setFid(val[0].trim());
                                        mb.setName(val[3].trim());
                                        mb.setX(val[5].trim());
                                        mb.setY(val[6].trim());
                                        mb.setFloor(val[4].trim());
                                        mb.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        mb.setIconType(matchedType.getIconType());
                                        mapBuildCommonService.addMapBuild2(mb);
                                    }
                                }else if(IconType.SHOP.getCode().equals(val[1].trim())) {
                                    List<ShangJia> shangjia2 = this.findByAllShangjia2(null, mapid, null, val[3].trim(), null, null, null, null, null,val[0].trim());
                                    ShangJia shangJia = new ShangJia();
                                    shangJia.setMap(mapid);
                                    if (!NullUtils.isEmpty(shangjia2) && shangjia2.size() == 1) {
                                        ShangJia shangJiaList = shangjia2.get(0);
                                        shangJia.setId(shangJiaList.getId());
                                        shangJia.setName(val[3].trim());
                                        shangJia.setFid(val[0].trim());
                                        shangJia.setX(val[5].trim());
                                        shangJia.setY(val[6].trim());
                                        shangJia.setFloor(val[4].trim());
                                        shangJia.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        shangJia.setIconType(matchedType.getIconType());
                                        this.updateShangjia(shangJia);
                                    }else {
                                        shangJia.setName(val[3].trim());
                                        shangJia.setFid(val[0].trim());
                                        shangJia.setX(val[5].trim());
                                        shangJia.setY(val[6].trim());
                                        shangJia.setFloor(val[4].trim());
                                        shangJia.setObjectType(val[1].trim());
                                        IconType matchedType = IconType.matchByCode(val[1].trim());
                                        shangJia.setIconType(matchedType.getIconType());
                                        this.addShangjia(shangJia);
                                    }
                                }else {
                                    continue;
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
            {
                String place = "<span style='color:red;font-size:27px;'>" + addParkNum + "</span>" + "条车位数据导入成功," +
                        "<span style='color:red;font-size:27px;'>" + updatePark + "</span>" + "条车位数据更新成功。<br/>";
                String floor = "<span style='color:red;font-size:27px;'>" + addCrossFloor + "</span>" + "条跨楼层Cross导入成功," +
                        "<span style='color:red;font-size:27px;'>" + updateCrossFloor + "</span>" + "条跨楼层Cross更新成功。<br/>";
                String exit = "<span style='color:red;font-size:27px;'>" + addExit + "</span>" + "条出入口数据导入成功," +
                        "<span style='color:red;font-size:27px;'>" + updateExit + "</span>" + "条出入口数据更新成功。<br/>";
                String exclusiveCarN = "<span style='color:red;font-size:27px;'>" + addExclusiveCar + "</span>" + "条专属车位数据导入成功," +
                        "<span style='color:red;font-size:27px;'>" + updateExclusiveCar + "</span>" + "条专属车位数据更新成功。<br/>";

                res = place + floor + exit + exclusiveCarN;
            }

        } catch (Exception e) {
            throw e;
        }
        return res;
    }

    @Override
    public int importExitFromExcel(MultipartFile excelFile, String fmapID) throws Exception {
        int count = 0;
        HSSFWorkbook book = null;
        Map_2d map_2d = map2dMapper.findByfmapID(fmapID);
        Integer mapid = null;
        if (map_2d != null) {
            mapid = map_2d.getId();
        }
        try {
            //使用poi解析Excel文件
            Map<Integer, String> numNumberExist = new LinkedHashMap<>();//ID重名错误
            Map<Integer, String> nullExist = new LinkedHashMap<>();//空指针判断
            Map<Integer, String> numNumberExist_update = new LinkedHashMap<>();//

            book = new HSSFWorkbook(excelFile.getInputStream());
            //根据名称获得指定Sheet对象
            HSSFSheet hssfSheet = book.getSheetAt(0);
            int sheet_num = book.getNumberOfSheets();
            for (int kk = 0; kk < sheet_num; kk++) {
                if (hssfSheet != null) {
                    List<HSSFPictureData> pictures = book.getAllPictures();
                    Map<Integer, HSSFPictureData> picDataMap = new HashMap<>();
                    if (hssfSheet.getDrawingPatriarch() != null) {
                        for (HSSFShape shape : hssfSheet.getDrawingPatriarch().getChildren()) {
                            HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                            if (shape instanceof HSSFPicture) {
                                HSSFPicture pic = (HSSFPicture) shape;
                                int row = anchor.getRow1();
                                int pictureIndex = pic.getPictureIndex() - 1;
                                try {
                                    HSSFPictureData picData = pictures.get(pictureIndex);
                                    picDataMap.put(row, picData);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    int rows = hssfSheet.getPhysicalNumberOfRows();
                    if (rows < 2) {
                        return 0;
                    }
                    String[] titles = {"sid", "类型id", "高度", "名称", "英文名称", "x坐标", "y坐标"};
                    HSSFRow headerRow = hssfSheet.getRow(0);
                    int cells = headerRow.getPhysicalNumberOfCells();
                    List<String> cellList = new LinkedList<>();
                    for (int i = 0; i < cells; i++) {
                        String cellName = headerRow.getCell(i).getStringCellValue();
                        cellList.add(cellName);
                    }
                    if (cellList.size() < titles.length) {
                        throw new ImportUsersException("导入数据模板格式错误");
                    }

                    for (int i = 1; i < rows; i++) {
                        HSSFRow row = hssfSheet.getRow(i);
                        if (row != null) {
                            for (int j = 0; j < cells; j++) {
                                if (row.getCell(j) != null) {
                                    row.getCell(j).setCellType(CellType.STRING);
                                }
                            }
                            String[] val = new String[12];
                            for (int j = 0; j < cells; j++) {
                                HSSFCell cell = row.getCell(j);
                                if (cell != null) {
                                    switch (cell.getCellTypeEnum()) {
                                        case FORMULA:
                                            break;
                                        case NUMERIC:
                                            val[j] = String.valueOf(cell.getNumericCellValue());
                                            break;
                                        case STRING:
                                            val[j] = cell.getStringCellValue();
                                            break;
                                        default:
                                            val[j] = "";
                                    }
                                }
                            }
                            try {
                                ParkingExit exit = new ParkingExit();
                                exit.setMap(mapid);
                                //判断卡号重名
                                if (!NullUtils.isEmpty(val[0]) && !NullUtils.isEmpty(val[4]) && EXIT_ID.equals(val[1].trim()) && val[3].indexOf("地库") != -1 && val[3].indexOf("口") != -1) {
                                    List<ParkingExit> exit1s = parkMapper.findExit(val[0].trim(), null, mapid, null);
                                    try {
                                        //  String[] exit_type = val[4].trim().split(":");
                                        Short type_default = 2;
                                        if (val[3].indexOf("出口") != -1) {
                                            type_default = 1;
                                        } else if (val[3].indexOf("出入口") != -1) {
                                            type_default = 2;
                                        }else if (val[3].indexOf("地库出入口") != -1) {
                                            type_default = 3;
                                        } else {
                                            type_default = 0;
                                        }
                                        if (!NullUtils.isEmpty(exit1s) && exit1s.size() == 1) {
                                            ParkingExit exit1 = exit1s.get(0);
                                            numNumberExist.put(i, val[0]);//重名或者名称异常
                                            exit1.setFid(val[0].trim());
                                            exit1.setX(val[5].trim());
                                            exit1.setY(val[6].trim());
                                            exit1.setFloor(val[4]);
                                            exit1.setType(type_default);
                                            parkMapper.updateExit(exit1);
                                            count++;
                                            continue;
                                        } else {
                                            exit.setFid(val[0].trim());
                                            exit.setName(val[3].trim());
                                            exit.setX(val[5].trim());
                                            exit.setY(val[6].trim());
                                            exit.setType(type_default);
                                            exit.setFloor(val[4]);
                                        }
                                    } catch (Exception e) {
                                        numNumberExist.put(i, val[0]);//重名或者名称异常
                                        continue;
                                    }
                                } else {
                                    nullExist.put(i, null);
                                    continue;
                                }

                                if (parkMapper.addExit(exit) > 0) {
                                    count++;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
            }
            int failNum = numNumberExist.size() + nullExist.size();
            {
                String errorStr = count + "条数据导入成功," + failNum + "条数据导入失败。<br/>";

                if (nullExist.size() > 0) {
                    errorStr += nullExist.size() + "条信息填写不完整:<br/>";
                    Integer[] keys = new Integer[nullExist.size()];
                    nullExist.keySet().toArray(keys);
                    for (int key : keys) {
                        String id = nullExist.get(key);
                        errorStr += "第" + key + "行：" + id + "<br/>";
                    }
                }
                if (numNumberExist.size() > 0) {
                    errorStr += numNumberExist.size() + "条信息的卡号已存在更新:<br/>";
                      /*  Integer[] keys=new Integer[numNumberExist.size()];
                        numNumberExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=numNumberExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }*/
                }
                //  throw new ImportUsersException(errorStr);
            }

        } catch (Exception e) {
            throw e;
        }
        return count;
    }

    @Override
    public ParkingCompany findCompanyByPhone(String phone) {
        return parkMapper.findCompanyByPhone(phone);
    }

    @Override
    public int addLicensePos(LicensePos licensePos) {
        return parkMapper.addLicensePos(licensePos);
    }

    @Override
    public void updateLicensePos(LicensePos licensePos) {
        parkMapper.updateLicensePos(licensePos);
    }

    @Override
    public LicensePos findLicensePosByLicenseAndMap(Integer map, String license, Integer userid) {
        return parkMapper.findLicensePosByLicenseAndMap(map, license, userid);
    }

    /**
     * 根据入井时间判断各个时段的人流量
     *
     * @param map
     * @param day
     * @return
     */
    @Override
    public List<Object> getPlaceUseRecord(Integer map, int day, String content) {
        List<Object> list = new ArrayList<>();
        try {
            //1 根据查询不同的天数 划分时间段 1->24小时 7->7天 30->30tian
            //1.1 拿当前时间往前推day天
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            String startTime = dateFormat.format(calendar.getTime());//开始时间
            String endTime = null;//结束时间
            //2 根据不同的天数 走不同分支
            if (day == 1) {
                //2.1 1天划分24小时查询每个小时内井下最大人数
                for (int i = 1; i <= 24; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    endTime = dateFormat.format(calendar.getTime());
                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count = 0;
                    List<PlaceUseRecordData> data = parkingRecordMapper.findPlaceUserRecordByTime(map, startTime, endTime, content);
                    manFlowMap.put("txt", dateFormat.parse(startTime).getHours() + "h");
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                startTime = dateFormat.format(calendar.getTime());//重新设置开始时间

                day = day + 1;
                for (int i = 2; i <= day; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    endTime = dateFormat.format(calendar.getTime());

                    if (day == 8) {//周
                        // int num=dateFormat.parse(startTime).getDay();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(startTime));
                        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        if (num < 0) {
                            num = 0;
                        }
                        String week = null;
                        switch (num) {
                            case 0:
                                week = LocalUtil.get(KafukaTopics.SUN);
                                break;
                            case 1:
                                week = LocalUtil.get(KafukaTopics.MON);
                                break;
                            case 2:
                                week = LocalUtil.get(KafukaTopics.TUS);
                                ;
                                break;
                            case 3:
                                week = LocalUtil.get(KafukaTopics.WED);
                                ;
                                break;
                            case 4:
                                week = LocalUtil.get(KafukaTopics.THU);
                                ;
                                break;
                            case 5:
                                week = LocalUtil.get(KafukaTopics.FRI);
                                ;
                                break;
                            case 6:
                                week = LocalUtil.get(KafukaTopics.SAT);
                                break;
                        }
                        manFlowMap.put("txt", week);
                    } else {//日
                        String date = "";
                        switch (lang) {
                            case "ko_KR":
                                //   date = "name_ko";
                                break;
                            case "zh_CN":
                                date = (dateFormat.parse(startTime).getMonth() + 1) + LocalUtil.get(KafukaTopics.MONTH) + dateFormat.parse(startTime).getDate() + LocalUtil.get(KafukaTopics.DAY);
                                break;
                            case "en_US":
                                //  String sdasd=dateFormat.parse(endTime).toString();
                                date = dateFormat.parse(startTime).toString().substring(4, 7) + " " + (dateFormat.parse(startTime).getDate()) + "th";
                                break;
                        }

                        manFlowMap.put("txt", date);
                    }
                    List<PlaceUseRecordData> data = parkingRecordMapper.findPlaceUserRecordByTime(map, startTime, endTime, content);
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据入井时间判断各个时段的人流量
     *
     * @param map
     * @param day
     * @return
     */
    @Override
    public List<Object> getPlaceChargeRecord(Integer map, int day, String content) {
        List<Object> list = new ArrayList<>();
        try {
            //1 根据查询不同的天数 划分时间段 1->24小时 7->7天 30->30tian
            //1.1 拿当前时间往前推day天
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            String startTime = dateFormat.format(calendar.getTime());//开始时间
            String endTime = null;//结束时间
            //2 根据不同的天数 走不同分支
            if (day == 1) {
                //2.1 1天划分24小时查询每个小时内井下最大人数
                for (int i = 1; i <= 24; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    endTime = dateFormat.format(calendar.getTime());
                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count = 0;
                    List data = parkingRecordMapper.findPlaceChargeRecordByTime(map, startTime, endTime, content);
                    manFlowMap.put("txt", dateFormat.parse(startTime).getHours() + "h");
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                startTime = dateFormat.format(calendar.getTime());//重新设置开始时间

                day = day + 1;
                for (int i = 2; i <= day; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    endTime = dateFormat.format(calendar.getTime());

                    if (day == 8) {//周
                        // int num=dateFormat.parse(startTime).getDay();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(startTime));
                        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        if (num < 0) {
                            num = 0;
                        }
                        String week = null;
                        switch (num) {
                            case 0:
                                week = LocalUtil.get(KafukaTopics.SUN);
                                break;
                            case 1:
                                week = LocalUtil.get(KafukaTopics.MON);
                                break;
                            case 2:
                                week = LocalUtil.get(KafukaTopics.TUS);
                                ;
                                break;
                            case 3:
                                week = LocalUtil.get(KafukaTopics.WED);
                                ;
                                break;
                            case 4:
                                week = LocalUtil.get(KafukaTopics.THU);
                                ;
                                break;
                            case 5:
                                week = LocalUtil.get(KafukaTopics.FRI);
                                ;
                                break;
                            case 6:
                                week = LocalUtil.get(KafukaTopics.SAT);
                                break;
                        }
                        manFlowMap.put("txt", week);
                    } else {//日
                        String date = "";
                        switch (lang) {
                            case "ko_KR":
                                //   date = "name_ko";
                                break;
                            case "zh_CN":
                                date = (dateFormat.parse(startTime).getMonth() + 1) + LocalUtil.get(KafukaTopics.MONTH) + dateFormat.parse(startTime).getDate() + LocalUtil.get(KafukaTopics.DAY);
                                break;
                            case "en_US":
                                //  String sdasd=dateFormat.parse(endTime).toString();
                                date = dateFormat.parse(startTime).toString().substring(4, 7) + " " + (dateFormat.parse(startTime).getDate()) + "th";
                                break;
                        }

                        manFlowMap.put("txt", date);
                    }
                    List data = parkingRecordMapper.findPlaceChargeRecordByTime(map, startTime, endTime, content);
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Object> getPlaceMapFeeAndFlow(Integer map, int day, String content) {
        List<Object> list = new ArrayList<>();
        try {
            //1 根据查询不同的天数 划分时间段 1->24小时 7->7天 30->30tian
            //1.1 拿当前时间往前推day天
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", localUtil.getCurrentLocale());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.DAY_OF_MONTH, -day);
            String startTime = dateFormat.format(calendar.getTime());//开始时间
            String endTime = null;//结束时间
            //2 根据不同的天数 走不同分支
            if (day == 1) {
                //2.1 1天划分24小时查询每个小时内井下最大人数
                for (int i = 1; i <= 24; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                    endTime = dateFormat.format(calendar.getTime());
                    //查询startTime-->endTime时间段内的井下最大人数
                    Integer count = 0;
                    FeeAndFlow data = parkingRecordMapper.getPlaceMapFeeAndFlow(map, startTime, endTime);
                    manFlowMap.put("txt", dateFormat.parse(startTime).getHours() + "h");
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                startTime = dateFormat.format(calendar.getTime());//重新设置开始时间

                day = day + 1;
                for (int i = 2; i <= day; i++) {
                    Map<String, Object> manFlowMap = new HashMap<>();
                    calendar.add(Calendar.DAY_OF_MONTH, 1);

                    endTime = dateFormat.format(calendar.getTime());

                    if (day == 8) {//周
                        // int num=dateFormat.parse(startTime).getDay();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(dateFormat.parse(startTime));
                        int num = cal.get(Calendar.DAY_OF_WEEK) - 1;
                        if (num < 0) {
                            num = 0;
                        }
                        String week = null;
                        switch (num) {
                            case 0:
                                week = LocalUtil.get(KafukaTopics.SUN);
                                break;
                            case 1:
                                week = LocalUtil.get(KafukaTopics.MON);
                                break;
                            case 2:
                                week = LocalUtil.get(KafukaTopics.TUS);
                                ;
                                break;
                            case 3:
                                week = LocalUtil.get(KafukaTopics.WED);
                                ;
                                break;
                            case 4:
                                week = LocalUtil.get(KafukaTopics.THU);
                                ;
                                break;
                            case 5:
                                week = LocalUtil.get(KafukaTopics.FRI);
                                ;
                                break;
                            case 6:
                                week = LocalUtil.get(KafukaTopics.SAT);
                                break;
                        }
                        manFlowMap.put("txt", week);
                    } else {//日
                        String date = "";
                        switch (lang) {
                            case "ko_KR":
                                //   date = "name_ko";
                                break;
                            case "zh_CN":
                                date = (dateFormat.parse(startTime).getMonth() + 1) + LocalUtil.get(KafukaTopics.MONTH) + dateFormat.parse(startTime).getDate() + LocalUtil.get(KafukaTopics.DAY);
                                break;
                            case "en_US":
                                //  String sdasd=dateFormat.parse(endTime).toString();
                                date = dateFormat.parse(startTime).toString().substring(4, 7) + " " + (dateFormat.parse(startTime).getDate()) + "th";
                                break;
                        }

                        manFlowMap.put("txt", date);
                    }
                    FeeAndFlow data = parkingRecordMapper.getPlaceMapFeeAndFlow(map, startTime, endTime);
                    manFlowMap.put("val", data);
                    list.add(manFlowMap);
                    startTime = endTime;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

  /*  @Override
    public List<Object> getPlaceUseRecordRange(String start, String end, Integer map, String content) {
        return parkingRecordMapper.getPlaceUseRecordRange( start,  end, map, content);
    }*/

    @Override
    public boolean delViolate(String id) {
        String[] split = id.split(",");
        for (String s : split) {
            parkMapper.deleteById(s);
        }
        return true;
    }

    @Override
    public List<ParkingCompany> getComByName(String companyName) {
        return parkMapper.getComByName(companyName);
    }

    @Override
    public List<ParkingCompany> getComByNameId(String companyName, Integer companyId, Integer map) {
        return parkMapper.getComByNameId(companyName, companyId, map);
    }

    @Override
    public List<Infrared> getPlaceByName(Integer map, String PlaceName) {
        return parkMapper.getPlaceByName(map, PlaceName);
    }

    @Override
    public List<ShangJia> findShangjiaPhone(String phone, Integer id) {
        return parkMapper.findShangjiaPhone(phone, id);
    }

    //添加查
    @Override
    public List<ShangJia> findShangjiaMapName(Integer id, String name, Integer mapId) {
        return parkMapper.findShangjiaMapName(id, name, mapId);
    }

    //修改查
    @Override
    public List<ShangJia> findShangjiaMapName1(String name, Integer mapId, Integer id) {
        return parkMapper.findShangjiaMapName1(name, mapId, id);
    }

    @Override
    public List<ShangJia> getShangjiaMap(Integer id) {
        return parkMapper.getShangjiaMap(id);
    }

    @Override
    public ShangJia getShangJiaById(String id) {
        return parkMapper.getShangJiaById(id);
    }

    @Override
    public List<ParkingPlace> getPlaceByComId(Integer comId) {
        return parkMapper.getPlaceByComId(comId);
    }

    @Override
    public void updatePlaceByComId(ParkingPlace parkingPlace) {
        parkMapper.updatePlaceByComId(parkingPlace);
    }

    @Override
    public ParkingCompany getComById(String id) {
        return parkMapper.getComById(id);
    }

    @Override
    public List<Object> getExclusiveAndFreePlaces(Integer map, Integer companyId,Integer preferenceCarBit,Integer isVip,String placeName) {
        return parkMapper.getExclusiveAndFreePlaces(map, companyId,preferenceCarBit,isVip,placeName);
    }
    @Override
    public List<Object> getOrdinaryPlaces(Integer map, Integer companyId, Integer preferenceCarBit, String[] placeId, Integer placeType) {
        return parkMapper.getOrdinaryPlaces(map,companyId,preferenceCarBit,placeId,placeType);
    }

    @Override
    public ParkingPlace getPlaceByPlaceId(Integer placeId,String desc,String name,Integer map) {
        return parkMapper.getPlaceByPlaceId(placeId,desc,name,map);
    }
    @Override
    public List<ParkingPlace> getPlaceListByPlaceId(Integer placeId, String desc, String name, Integer map) {
        return parkMapper.getPlaceListByPlaceId(placeId,desc,name,map);
    }

    @Override
    public ParkingPlace getPlaceByPlaceNames(Integer mapId, String placeName,String fid,String companyId) {
        return parkMapper.getPlaceByPlaceNames(mapId, placeName,fid,companyId);
    }

    @Override
    public List<ParkingPlace> getPlaceByPlaceNameList(Integer mapId, List<String> nameList) {
        return parkMapper.getPlaceByPlaceNameList(mapId, nameList);
    }

    @Override
    public List<ParkingPlace> getCurrentPlacesBindCompany(Integer mapId, String placeName, String companyId) {
        return parkMapper.getCurrentPlacesBindCompany(mapId, placeName,companyId);
    }


    @Override
    @Async(value = "SubAsyncExecutor")
    public  CompletableFuture<Object> addLicensePos(Double lng, Double lat, String key, Double minDis, Integer place_type, Integer mapId){
        CommonResult<Object> res = new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS));
        double[] xy = Gps_xy.lonLat2Mercator(lng, lat);
        double[] target = {xy[0], xy[1], 0};
        List<MapPlace> data = parkMapper.findPlaceCountGroupByMap(1,place_type,mapId, null);
        for (MapPlace mapPlace : data) {
            double[] xy_park = Gps_xy.lonLat2Mercator(Double.parseDouble (mapPlace.getLng()), Double.parseDouble (mapPlace.getLat()));
            double[] park = {xy_park[0], xy_park[1], 0};
            double dis = PercentToPosition.getDis(target, park);
            mapPlace.setDis((int) Math.round(dis));
            int dis_int = mapPlace.getDis();
            if (dis >= 1000) {
                mapPlace.setDisText(Math.round(dis_int / 1000f * 10) / 10f + "公里");
            } else {
                mapPlace.setDisText(dis_int + "米");
            }
        }
        if (key == null || "dis".equals(key)) {
            data.sort (Comparator.comparingInt (MapPlace::getDis));
        }
        if ("place".equals (key)) {
            data.sort ((a, b) -> b.getEmpty () - a.getEmpty ());
        }
        List<MapPlace> resData = new ArrayList<>();
        if (minDis != null) {
            for (MapPlace mapPlace : data) {
                if (mapPlace.getDis() < minDis) {
                    resData.add(mapPlace);
                }
            }
        } else {
            resData = data;
        }
        res.setData(resData);
        if (resData.size() == 0) {
            res.setCode(401);
        }
        return CompletableFuture.completedFuture(res);
    }

    @Override
    public List<Infrared> getInfraredByPlaceId(String num,Integer placeId) {
        return parkMapper.getInfraredByPlaceId(num,placeId);
    }

    @Override
    @Transactional
    public Integer batchUpdateUsers(List<PlaceVo> users) {
        Instant start = Instant.now();

        try (SqlSession batchSqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH,false)) {
            ParkMapper parkMapper = batchSqlSession.getMapper(ParkMapper.class);

            int totalUsers = users.size();
            for (int i = 0; i < totalUsers; i++) {
                parkMapper.updateBatchById2(users.get(i));
                if ((i % 1000 == 0 && i > 0) || i == totalUsers-1) {
                    batchSqlSession.flushStatements();
                }
            }
            Instant end = Instant.now();
            Duration requestDuration = Duration.between(start, end);
            log.error("batchUpdateUsers执行了：" + requestDuration.toMillis() + " milliseconds.");
            return 1;
        }catch (Exception e) {
            log.error("批量更新出错", e);
            throw e;
        }

    }

    @Override
    @Transactional
    public <T,U,R> int batchUpdateOrInsert(List<T> data, Class<U> mapperClass, BiFunction<T, U, R> function) {
        int i = 1;
        try (SqlSession batchSqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.BATCH,false)) {
            U mapper = batchSqlSession.getMapper(mapperClass);
            int size = data.size();
            for (T element : data) {
                function.apply(element, mapper);
                if ((i % 1000 == 0) || i == size) {
                    batchSqlSession.flushStatements();
                    batchSqlSession.clearCache(); // 清理缓存以减少内存占用
                }
                i++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e); // CustomException 可以替换为 RuntimeException 或自定义异常
        }
        return i - 1;
    }

    @Override
    @Transactional
    public void processPlaceRecord(ParkingPlace place, Infrared infrared,String timeStr, LocalDateTime dateTime, List<ParkingPlace> places) {
        try {
            // 设置key的名称
            String lockKey = "mapId:" + place.getMap() + ":placeId:" + place.getId()+":state:"+place.getState();
            if(redissonClient.getLock(lockKey).tryLock(0, 1, TimeUnit.MINUTES)){
                PlaceUseRecord placeRecord = new PlaceUseRecord();
                List<Infrared> repeatInfrared = tagMapper.findInfraredId(place.getId(), place.getMap(), infrared.getNum());

                Short infraredUploadStatus = infrared.getStatus();
                Short repeatInfraredStatus = (NullUtils.isEmpty(repeatInfrared) ? null : repeatInfrared.get(0).getStatus());

                // 判断是否存在
                if (!NullUtils.isEmpty(repeatInfrared)) {
                    placeRecord.setPlace(place.getId());
                    placeRecord.setDateTime(dateTime);
                    if (infraredUploadStatus == 0 && repeatInfraredStatus != null && repeatInfraredStatus == 0) {
                        placeRecord.setEnd(timeStr);
                        updatePlaceRecord(placeRecord, place);
                    } else {
                        placeRecord.setMap(place.getMap());
                        placeRecord.setStart(timeStr);
                        addPlaceRecord(placeRecord, place,dateTime);
                    }
                } else {
                    placeRecord.setDateTime(dateTime);
                    placeRecord.setPlace(place.getId());
                    if (infraredUploadStatus == 0) {
                        placeRecord.setEnd(timeStr);
                        updatePlaceRecord(placeRecord, place);
                    } else {
                        placeRecord.setStart(timeStr);
                        placeRecord.setMap(place.getMap());
                        addPlaceRecord(placeRecord, place, dateTime);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    public void findAndProcessNearbyCameras(Integer placeId, Integer map,String placeName, String floor,  String x, String y, LocalDateTime baseTime) {
        CameraConfig boundCamera = cameraConfigService.findByPlaceName(placeName);

         if (!NullUtils.isEmpty(boundCamera)) {
            // 已绑定摄像头，查询视频记录
            processExistingCamera(boundCamera.getSerialNumber(), placeId, boundCamera.getId());
        } else {
            // 未绑定摄像头，查找最近的摄像头
            findNearestCamera(placeId, map, placeName, floor, x, y);
        }
    }

    private void processExistingCamera(String serialNumber, Integer placeId, Long cameraId) {
        // 根据绑定摄像头编号，查询视频车位记录中place_id为空且唯一标志位<4的数据
//        CameraConfigResponse existingBinding = cameraConfigService.getAvailableCameras(map, floor, placeName);
        List<CameraVehicleCapture> records = cameraConfigService.findRecordsBySerialNumber(serialNumber);
        processRecords(records,placeId,cameraId);
    }

    private void findNearestCamera(Integer placeId, Integer map, String placeName, String floor, String x, String y) {
        // 查找最近的摄像头
        List<CameraConfigResponse> cameras = cameraConfigService.getAvailableCameras(map, floor, placeName);
        if (NullUtils.isEmpty(cameras)) {
            return;
        }

        // 找到最近的且在半径范围内的摄像头
        CameraConfigResponse nearestCamera = cameras.stream()
                .peek(camera -> {
                    double distance = Math.sqrt(Math.pow(Double.parseDouble(camera.getX()) - Double.parseDouble(x), 2)+Math.pow(Double.parseDouble(camera.getY()) - Double.parseDouble(y), 2));
                    camera.setDistance(distance);
                })
                .filter(camera -> camera.getDistance() < Double.parseDouble(camera.getRadius()))
                .min(Comparator.comparingDouble(CameraConfigResponse::getDistance))
                .orElse(null);

        if (nearestCamera != null) {
            List<CameraVehicleCapture> records = cameraConfigService.findRecordsBySerialNumber(nearestCamera.getSerialNumber());


            processRecords(records, placeId, nearestCamera.getCameraId());
        }
    }
    private void processRecords(List<CameraVehicleCapture> records, Integer placeId, Long cameraId) {
        if (NullUtils.isEmpty(records)) {
            return;
        }

        int n = records.size();
        // 先判断 n<4
        if (n < 4) {
            // 再判断 n=1
            if (n == 1) {
                // 更新该视频记录表的车位id为检测器对应的车位id，唯一标志为1
                CameraVehicleCapture record = records.get(0);
                record.setPlace(placeId.toString());
                record.setUniqueFlag(1);
                record.setPlaceRecordTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cameraVehicleCaptureService.updateById(record);
            } else {
                // n不等于1时
                // 创建视频记录表与车位绑定记录
                // 批量保存CameraPlace记录
                List<CameraPlace> cameraPlaces = records.stream()
                        .map(record -> {
                            CameraPlace cameraPlace = new CameraPlace();
                            cameraPlace.setCameraVehicleCaptureId(record.getId());
                            cameraPlace.setPlaceId(Long.valueOf(placeId));
                            return cameraPlace;
                        }).collect(Collectors.toList());
                cameraPlaceService.saveBatch(cameraPlaces);

                // 批量更新uniqueFlag
                List<CameraVehicleCapture> updatedRecords = records.stream().peek(record -> record.setUniqueFlag(record.getUniqueFlag() + 1)).collect(Collectors.toList());
                cameraVehicleCaptureService.updateBatchById(updatedRecords);
                // 获取最大标志位k
                int k = records.stream()
                        .mapToInt(CameraVehicleCapture::getUniqueFlag)
                        .max()
                        .orElse(0);

                // 判断k是否等于记录数n
                if (k == n) {
                    // 将该车位编号更新到该n条记录表中
                    for (CameraVehicleCapture record : records) {
                        record.setPlace(placeId.toString());
                        record.setPlaceRecordTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                        cameraVehicleCaptureService.updateById(record);
                    }
                }
            }
        } else {
            // n>=4时，更新标志位为4
            for (CameraVehicleCapture record : records) {
                record.setUniqueFlag(4);
                record.setPlaceRecordTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                cameraVehicleCaptureService.updateById(record);
            }
        }

    }

    @Override
    public void addOrUpdatePlaceRecord(PlaceVo v) {
        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(v.getMap(), v.getId(), "time");
        PlaceUseRecord placeRecord = new PlaceUseRecord();
        if (NullUtils.isEmpty(placeRecords)) {
            if(v.getState()==1){
                placeRecord.setMap(v.getMap());
                placeRecord.setPlace(v.getId());
                placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
                bookMapper.addPlaceUseRecord(placeRecord);
            }
        } else {
            if(v.getState()==0){
                placeRecord.setId(placeRecords.get(0).getId());
                placeRecord.setEnd(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
                LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(placeRecord.getTimestamp()), ZoneId.of("Asia/Shanghai"));
                LocalDateTime currentDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.of("Asia/Shanghai"));
                Duration duration = Duration.between(localDateTime, currentDateTime);
                long diffInMinutes = Math.abs(duration.toMinutes());
                if (diffInMinutes < 5) {
                    SpringContextHolder2.parkingPlaceConcurrentHashMap.remove(v.getId());
                }
                bookMapper.UpdatePlaceUseRecordByid(placeRecord);
            }

        }
    }

    @Override
    public List<ExcelDataVo> getParkingPlaceList(Integer mapId) {
        return parkMapper.selectParkingPlaceList(mapId);
    }

    @Override
    public List<ParkingPlace> findByIds(List<Long> ids) {
        return parkMapper.findByIds(ids);
    }

    @Override
    public void updatePlaceTests(String map,LocalDateTime now) {
        parkMapper.updatePlaceTests(map,now);
    }


    private void addPlaceRecord(PlaceUseRecord placeRecord, ParkingPlace place, LocalDateTime baseTime) {
        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(), "time");
        if (NullUtils.isEmpty(placeRecords)) {
            placeRecord.setMap(placeRecord.getMap());
            placeRecord.setPlace(placeRecord.getPlace());
            placeRecord.setStart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
            bookMapper.addPlaceUseRecord(placeRecord);
            findAndProcessNearbyCameras(place.getId(),place.getMap(),place.getName(), place.getFloor(), place.getX(), place.getY(), baseTime);
        }
    }


    private void updatePlaceRecord(PlaceUseRecord placeRecord, ParkingPlace place) {
        List<PlaceUseRecord> placeRecords = bookMapper.selectPlaceUseRecordByPlaceidAndMapid(place.getMap(), place.getId(), "time");
        if (!NullUtils.isEmpty(placeRecords)) {
            placeRecord.setId(placeRecords.get(0).getId());
            LocalDateTime currentDateTime = LocalDateTime.now();
            Duration duration = Duration.between(placeRecord.getDateTime(), currentDateTime);
            long diffInMinutes = Math.abs(duration.toMinutes());
            if (diffInMinutes < 5) {
                SpringContextHolder2.parkingPlaceConcurrentHashMap.remove(place.getId());
            }
            bookMapper.UpdatePlaceUseRecordByid(placeRecord);
        }
    }

}
