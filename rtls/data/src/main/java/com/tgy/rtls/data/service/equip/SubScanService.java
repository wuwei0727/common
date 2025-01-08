package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.SubScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2021/1/19
 */
public interface SubScanService {
    /**
     *获取当前搜寻到的分站信息
     * */
    List<SubScan> findByAll(Integer status,String code1,String code2);

    /*
     * 根据编号判断该分站是否存储
     * */
    SubScan findByNum(String num);


    /*
     * 存储搜寻到的分站
     * */
    boolean addSubScan(SubScan subScan);

    /*
     * 修改状态
     * */
    boolean updateStatus(String num , Integer status);

    /*
     * 清空上一次的数据
     * */
    boolean delSubScan();
}
