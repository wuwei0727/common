package com.tgy.rtls.data.mapper.promoter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tgy.rtls.data.entity.promoter.PromoterQrCode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/3/20 - 15:24
 */
public interface PromoterQrCodeMapper extends BaseMapper<PromoterQrCode> {
    int deleteByPrimaryKey(Integer id);

    @Override
    int insert(PromoterQrCode record);

    int insertSelective(PromoterQrCode record);

    PromoterQrCode selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(PromoterQrCode record);

    int updateByPrimaryKey(PromoterQrCode record);

    List<PromoterQrCode> getPromoterQrCodeInfo(@Param("name") String name, @Param("shangJiaName") String shangJiaName, @Param("map") Integer map, @Param("desc") String desc, @Param("mapids") String[] mapids);

    int deleteByIdIn(@Param("ids")String[] ids);

    int updateQrCodeById(PromoterQrCode record);

    List<PromoterQrCode> getById(Integer id);
}