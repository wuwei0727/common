package com.tgy.rtls.data.service.common;

import com.tgy.rtls.data.entity.common.Operationlog;

import javax.servlet.ServletOutputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common
 * @date 2020/11/19
 */
public interface OperationlogService {
    /*
     * 查询操作日志
     * */
    List<Operationlog> findByAll(String userName, String ip, String startTime, String endTime);

    /*
     * 新增操作日志 uid-->登录人员id   incident-->事件
     * */
    boolean addOperationlog(Integer uid,String incident,Integer instanceidNew);
    boolean addOperationlog(Integer uid,String incident);
    boolean addOperationloguser(Integer userId,String incident);
    /*
     * 删除操作日志
     * */
    void deleteOperationlog(Integer instanceid);


    /*
    * 操作日志导出
    * */
    void exportOperation(ServletOutputStream out,String startTime,String endTime,String title)throws Exception;

    boolean addUserOperationlog(Integer uid, String ip, String incident, LocalDateTime time);
}
