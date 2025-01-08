package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wuwei
 */
@ApiModel(value="信标信号测试")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "beaconTest")
public class BeaconTest implements Serializable {
    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    @ApiModelProperty(value="id")
    private Integer id;

    /**
     * 机型Id
     */
    @ApiModelProperty(value="机型Id")
    private String typeid;

    /**
     * 信标Id
     */
    @TableField(value = "beaconId")
    @ApiModelProperty(value="信标Id")
    private String beaconid;

    /**
     * 一米处信号强度
     */
    @TableField(value = "rssi_1")
    @ApiModelProperty(value="一米处信号强度")
    private String rssi1;

    /**
     * 信号强度
     */
    @TableField(value = "rssi")
    @ApiModelProperty(value="信号强度")
    private String rssi;

    /**
     * 时间戳
     */
    @TableField(value = "`timestamp`")
    @ApiModelProperty(value="时间戳")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;

    private static final long serialVersionUID = 1L;
}