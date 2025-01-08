package com.tgy.rtls.data.mapper.map;

import com.tgy.rtls.data.entity.map.BsConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.mapper.map
 * @date 2020/10/20
 * 分站参数配置接口
 */
public interface BsConfigMapper {
    /*
    * 地图下查询分站参数信息 map-->地图id
    * */
    List<BsConfig> findByAll(@Param("map")Integer map,@Param("name")String name);

    /*
    * 分站参数详情 id-->自增id
    * */
    BsConfig findById(@Param("id")Integer id,@Param("name")String name);
    BsConfig findById2(@Param("id")String id);

    /*
    * 根据分站编号查询分站参数信息
    * */
    BsConfig findByNum(@Param("num")String num,@Param("name")String name);
    List<BsConfig> findByNum2(@Param("num")String num,@Param("name")String name);

    /*
    * 新增分站参数信息
    * */
    int addBsConfig(@Param("bsid")Integer bsid,@Param("disfix")String disfix,@Param("antennadelay")String antennadelay);

    int addDisparkBsConfig(@Param("bsConfig")BsConfig bsConfig);

    /*
    * 修改分站参数信息
    * */
    int updateBsConfig(@Param("bsConfig")BsConfig bsConfig);

    //int updateSubBsConfig(@Param("bsid")Integer bsid,@Param("disfix")String disfix,@Param("antennadelay")String antennadelay);

    /*
    * 删除分站参数信息
    * */
    int delBsConfig(@Param("ids")String[] ids);

    /**
     * 删除信标
     * @param ids
     * @return
     */
    int delSub(@Param("ids")String[] ids);

    String getSub(@Param("bsid") Integer bsid);

    /*
     * 根据分站id删除分站参数信息
     * */
    int delBsConfigBsid(@Param("ids")String[] ids);

    /*
     * 通过识别码和基站编号找到对应的基站信息 code1-->识别码1 code2-->识别码2 num-->基站编号
     * */
    BsConfig findByCodeNum(@Param("code1")String code1, @Param("code2")String code2, @Param("num")String num,@Param("name")String name);


}
