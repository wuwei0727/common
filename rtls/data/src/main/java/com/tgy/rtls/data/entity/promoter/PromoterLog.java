package com.tgy.rtls.data.entity.promoter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *@author wuwei
 *@date 2024/3/21 - 16:58
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "promoter_log")
public class PromoterLog {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "pro_info_id")
    private Integer proInfoId;
    @TableField(value = "user_id")
    private Integer userId;

    @TableField(value = "dest_id")
    private Integer destId;

    @TableField(value = "shangjia_name")
    private String shangjiaName;

    @TableField(value = "`map`")
    private String map;

    @TableField(value = "map_name")
    private String mapName;

    @TableField(value = "`desc`")
    private String desc;
}