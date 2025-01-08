package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.SubScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2021/1/19
 * 搜寻分站管理
 */
public interface SubScanMapper {
    /**
    *获取当前搜寻到的分站信息
     * */
    List<SubScan> findByAll(@Param("status")Integer status,@Param("code1")String code1,@Param("code2")String code2,String name);

    /*
    * 根据编号判断该分站是否存储
    * */
    SubScan findByNum(@Param("num")String num);


    /*
    * 存储搜寻到的分站
    * */
    int addSubScan(@Param("subScan")SubScan subScan);

    /*
    * 修改状态
    * */
    int updateStatus(@Param("num")String num,@Param("status")Integer status);

    /*
    * 清空上一次的数据
    * */
    int delSubScan();
}
