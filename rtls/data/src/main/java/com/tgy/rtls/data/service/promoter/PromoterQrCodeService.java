package com.tgy.rtls.data.service.promoter;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tgy.rtls.data.entity.promoter.PromoterQrCode;

import java.util.List;

/**
 * @author wuwei
 * @date 2024/3/20 - 15:22
 */
public interface PromoterQrCodeService extends IService<PromoterQrCode> {

    int deleteByPrimaryKey(Integer id);

    int insert(PromoterQrCode record);

    int insertSelective(PromoterQrCode record);

    PromoterQrCode selectByPrimaryKey(Integer id);
    List<PromoterQrCode> getById(Integer id);

    int updateByPrimaryKeySelective(PromoterQrCode record);

    int updateByPrimaryKey(PromoterQrCode record);
    int updateQrCodeById(PromoterQrCode record);

    List<PromoterQrCode> getPromoterQrCodeInfo(String name, String shangJiaName, Integer map, String desc, String[] mapids);

    void deleteByIdIn(String[] split);
}

