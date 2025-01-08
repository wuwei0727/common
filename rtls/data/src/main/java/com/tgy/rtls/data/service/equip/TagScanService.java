package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.equip.TagScan;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip
 * @date 2021/1/19
 */
public interface TagScanService {
    /*
     * 搜寻定位卡列表
     * */
    List<TagScan> findByAll(Integer status, Integer type,String code1,String code2);

    /*
     * 根据编号判断定位卡是否存储
     * */
    TagScan findByNum(String num);

    /*
     * 存储搜寻到的定位卡
     * */
    boolean addTagScan(TagScan tagScan);


    /*
     * 修改状态
     * */
    boolean updateStatus(String num ,Integer status);

    /*
     * 清空搜寻到的定位卡
     * */
    boolean delTagScan();


    boolean addInfrared(Infrared infrared);
    boolean updateInfrared(DeviceInfoVO infrared);
}
