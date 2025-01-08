package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/10/13
 * 标签
 */
@Data
@ToString
@ApiModel("差值")
public class MagDiff implements Serializable {
    private Integer id;
    @ApiModelProperty("标签编号")
    private Integer ned;
    private Short ned_state;
    private Integer infrared;
    private Short infrared_state;

}
