package com.tgy.rtls.data.entity.equip;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/31
 * 地图分站统计信息
 */
@Data
@ToString
public class SubSyn  implements Serializable {
    private Integer id;
    private String num;//分站编号
    private String typeName;//类型名
    private String networkName;//网络状态名
    private String powerName;//供电状态名
    private String errorName;//错误码名称
    private Integer count;//检测人数
    private Integer maxnum;//人数上限
    private String status;//正常 超员
}
