package com.tgy.rtls.data.entity.promoter;

import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *@author wuwei
 *@date 2024/3/20 - 10:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PromoterInfo extends BaseEntitys {
    private String map;
    private String name;
    private String phone;
    private String province;
    private String city;
    private String area;
    private List<PromoterLog> promoterCount;
}