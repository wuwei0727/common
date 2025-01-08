package com.tgy.rtls.data.entity.park;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-05-16 11:33
*@Description: TODO
*@Version: 1.0
*/
/**
    * 显示屏配置区域表
    */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowScreenConfigArea implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "x")
    private String x;

    @TableField(value = "y")
    private String y;

    private String floor;


    @TableField(value = "z")
    private String z;

    /**
     * 显示屏id
     */
    @TableField(value = "sid")
    private Long sid;

    private String areaQuFen;
    private String floorName;

    private List<ShowScreenConfigArea> points;


    private static final long serialVersionUID = 1L;
}