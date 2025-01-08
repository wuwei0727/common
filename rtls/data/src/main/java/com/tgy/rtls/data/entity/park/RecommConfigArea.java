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
*@CreateTime: 2023-05-12 14:35
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommConfigArea implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "x")
    private String x;

    @TableField(value = "y")
    private String y;

    private String floor;
    private String areaQuFen;

    private List<RecommConfigArea> points;

    @TableField(value = "areaId")
    private Long areaId;
    private Long areaPlaceId;
    private Long recommConfigId;

    private static final long serialVersionUID = 1L;

    public RecommConfigArea(String number, String number1, String number2, String number3) {
    }
}