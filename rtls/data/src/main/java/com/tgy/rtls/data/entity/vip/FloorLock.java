package com.tgy.rtls.data.entity.vip;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.config.CustomLocalDateTimeDeserializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author wuwei
 * @createTime 2023/4/5 16:13
 */
@ApiModel(description="地锁表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "floor_lock")
public class FloorLock implements Serializable {
    private static final long serialVersionUID = 1L;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @TableField(value = "place")
    private Integer place;

    @TableField(value = "device_num")
    @ApiModelProperty(value="设备编号")
    private String deviceNum;

    @TableField(value = "parking_name")
    @ApiModelProperty(value="车位名称")
    private String parkingName;

    @TableField(value = "`map`")
    @ApiModelProperty(value="地图")
    private Long map;

    @TableField(value = "`state`")
    @ApiModelProperty(value="状态")
    private Integer state;

    @TableField(value = "`floor_lock_state`")
    @ApiModelProperty(value="地锁状态.0：降锁 1：升锁,3/4：位置异常状态")
    private String floorLockState;

    @TableField(value = "`model`")
    @ApiModelProperty(value="2:正常模式3:升锁模式4：降锁模式")
    private String model;
    @TableField(value = "`power`")
    @ApiModelProperty(value="电量")
    private String power;

    @TableField(value = "`networkstate`")
    @ApiModelProperty(value="0离线1在线2低电量")
    private Byte networkstate;

    @TableField(value = "start_time")
    @ApiModelProperty(value = "开始时间")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @JSONField(deserializeUsing = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    @ApiModelProperty(value = "结束时间")
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @JSONField(deserializeUsing = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime endTime;

    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime offlineTime;

    //蜂鸟地图相关
    @TableField(exist=false)
    private String mapName;
    @TableField(exist=false)
    @ApiModelProperty(value = "key值")
    private String mapKey;
    @TableField(exist=false)
    @ApiModelProperty(value = "蜂鸟应用名称")
    private String appName;
    @TableField(exist=false)
    @ApiModelProperty(value = "蜂鸟地图ID")
    private String fmapID;
    @TableField(exist=false)
    private String mapImg;//地图路径
    @TableField(exist=false)
    private String themeImg;//主题路径
    @TableField(exist=false)
    private String x;
    @TableField(exist=false)
    private String y;
    @TableField(exist=false)
    private String fid;
    @TableField(exist=false)
    private Integer floorLockId;
    @TableField(exist=false)
    private Integer placeState;
    @TableField(exist=false)
    private String floor;
    @TableField(exist=false)
    private String mapId;
    @TableField(exist=false)
    private  Integer company;
    @TableField(exist=false)
    private long date;//当前时间 地锁
    @TableField(exist=false)
    private String floorName;
    @TableField("qrcode")
    private String qrcode;
    @TableField("qrcodelocal")
    private String qrcodelocal;

    @TableField(exist=false)
    @JsonFormat(shape= JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    @JSONField(deserializeUsing = CustomLocalDateTimeDeserializer.class)
    private LocalDateTime validEndTime;
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}