package com.tgy.rtls.data.mapper.common;

import com.tgy.rtls.data.entity.equip.SubSyn;
import com.tgy.rtls.data.entity.map.AreaVO;
import com.tgy.rtls.data.entity.user.PersonArea;
import com.tgy.rtls.data.entity.user.PersonIncoal;
import com.tgy.rtls.data.entity.user.PersonOff;
import com.tgy.rtls.data.entity.user.PersonSub;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.common
 * @date 2020/10/28
 * 人员记录相关接口
 */
public interface RecordMapper {
    /*
    * 添加下井记录
    * */
    int addIncoal(@Param("personid")Integer personid,@Param("map")Integer map);

    /*
    * 修改下井记录  人员离井后 结束下井记录
    * */
    int updateIncoal(@Param("personid")Integer personid,@Param("map")Integer map, @Param("outTime")String outTime);

    /*
    * 删除下井记录 ids-->地图id集
    * */
    int delIncoal(@Param("ids")String[] ids);

    /*
    * 查询进入分站的记录
    * */
    int findByInSub(@Param("personid")Integer personid,@Param("num")String num);
    /*
     * 添加进入分站记录
     * */
    int addInsub(@Param("personid")Integer personid,@Param("num")String num,@Param("map")Integer map);

    /*
     * 修改进入分站记录
     * */
    int updateInsub(@Param("personid")Integer personid, @Param("outTime")String outTime);

    /*
    * 删除进出分站记录 num-->分站编号
    * */
    int delInsub(@Param("ids")String[] ids);

    /*
    * 查询进出区域的记录
    * */
    int findByInArea(@Param("personid")Integer personid,@Param("area")Integer area);

    /*
    * 添加进入区域记录
    * */
    int addInArea(@Param("personid")Integer personid,@Param("area")Integer area,@Param("map")Integer map);

    /*
     * 修改进入区域记录
     * */
    int updateInArea(@Param("personid")Integer personid,@Param("area")Integer area, @Param("outTime")String outTime);

    /*
    * 删除进出区域记录, LocalUtil.get(KafukaTopics.TIMEFORMAT)
    * */
    int delInArea(@Param("id")Integer id);

    /*
     * 井下人数信息 map-->地图id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonIncoal> findByIncal(@Param("map")Integer map, @Param("departmentid")Integer departmentid,
                                   @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,
                                   @Param("keyword")String keyword,String timeformat,String online,String offline);

    /*
     * 离线人数信息 map-->地图id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonOff> findByOff(@Param("map")Integer map, @Param("departmentid")Integer departmentid,
                              @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,
                              @Param("keyword")String keyword,String timeformat,String online,String offline);

    /*
    * 超时人员信息 instanceid-->实例id map-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
    * */
    List<PersonIncoal> findByOvertime(@Param("map")Integer map, @Param("departmentid")Integer departmentid,
                                   @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,
                                   @Param("keyword")String keyword,@Param("pageIndex")Integer pageIndex,@Param("pageSize")Integer pageSize,String name,String timeformat,String offline);

    /*
    *地图分站信息统计 num-->卡号 networkstate-->网络状态 powerstate-->供电状态 relevance-->是否关联地图 map-->地图id error-->故障信息 instanceid-->实例id
    * */
    List<SubSyn> findBySub(@Param("map")Integer map,@Param("num")String num,@Param("networkstate")Integer networkstate,@Param("powerstate")Integer powerstate,@Param("error")Integer error,@Param("name")String name);

    /*
     * 地图分站信息统计 num-->分站编号 departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
     * */
    List<PersonSub> findByPersonSub(@Param("num")String num, @Param("departmentid")Integer departmentid,
                                    @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,
                                    @Param("keyword")String keyword,String timeformat);

    /*
     * 重点区域
     * */
    List<PersonArea> findByPersonArea(@Param("area")String area,@Param("departmentid")Integer departmentid,
                                      @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,
                                      @Param("keyword")String keyword,String timeformat);

    /*
    * 项目概览-区域列表
    * */
    List<AreaVO> findByArea(@Param("map")Integer map,@Param("name")String name,@Param("type")Integer type,
                            @Param("enable")Integer enable);



}
