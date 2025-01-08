package com.tgy.rtls.data.mapper.view;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.equip.InfraredOrigin;
import com.tgy.rtls.data.entity.view.PFindCar;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.mapper.view
 * @Author: wuwei
 * @CreateTime: 2023-08-08 17:03
 * @Description: TODO
 * @Version: 1.0
 */
@Mapper
public interface FindCarMapper extends BaseMapper<PFindCar> {
    void insertFindCar(@Param("findcar") PFindCar findcar);

//    List<PFindCar> selectList(@Param("map")String map);
    List<PFindCar> selectList(@Param("findCar")PFindCar findCar);
    void insertInfraredOrigin(@Param("infrared") InfraredOrigin infraredOrigin);
}
