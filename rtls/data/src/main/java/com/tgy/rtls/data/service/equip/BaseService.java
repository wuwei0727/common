package com.tgy.rtls.data.service.equip;

import com.tgy.rtls.data.entity.equip.Basestation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.equip.impl
 * @date 2021/1/5
 */
public interface BaseService {
    /*
     * 查询微基站信息
     * */
    List<Basestation> findByAll(String num,Integer networkstate,String desc,Integer map,Integer relevance,Integer instanceid);


    /*
     * 微基站详情信息
     * */
    Basestation findById(Integer id);

    /*
     * 微基站编号重名判断
     * */
    Basestation findByNum(String num);

    /*
     * 新增微基站
     * */
    boolean addBasestation(Basestation base);

    /*
     * 修改微基站
     * */
    boolean updateBasestation(Basestation base);

    /*
     * 修改基站状态
     * */
    void updateBaseNetworkstate(String num,Integer networkstate);

    /*
     * 删除微基站
     * */
    boolean delBasestation(String ids);

 /*   boolean delBasestationBynum(String num);*/

    /*
     * 删除微基站
     * */
    int delBasestationByInstance(@Param("instanceid")Integer instanceid);

}
