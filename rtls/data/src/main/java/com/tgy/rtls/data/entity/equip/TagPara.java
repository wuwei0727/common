package com.tgy.rtls.data.entity.equip;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/13
 * 标签
 */
@Data
@ToString
@ApiModel("标签")
public class TagPara implements Serializable {

    private String tagid;//定位卡编号
    private Float fixvalue;//校正值
    private String  mac ;//设备地址


}
