package com.tgy.rtls.data.entity.map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 地图主题
 */
@ApiModel(value = "地图主题")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Maptheme implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 主题名称
     */
    @TableField(value = "themeName")
    @ApiModelProperty(value = "主题名称")
    private String themeName;

    /**
     * 地图Id
     */
    @TableField(value = "mapId")
    @ApiModelProperty(value = "地图Id")
    private String mapId;
    private static final long serialVersionUID = 1L;
}