package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.Basestation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2021/1/4
 * 微基站接口
 */
public interface BaseMapper {
    /*
    * 查询微基站信息
    * */
    List<Basestation> findByAll(@Param("num")String num,@Param("networkstate")Integer networkstate,@Param("desc")String desc,
                                @Param("map")Integer map,@Param("relevance")Integer relevance,@Param("instanceid")Integer instanceid,String name);


    /*
    * 微基站详情信息
    * */
    Basestation findById(@Param("id")Integer id);

    /*
    * 微基站编号重名判断
    * */
    Basestation findByNum(@Param("num")String num);

    /*
    * 新增微基站
    * */
    int addBasestation(@Param("base")Basestation base);

    /*
    * 修改微基站
    * */
    int updateBasestation(@Param("base")Basestation base);

    /*
    * 修改基站状态
    * */
    int updateBaseNetworkstate(@Param("num")String num,@Param("networkstate")Integer networkstate);

    /*
    * 删除微基站
    * */
    int delBasestation(@Param("ids")String[] ids);
    /*
     * 删除微基站
     * */
    int delBasestationByInstance(@Param("instanceid")Integer instanceid);



}
