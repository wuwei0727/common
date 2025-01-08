package com.tgy.rtls.data.entity.map;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.tgy.rtls.data.entity.park.floorLock.BaseEntitys;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.map
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
/**
 * 反馈类型表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "feedback_type")
public class FeedbackType extends BaseEntitys implements Serializable {
    /**
     * 类型名称
     */
    @TableField(value = "type_name")
    private String typeName;

    /**
     * 类型编码
     */
    @TableField(value = "type_code")
    private String typeCode;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;

    /**
     * 状态 0:禁用 1:启用
     */
    @TableField(value = "`status`")
    private Integer status;

    private static final long serialVersionUID = 1L;
}