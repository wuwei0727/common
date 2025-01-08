package com.tgy.rtls.data.entity.view;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.view
 * @Author: wuwei
 * @CreateTime: 2022-09-17 22:23
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@ApiModel(description = "大屏数据")
@Accessors(chain = true)
public class ViewVo{
    private Short gender;//用户的性别，值为 1 时是男性，值为 2 时是女性，值为 0 时是未知
    private String city;//城市
    private Integer userCount;//用户总数

    /**
     * Construct
     */
    @ApiModelProperty(value = "车位总数")
    private Integer carBitCount;//
    private Integer carBitIdle;//空闲车位
    private Integer carBitOccupy;//占用车位
    private Integer carBitReserved;//已预约
    private Integer carBitExclusive;//专用车位
    private Integer carBitVIP;//VIP车位
    private Integer carBitChargePark;//充电车位

    private Integer detectorCount;//车位检测器总数
    private Integer detectorOffLine;//离线
    private Integer detectorOnLine;//在线
    private Integer detectorLowPower;//车位检测器低电量

    private Integer subCount;//蓝牙信标总数
    private Integer subOffLine;//离线
    private Integer subOnLine;//在线
    private Integer subLowPower;//信标低电量

    private Integer roadSpikeCount;//蓝牙信标总数
    private Integer rsOnLine;//在线
    private Integer rsOffLine;//离线

    private Integer gatewayCount;//网关总数
    private Integer gatewayOffLine;//离线
    private Integer gatewayOnLine;//在线
    private Integer gatewayLowPower;//网关低电量

    private Integer allMerchant;//入驻商家总数
    private Integer allFirm;//入驻公司总数
    private Integer num;//数量
    private Integer man;//男
    private Integer female;//女
    private Integer unknown;//未知

    //实时进出数据
    private String time;//进出时间
    private String parkingLot;//停车场
    private String carbit;//车位

    private Integer totalNum;//总数量
    private Integer month;//月

    private String perMonth;//每月
    private String monthTotalNum;//每月用户总数
    private String useFrequency;//累积使用频次

    private String findCarFrequency;//寻车次数
    private String useCarFrequency;//车位使用次数
    private String recommendFrequency;//推荐次数
    private String abbreviation;


    private Integer free;//空闲
    private Integer occupy;//占用
    private Integer reserved;//已预约
    private Integer chargePark;//充电车位

    //蜂鸟地图相关)
    @ApiModelProperty(value = "地图名称")
    private String mapName;
    @ApiModelProperty(value = "地图Id")
    private Integer mapId;
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    @ApiModelProperty(value = "地图路径")
    private String mapImg;//
    @ApiModelProperty(value = "主题路径")
    private String themeImg;//
    @ApiModelProperty(value = "经度")
    private String lng;
    @ApiModelProperty(value = "纬度")
    private String lat;
    private String map;//websocket
    private String place;//websocket

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        ViewVo viewVo = new ViewVo();
        ViewVo viewVo1 = viewVo.setCity("");
    }
}
