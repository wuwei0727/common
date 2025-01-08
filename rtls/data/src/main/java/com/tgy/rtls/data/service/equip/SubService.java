package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.BsSyn;
import com.tgy.rtls.data.entity.equip.SubFirmware;
import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.equip.Substation;
import com.tgy.rtls.data.entity.map.BsConfig;
import com.tgy.rtls.data.entity.equip.DeviceVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip
 * @date 2020/10/19
 */
public interface SubService {
    /*
     * 实例下分站列表信息 num-->卡号 networkstate-->网络状态 powerstate-->供电状态 relevance-->是否关联地图 error-->故障信息 instanceid-->实例id
     * */
    List<BsSyn> findByAll(String num,Integer type,Integer networkstate,Integer powerstate,Integer relevance,Integer map,Integer error,String desc,String start,String end,Integer instanceid);

    List<BsSyn> findByAll2(String num,Integer type,String typeName,Integer networkstate,Integer powerstate,Integer power,Integer relevance,Integer map,Integer error,String desc,String start,String end,Integer instanceid,String floorName,String[] maps);
    List<DeviceVo> getSubAll();

    /*
     * 根据分站id获取分站名
     * */
    String findByNameId(String ids);

    /*
     * 分站信息详情
     * */
    Substation findById(Integer id);

    /*
     * 分站编号 重名判断
     * */
    Substation findByNum(String num);

    /*
    * 查询分站当前人数和最大人数上限
    * */
    SubSyn findByMaxnum(String num);


    /*
     * 新增分站信息
     * */
    Boolean addSub(Substation sub, String http);

    /*
    * 开放接口：新增分站信息
    * */
    Boolean addDisparkSub(BsConfig bsConfig,String num);

    /*
     * 修改分站信息
     * */
    Boolean updateSub(Substation newSub, String http) throws InterruptedException;

    /*
     * 删除分站信息
     * */
    Boolean delSub(String ids);

    /*
     * 修改基站网络状态 0离线 1在线
     * */
    void updateSubNetworkstate(String num, Integer state);

    /*
     * 修改基站供电状态 0主电供电 1备用电源供电
     * */
    void updateSubPowerstate(String num,Integer state);

    /*
     * 修改基站电压相关
     * */
    void updateSubBattery(String num, String batteryVolt, String batteryTime,Short power);

    /*
     * 修改基站ip地址
     * */
    //void updateSubIp(String num,String ip);
    /*
     * 查询地图上分站数 map-->地图ID
     * */
    int findBySubCount(Integer map);

    /*
    * 基站升级 type 0应用程序  3uwb
    * */
    void upgradeSub(Integer type,String num,String path);

    /*
    * 基站调试
    * */
    void debugSub(SubFirmware subFirmware);

    void deleteSub(String  name);


    void updateSubError(String num,Integer error);

    void delSubInstance(Integer instanceid);
    List<BsSyn> findBeaconByMap(@Param("map")Integer map);
    Substation getCurrentSubMapName(String num,@Param("map")Integer map);

    List<DeviceVo> getSubMoreThan30Days(@Param("map")String map);

    List<DeviceVo> getSubMoreThan60Days(@Param("map")String map);
    List<DeviceVo> substationBatteryTimeWarningLevelsQuery();

    void updateSubForOffline(Integer id);

    void updateSubLessThanBatterySub(Integer id);

    List<Substation> timeNotUpdateBetweenFetche(String map);

    /**
     * 导入蓝牙信标位置
     * @param fileName 文件名称
     * @param file
     * @return Excel表格
     * @throws Exception
     */
    String importSubLocationExcel(String fileName,MultipartFile file) throws Exception;

    void exportSubLocationExcel(String mapId, HttpServletResponse response) throws Exception;

    void endAllAlarms(List<String> deviceIds, Integer equipmentType);

    int updateLifetimeByMap(Integer map, Integer lifetimeMonths,Integer type);

    void updateAddTime(Integer map, String num, LocalDateTime now);
}
