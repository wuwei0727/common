package com.tgy.rtls.data.entity.user;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 人员和地图关联表
 * @TableName sys_user_map
 */
@TableName(value ="sys_user_map")
@Data
public class SysUserMap implements Serializable {
    /**
     * 人员ID
     */
    @TableId
    private Integer userId;

    /**
     * 地图ID
     */
    private Integer mapId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}