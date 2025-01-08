package com.tgy.rtls.data.entity.warn;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.warn
*@Author: wuwei
*@CreateTime: 2024-10-25 09:23
*@Description: TODO
*@Version: 1.0
*/
/**
 * 车位白名单表，存储不参与报警检测的车位信息
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "whitelist_slots")
public class WhitelistSlots extends BaseEntitys implements Serializable {
    /**
     * 车位ID，表示不参与报警检测的车位
     */
    @TableField(value = "place_id")
    private String placeId;

    /**
     * 关联的报警配置ID，指明该车位白名单与哪个报警配置相关
     */
    @TableField(value = "config_id")
    private Integer configId;

    @TableField(exist = false)
    private String mapId;
    @TableField(exist = false)
    private String mapName;
    @TableField(exist = false)
    private String placeName;

    private static final long serialVersionUID = 1L;
}