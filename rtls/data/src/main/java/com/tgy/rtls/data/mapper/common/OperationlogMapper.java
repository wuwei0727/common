package com.tgy.rtls.data.mapper.common;

import com.tgy.rtls.data.entity.common.Operationlog;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.common
 * @date 2020/11/10
 */
public interface OperationlogMapper {
    /*
     * 查询操作日志
     * */
    List<Operationlog> findByAll(@Param("userName") String userName, @Param("ip") String ip, @Param("startTime") String startTime, @Param("endTime") String endTime);

    /*
    * 新增操作日志 uid-->登录人员id   incident-->事件
    * */
    int addOperationlog(@Param("uid")Integer uid,@Param("incident")String incident,Integer instanceid);


    void deleteOperationlog(Integer  instanceid);

    boolean addUserOperationlog(@Param("uid") Integer uid, @Param("ip") String ip, @Param("incident") String incident, @Param("time") LocalDateTime time);
}
