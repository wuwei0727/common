package com.tgy.rtls.data.service.message;

import com.tgy.rtls.data.entity.message.WarnMap;
import com.tgy.rtls.data.entity.message.WarnRecord;
import com.tgy.rtls.data.entity.message.WarnRule;
import com.tgy.rtls.data.entity.type.Status;


import javax.servlet.ServletOutputStream;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.message
 * @date 2020/10/30
 */
public interface WarnRecordService {
    /*
     * 实例下查询报警记录 instanceid-->实例id startTime-->开始时间  endTime-->结束时间  type-->报警类型
     * */
    List<WarnRecord> findByRecordAll(String instanceid,Integer map,String startTime,String endTime,Integer type,Integer warnstate, Integer areaType,String areaName);
    /*
    /*
     * 报警类型查询
     * */
    List<Status> findByWarnType();

    /*
     * 报警记录存储
     * */
    Boolean addWarnRecord(WarnRecord warn);

    /*
     * 查询正在报警的记录
     * */
    List<WarnRecord> findByWarn(Integer map);

    WarnRecord findByRecordId(Integer id);

    /*
     * 查询报警规则信息
     * */
    List<WarnRule> findByRuleAll(Integer instanceid,Integer type,Integer map, Integer enable);
    List<WarnMap> findByMap(Integer map);

    WarnRule findByType(Integer type,Integer map,Integer enable);
    /*
     * 报警规则编辑
     * */
    Boolean updateWarnRule(List<WarnRule> rules);

    /*
     * 查看人员是否有生成对应类型的报警信息 map-->地图id area-->区域id personid-->人员id  type-->类型id
     * */
    WarnRecord findByType(Integer map,Integer area,Integer personid,Integer type);

    /*
     * 结束报警记录
     * */
    boolean updateWarnRecord(String endTime,Integer id);

    void exportWarnRecord(ServletOutputStream out, String instanceid, Integer map, String startTime, String endTime, Integer type,String title,Integer areaType,String areaName,Integer warnstate)throws Exception;

}
