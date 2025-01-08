package com.tgy.rtls.data.entity.map;

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
*@BelongsPackage: com.tgy.rtls.data.entity.map
*@Author: wuwei
*@CreateTime: 2024-12-05 17:13
*@Description: TODO
*@Version: 1.0
*/
/**
 * 用户反馈表
 */
@Data
@EqualsAndHashCode(callSuper=true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "feedback")
public class Feedback extends BaseEntitys implements Serializable {
    /**
     * 反馈类型
     */
    @TableField(value = "feedback_type")
    private String feedbackType;
    private String map;
    private String placeName;
    private String contactInfo;

    /**
     * 反馈内容
     */
    @TableField(value = "content")
    private String content;

    /**
     * 图片地址
     */
    @TableField(value = "image_url")
    private String imageUrl;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private Long userId;

    /**
     * 状态 0:未处理 1:已处理
     */
    @TableField(value = "`status`")
    private Integer status;

    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private String mapName;
}