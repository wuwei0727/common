package com.tgy.rtls.data.entity.promoter;

import com.baomidou.mybatisplus.annotation.TableField;
import com.tgy.rtls.data.entity.pay.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author wuwei
 * @date 2024/3/20 - 15:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PromoterQrCode extends BaseEntity {

    private Integer proInfoId;

    private Integer destId;

    private String shangjiaName;

    private String map;
    private String mapName;
    @TableField(exist = false)
    private String mapId;


    private String floor;

    private String x;

    private String y;

    private String fid;

    private String type;

    @TableField(value = "`desc`")
    private String desc;
    @TableField(exist = false)
    private String promoterPersonName;
    private String qrcode;
    private String qrcodelocal;
    @TableField(exist = false)
    private String name;

}