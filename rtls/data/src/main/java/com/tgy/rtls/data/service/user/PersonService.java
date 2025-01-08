package com.tgy.rtls.data.service.user;

import com.tgy.rtls.data.entity.equip.InfraredOrigin;
import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.user.PersonVO;
import com.tgy.rtls.data.entity.xianjiliang.TypeCount;
import com.tgy.rtls.data.entity.xianjiliang.TypeTime;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service
 * @date 2020/10/14
 */
public interface PersonService {
    /*
     * 查询实例下人员信息 instanceid-->实例id departmentid-->部门id worktypeid-->工种id jobid-->职务id  keyword-->关键字（姓名/工号）
     * */
    List<PersonVO> findByAll(String instanceid, Integer departmentid, Integer worktypeid, Integer jobid,Integer classid, Integer status, String keyword,Integer workorder);


    /*
     * 查询井下人员信息
     * */
    List<Person> findByInCoal(Integer map);
    /*
     * 查询所有在井下的人员信息
     * */
    List<Person> findByInCoalPerson();
    /*
     * 查询地图上所有在线人数
     * */
    List<Person> findByPersonOnLine(Integer map);

    /*
     * 查询离线人员信息
     * */
    List<Person> findByPersonOff(Integer map);

    /*
     * 实例下人员的详情信息 id-->人员id
     * */
    Person findById(Integer id);

    /*
     * 判断人员离线时长 id-->人员id
     * */
    Person findByOffLine(Integer id);

    /*
     * 根据人员id查询人员名并将其拼接成一个字符
     * */
    String findByNameId(String ids);

    /*
     * 判断人员工号重名
     * */
    Person findByNum(String num);
    /*
     * 实例下新增人员
     * */
    Boolean addPerson(Person person);

    /*
     * 实例下修改人员
     * */
    Boolean updatePerson(Person person);

    /*
     * 实例下删除人员
     * */
    Boolean delPerson(String ids);


    /*
     *  删除实例下的人员 instanceid-->实例id
     * */
    Boolean delPersonInstance(Integer instanceid);

    /*
     * 判断人员是否绑定了标签  根据标签编号查询人员信息 num-->标签编号
     * */
    Person findByTagNum(String num);

    /*
     * 修改人员的离线时间
     * */
    void updatePersonOff(Integer personid, String offTime);

    /*
     * 修改人员的井下状态和下井时间
     * */
    void updatePersonMine(Integer personid,Integer minestate,String time);

    /*
     * 修改人员所在地图
     * */
    void updatePersonMap(Integer personid, Integer map);


    /*
     *修改人员所在分站和进入分站时间
     * */
    void updatePersonSub(Integer personid, String sub, String insubTime);

    /*
     * 查询有多少人在井下 map-->地图id
     * */
    int findByCount(Integer map);

    /*
     * 查询有多少人离线 map-->地图id
     * */
    int findByOff(Integer map);

    /*
    * 查询有多少人超时 map->地图id
    * */
    int findByOvertime(Integer map);

    /*
     * 人员导入
     * */
    int importPersonFromExcel(MultipartFile excelFile, Integer instanceid)throws Exception;

    /*
     * 人员信息导出
     * */
    void exportPerson(ServletOutputStream out, String instanceid,String keyword, Integer departmentid, Integer worktypeid, Integer jobid
            ,Integer status,String title)throws Exception;


    /*
     * 车位检测器信息导出
     * */
    void exportInfrered(ServletOutputStream out, List<InfraredOrigin> list,String title) throws IOException;
    void exportInfrered2(ServletOutputStream out, List<ESInfraredOriginal> list,String title) throws IOException;
    /*
     * 车位检测器信息导出
     * */
    void exportInfreredCount(ServletOutputStream out, Map map) throws IOException;
    void exportInfreredCount2(ServletOutputStream out, List<ESInfraredOriginal> esInfraredOriginals) throws IOException;

    /**
     * 样品超时
     * @return
     */
    List<Person> findAllPerson();
  //单间样品分类统计时间
    List<TypeTime> findTypeTimeByTypeid(Integer instanceid,Integer typeId);
    //统计盘点
    List<TypeCount> findTypeCountByTypeid(Integer instanceid, Integer typeId);
}

