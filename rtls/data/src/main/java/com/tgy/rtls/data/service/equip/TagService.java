package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.*;
import com.tgy.rtls.data.entity.nb_device.Nb_device;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip
 * @date 2020/10/16
 */
public interface TagService {
    /*
     * 实例下定位卡信息查询 num-->卡号 instanceid-->实例id
     * */
    List<Tag> findByAll(String num,Integer binding,String desc,Integer instanceid);

    /*
     * 查询地图上在线的标签
     * */
    List<Tag> findByTagOnLine(Integer map);

    /*
     * 根据定位卡id获取定位卡编号
     * */
    String findByNameId(String ids);

    /*
     * 实例下定位卡详情查询 id-->定位卡id
     * */
    Tag findById(Integer id);

    /*
     * 实例下定位卡卡号重名判断 num-->卡号
     * */
    Tag findByNum(String num);

    /*
     * 实例下新增定位卡
     * */
    Boolean addTag(Tag tag);

    /*
     * 实例下修改定位卡
     * */
    Boolean updateTag(Tag tag);

    /*
     * 删除定位卡 ids-->定位卡id集
     * */
    Boolean delTag(String ids);

    /*
     * 标签在线离线状态转换 num-->定位卡标签  status-->状态 0离线 1在线
     * */
    void updateTagStatus(String num, Integer status);

    /*
     * 标签电压信息修改
     * */
    void updateTagBatteryTime(String num,Double batteryVolt,String batteryTime);

    /*
    * 标签导入
    * */
    int importLabelFromExcel(MultipartFile excelFile,Integer instanceid)throws Exception;

    /*
    * 标签应用程序版本升级
    * */
    void upgradeTag(String num,String path);

    /*
    * 标签调试
    * */
    void debugTag(TagFirmware tagFirmware);

    void delTagInstance(Integer instanceid);


    void addTagVolt(TagVolt tagVolt);
    void addBeaconRssi(TagBeacon tagBeacon);

    TagPara  findTagid(String tagid);
    TagPara  findTagMac( String mac);
    /*
     * 查询nb设备
     * */
    List findByNbMac(@Param("mac") String mac,@Param("license") String license);

    void addNb(@Param("nb_device")Nb_device nb_device);

    void updateNb(@Param("nb_device")Nb_device nb_device);

}
