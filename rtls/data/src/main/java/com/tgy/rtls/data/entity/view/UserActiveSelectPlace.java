package com.tgy.rtls.data.entity.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author wuwei
 * @date 2024/3/20 - 18:45
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)//链式编程
public class UserActiveSelectPlace {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    @TableField(value = "`user_id`")
    private Integer userId;
    @TableField(value = "`dest_id`")
    private Integer destId;
    @TableField(value = "`shang_jia_name`")
    private String shangJiaName;

    @TableField(value = "`map`")
    private String map;

    @TableField(value = "map_name")
    private String mapName;

    @TableField(value = "place")
    private String place;

    @TableField(value = "place_name")
    private String placeName;
    @TableField(value = "`desc`")
    private String desc;

    @TableField(value = "addTime")
    private LocalDateTime addtime;
}