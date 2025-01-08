package com.tgy.rtls.data.service.map;

import com.tgy.rtls.data.entity.map.BsConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.map
 * @date 2020/10/20
 */
public interface BsConfigService {

    /*
     * 地图下查询分站参数信息 map-->地图id
     * */
    List<BsConfig> findByAll(Integer map);
    String findByAll1(Integer map);

    /*
     * 分站参数详情 id-->自增id
     * */
    BsConfig findById(Integer id);

    /*
     * 根据分站编号查询分站参数信息
     * */
    BsConfig findByNum(String num);


    /*
     * 修改分站参数信息
     * */
    Boolean updateBsConfig(BsConfig bsConfig);

    Boolean updateBsConfig(BsConfig bsConfig,String num);

    /*
     * 删除分站参数信息
     * */
    Boolean delBsConfig(String ids);
    Boolean delBsConfig1(String ids, Integer map);

    void addDisparkBsConfig(@Param ("bsConfig")BsConfig bsConfig);
}
