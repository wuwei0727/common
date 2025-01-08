package com.tgy.rtls.data.mapper.user;

import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.user.PersonVO;
import com.tgy.rtls.data.entity.xianjiliang.TypeCount;
import com.tgy.rtls.data.entity.xianjiliang.TypeTime;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.user
 * @date 2020/10/14
 */
public interface PersonMapper {
    /*
    * 查询实例下人员信息 instanceid-->实例id departmentid-->部门id worktypeid-->工种id jobid-->职务id status-->在线状态  keyword-->关键字（姓名/工号）
    * */
    List<PersonVO> findByAll(@Param("instanceid")String instanceid, @Param("departmentid")Integer departmentid,
                             @Param("worktypeid")Integer worktypeid, @Param("jobid")Integer jobid,@Param("classid")Integer classid,
                             @Param("status")Integer status, @Param("keyword")String keyword,@Param("workorder")Integer workorder,String name);

    /*
    * 根据人员id查询人员名并将其拼接成一个字符
    * */
    String findByNameId(@Param("ids")String[] ids);

    /*
    * 实例下查询所有在线人员的信息 用于发送撤退命令
    * */
    List<Person> retreatPerson(@Param("instanceid")Integer instanceid);

    /*
    * 查询地图上所有在线人数
    * */
    List<Person> findByPersonOnLine(@Param("map")Integer map);
    /*
    * 查询井下人员信息
    * */
    List<Person> findByInCoal(@Param("map")Integer map);

    /*
    * 查询所有在井下的人员信息
    * */
    List<Person> findByInCoalPerson();
    /*
    * 查询白名单下人员信息 turnoverid-->进出报警规则id
    * */
    List<PersonVO> findByWhitelist(@Param("turnoverid")Integer turnoverid,String name);

    /*
    * 实例下人员的详情信息 id-->人员id
    * */
    Person findById(@Param("id")Integer id,String offline);
    /**
     *   查询所有样品
     */

   List<Person> findAllPerson();

    /*
     * 查询离线人员信息
     * */
    List<Person> findByPersonOff(@Param("map")Integer map);

    /*
     * 判断人员离线时长 id-->人员id
     * */
    Person findByOffLine(@Param("id")Integer id,String name);

    /*
    * 判断人员工号重名
    * */
    Person findByNum(@Param("num")String num);

    /*
    * 实例下新增人员
    * */
    int addPerson(@Param("person")Person person);

    /*
     * 实例下修改人员
     * */
    int updatePerson(@Param("person")Person person);

    /*
     * 实例下删除人员
     * */
    int delPerson(@Param("ids")String[] ids);

    /*
    *  删除实例下的人员 instanceid-->实例id
    * */
    int delPersonInstance(@Param("instanceid")Integer instanceid);

    /*
    * 判断人员是否绑定了标签  根据标签编号查询人员信息 num-->标签编号
    * */
    Person findByTagNum(@Param("num")String num);

    /*
    * 修改人员的井下状态和下井时间
    * */
    int updatePersonMine(@Param("personid")Integer personid,@Param("minestate")Integer minestate, @Param("time")String time);

    /*
    * 修改人员的离线时间
    * */
    int updatePersonOff(@Param("personid")Integer personid, @Param("offTime")String offTime);

    /*
    * 修改人员所在地图
    * */
    int updatePersonMap(@Param("personid")Integer personid,@Param("map")Integer map);

    /*
     *修改人员所在分站和进入分站时间
     * */
    int updatePersonSub(@Param("personid")Integer personid,@Param("sub")String sub,@Param("insubTime")String insubTime);

    /*
     * 查询有多少人在井下 map-->地图id
     * */
    int findByCount(@Param("map")Integer map);

    /*
    * 查询有多少人离线 map-->地图id
    * */
    int findByOff(@Param("map")Integer map);

    /*
    * 查询有多少人超时 map-->离线id
    * */
    int findByOvertime(@Param("map")Integer map);

    /*
    * 删除人员的下井记录 ids-->人员id集
    * */
    int delIncoal(@Param("ids")String[] ids);

    /*
    * 删除人员的出入区域记录 ids-->人员id集
    * */
    int delInarea(@Param("ids")String[] ids);
    /*
    * 删除人员的出入分站记录  ids-->人员id集
    * */
    int delInsub(@Param("ids")String[] ids);
    /*
    * 删除人员的报警记录  ids-->人员id集
    * */
    int delWarnrecord(@Param("ids")String[] ids);
    /**
     * 西安计量研究院统计人员/样品，按照类别统计样品检测耗时统计
     */
    List<TypeTime> findTypeTimeByTypeid(@Param("instanceid") Integer instanceid,@Param("typeId") Integer typeId);

    List<TypeCount> findTypeCountByTypeid(@Param("instanceid") Integer instanceid,@Param("typeId") Integer typeId);






}
