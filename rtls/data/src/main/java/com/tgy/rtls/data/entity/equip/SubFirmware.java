package com.tgy.rtls.data.entity.equip;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/10
 * 标签调试
 */
@Data
@ToString
public class SubFirmware implements Serializable {
    private String bsid;//基站编号
    private String keyOrder;//下发命令
    private Integer beepInterval;//蜂鸣器鸣叫间隔
    private Integer beepState;//蜂鸣器状态
    private String backgroundUrl;//背景图片
    private String word;//公司文字
    private String locationword;//基站位置信息
    private Integer warningState;//继电器开关 0：关 1：开
    private String powerLevel;//功率
    private String backgroundUrllocal;//背景图片
}
