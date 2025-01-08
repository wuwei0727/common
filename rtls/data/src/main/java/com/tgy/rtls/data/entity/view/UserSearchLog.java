package com.tgy.rtls.data.entity.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 *@author wuwei
 *@date 2024/3/18 - 14:34
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserSearchLog {
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String map;

    private String mapName;

    private Integer count;
}