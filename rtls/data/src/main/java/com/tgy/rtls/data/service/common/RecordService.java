package com.tgy.rtls.data.service.common;

import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.user.PersonArea;
import com.tgy.rtls.data.entity.user.PersonIncoal;
import com.tgy.rtls.data.entity.user.PersonOff;
import com.tgy.rtls.data.entity.user.PersonSub;

import javax.servlet.ServletOutputStream;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.common
 * @date 2020/10/29
 */
public interface RecordService {
    /*
     * 添加下井记录
     * */
    Boolean addIncoal(Integer personid,Integer map);

    /*
     * 修改下井记录  人员离井后 结束下井记录
     * */
    Boolean updateIncoal(Integer personid,Integer map,String  outTime);

    /*
     * 查询进入分站的记录
     * */
    int findByInSub(Integer personid,String num);
    /*
     * 添加进入分站记录
     * */
    int addInsub(Integer personid,String num,Integer map);

    /*
     * 修改进入分站记录
     * */
    int updateInsub(Integer personid,String outTime);

    /*
     * 查询进出区域的记录
     * */
    int findByInArea(Integer personid,Integer area);
    /*
     * 添加进入区域记录
     * */
    int addInArea(Integer personid,Integer area,Integer map);

    /*
     * 修改进入区域记录
     * */
    int updateInArea(Integer personid,Integer area, String outTime);

    /*
     * 井下人数信息 map-->地图id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonIncoal> findByIncal(Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword);

    /*
     * 离线人数信息 map-->地图id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonOff> findByOff(Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword);

    /*
     * 超时人员信息 map-->地图id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonIncoal> findByOvertime(Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword,Integer pageIndex,Integer pageSize);
    /*
     *地图分站信息统计 num-->卡号 networkstate-->网络状态 powerstate-->供电状态  map-->地图id error-->故障信息 instanceid-->实例id
     * */
    List<SubSyn> findBySub(Integer map, String num, Integer networkstate, Integer powerstate,Integer error);

    /*
     * 地图分站信息统计 num-->分站编号 departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonSub> findByPersonSub(String num,Integer departmentid, Integer worktypeid,Integer jobid, String keyword);

    /*
     * 重点区域
     * */
    List<PersonArea> findByPersonArea(String area, Integer departmentid,Integer worktypeid,Integer jobid,String keyword);

    /*
     * 项目概览-区域列表
     * */
    List<AreaVO> findByArea(Integer map,String name,Integer type, Integer enable);

    /*
    * 井下人数信息导出
    * */
    void exportPersonIncoalToExcel(ServletOutputStream out,Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword,String title) throws Exception;

    /*
     * 离线人数信息导出
     * */
    void exportPersonOff(ServletOutputStream out,Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword,String title) throws Exception;
    /*
     * 超时人数信息导出
     * */
    void exportPersonOvertimeToExcel(ServletOutputStream out,Integer map, Integer departmentid, Integer worktypeid,Integer jobid, String keyword,String title) throws Exception;
    /*
     * 地图分站信息导出
     * */
    void exportexportSubToExcel(ServletOutputStream out,Integer map, String num, Integer networkstate, Integer powerstate,Integer error,String title) throws Exception;
    /*
     * 重点区域信息导出
     * */
    void exportAreaToExcel(ServletOutputStream out,Integer map,String name,Integer type, Integer enable,String title) throws Exception;

    /*
     * 地图分站人数信息导出
     * */
    void  exportPersonSub(ServletOutputStream out,String num,Integer departmentid, Integer worktypeid,Integer jobid, String keyword,String title)throws Exception;

    /*
     * 重点区域人数导出
     * */
    void exportPersonArea(ServletOutputStream out,String area, Integer departmentid,Integer worktypeid,Integer jobid,String keyword,String title)throws Exception;

}
