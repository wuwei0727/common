package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.map.Map_2d;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.bean.user
 * @date 2019/10/10
 */

/*
 * 成员
 * */
@Data
@ToString
@ApiModel("用户表")
public class Member implements Serializable {
    private Integer uid;//用户ID
    private String membername;
    private String password;
    private String phone;
    private Integer cid;  //角色id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String loginTime;
    private String url;
    private Integer enabled;//0不启用1启用
    private String describe;
    private String cname;
    private List<Permission> permissions;

    private List<Map_2d> mapList;//地图
    @ApiModelProperty(value ="关联地图" )
    private String mapName;
    private String rawPassword;
    private String creatorId;//用户ID
}
