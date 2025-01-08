package com.tgy.rtls.data.entity.equip;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("标签调试")
public class TagFirmware implements Serializable {
    @ApiModelProperty("标签编号")
    private String tagid;//标签编号
    @ApiModelProperty("基站编号")
    private String bsid;//基站编号
    @ApiModelProperty("读写标志位 0读 1写")
    private Integer type;
    @ApiModelProperty("下发命令")
    private String keyOrder;//下发命令
    @ApiModelProperty("蜂鸣器鸣叫间隔")
    private Integer beepInterval;//蜂鸣器鸣叫间隔
    @ApiModelProperty("蜂鸣器状态")
    private Integer beepState;//蜂鸣器状态
    @ApiModelProperty("文字输入")
    private String text;//文字输入
    @ApiModelProperty("音频路径")
    private String url;//音频路径
    @ApiModelProperty("新标签编号")
    private String newId;//新标签编号
    @ApiModelProperty("功放 0 关闭 1打开")
    private Integer pa;//功放 0 关闭 1打开
    @ApiModelProperty("功率")
    private String powerLevel;//功率
    @ApiModelProperty("0退出低功耗模式 1开启低功耗模式")
    private Integer lowPowerMode;//0退出低功耗模式 1开启低功耗模式
    @ApiModelProperty("传感器上传周期")
    private Integer sensorInterval;//传感器上传周期
    @ApiModelProperty("运动阈值")
    private Integer moveLevel;//运动阈值
    @ApiModelProperty("心率监测间隔")
    private Integer heartInterval;//心率监测间隔
    @ApiModelProperty("定位间隔")
    private Integer locaInval;//定位间隔
    @ApiModelProperty("接收窗口时间")
    private Integer rxInval;//接收窗口时间
    @ApiModelProperty("目标基站地址")
    private String groupbslist;//目标基站地址
    @ApiModelProperty("组测距周期")
    private Integer grouprangetime;//组测距周期
}
