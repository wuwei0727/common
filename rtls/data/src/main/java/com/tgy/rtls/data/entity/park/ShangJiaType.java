package com.tgy.rtls.data.entity.park;

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
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2024-11-12 09:42
*@Description: TODO
*@Version: 1.0
*/
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "shangjia_type")
public class ShangJiaType extends BaseEntitys implements Serializable {
    /**
     * 商家名字
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 图标
     */
    @TableField(value = "url")
    private String url;


    @TableField(exist = false)
    private String phone;

    private static final long serialVersionUID = 1L;
}