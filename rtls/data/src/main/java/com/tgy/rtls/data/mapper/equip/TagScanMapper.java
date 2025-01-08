package com.tgy.rtls.data.mapper.equip;

import com.tgy.rtls.data.entity.equip.DeviceInfoVO;
import com.tgy.rtls.data.entity.equip.Infrared;
import com.tgy.rtls.data.entity.equip.TagScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.equip
 * @date 2021/1/19
 * 搜寻定位卡管理
 */
public interface TagScanMapper {
    /*
    * 搜寻定位卡列表
    * */
    List<TagScan> findByAll(@Param("status")Integer status,@Param("type")Integer type,@Param("code1")String code1,@Param("code2")String code2,String name);

    /*
    * 根据编号判断定位卡是否存储
    * */
    TagScan findByNum(@Param("num")String num);

    /*
    * 存储搜寻到的定位卡
    * */
    int addTagScan(@Param("tagScan")TagScan tagScan);

    /*
    * 修改状态
    * */
    int updateStatus(@Param("num")String num ,@Param("status")Integer status);

    /*
    * 清空搜寻到的定位卡
    * */
    int delTagScan();

    int addInfrared(@Param("infrared") Infrared infrared);

    int updateInfrared(DeviceInfoVO infrared);

}
