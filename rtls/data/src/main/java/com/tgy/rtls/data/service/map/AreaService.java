package com.tgy.rtls.data.service.map;

import com.tgy.rtls.data.entity.map.Area;
import com.tgy.rtls.data.entity.map.AreaSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map
 * @date 2020/10/21
 */
public interface AreaService {
    /*
     * 地图下区域信息查询 map-->地图id type-->区域类型id name-->区域名称
     * */
    List<Area> findByAll(Integer map,Integer type,String name);

    List<Area> findByMap(Integer map);
    /*
     * 地图下区域信息详情
     * */
    Map<String,Object> findById(Integer id);


    Area findByName(String name);

    Area findByAreaId(Integer id);
    /*
     * 新增区域 area-->区域信息 dots-->区域点信息 turnovers-->进出报警规则  overloads-->超员报警规则 detections-->进出口检测
     * */
    Boolean addArea(AreaSyn areaSyn,Integer instanceid);

    /*
     * 修改区域 area-->区域信息 dots-->区域点信息 turnovers-->进出报警规则  overloads-->超员报警规则 detections-->进出口检测
     * */
    Boolean updateArea(AreaSyn areaSyn,Integer instanceid);

    /*
     * 删除区域 result-->用于判断是否删除该区域的事件日志类型
     * */
    Boolean delArea(Integer id,Boolean result);
    /*
     * 删除地图下的区域 result-->用于判断是否删除该区域的事件日志类型
     * */
    void delAreaMap(String[] mapids);

    /*
     * 查看当前区域的人数情况并发送给前端
     * */
    AreaVO findByAreaCount(Integer id);
}
