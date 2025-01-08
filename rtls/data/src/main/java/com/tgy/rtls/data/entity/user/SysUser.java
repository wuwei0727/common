package com.tgy.rtls.data.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.map.Map_2d;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
/**
 * SmallApp用户信息表
 * @TableName sys_user
 */
@TableName(value ="sys_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SysUser implements Serializable {

    @ApiModelProperty(value ="用户ID" )
    @TableId(type = IdType.AUTO)
    private Integer userId;

    @ApiModelProperty(value ="用户名" )
    private String userName;

    @ApiModelProperty(value ="密码" )
    private String password;

    @ApiModelProperty(value ="登录时间" )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "login_time")
    private Date loginTime;

    @ApiModelProperty(value ="创建时间" )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "createdTime")
    private Date createdTime;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "updatedTime")
    private Date updatedTime;

    @ApiModelProperty(value ="地图id" )
    private String mapid;

    @ApiModelProperty(value ="地图" )
    private List<Map_2d> mapList;

    @ApiModelProperty(value ="关联地图" )
    private String mapName;

    @ApiModelProperty(value ="创建人用户id" )
    @TableField(value = "createuId")
    private Integer createuId;

    @ApiModelProperty(value ="原密码" )
    private String rawPassword;

    @ApiModelProperty(value ="0不启用1启用" )
    private Integer enable;
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}