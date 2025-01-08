package com.tgy.rtls.data.entity.mapconfig;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
    * 跳转地图关联表
    */
@ApiModel(value="跳转地图关联表")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "park.jump_map_param")
public class JumpMapParam implements Serializable {

    @TableId(value = "id", type = IdType.INPUT)
    @ApiModelProperty(value="ID")
    private Long id;

    @TableField(value = "name")
    private String name;

    @TableField(value = "map_id")
    @ApiModelProperty(value="地图ID")
    private Long mapId;

    @TableField(value = "app_id")
    @ApiModelProperty(value="小程序ID")
    private Long appId;

    @TableField(value = "short_link")
    @ApiModelProperty(value="短链接(需要跳转的url链接)")
    private String shortLink;

    @TableField(value = "app_secret")
    @ApiModelProperty(value="app秘钥")
    private String appSecret;

    @ApiModelProperty(value ="创建时间" )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "createdTime")
    private Date createdTime;

    private String mapName;

    private static final long serialVersionUID = 1L;

    public static final String COL_ID = "id";

    public static final String COL_MAP_ID = "map_id";

    public static final String COL_APP_ID = "app_id";

    public static final String COL_SHORT_LINK = "short_link";
}