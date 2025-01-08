package com.tgy.rtls.data.entity.userinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author rtls
 * @since 2020-11-13
 */
@Data

public class WechatUserPosition implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer userid;
    private Integer map;
    private String x;
    private String y;
    private String niceName;
    private Integer floor;
    private String speed;//速度
    private String target;//目的地 自由行走时为null
    private String state;//状态  寻车，自由行走，找车位，出口导航，地点导航
    private String nav_state;//导航状态 开车或者步行，非导航状态时为null
    private String startX;//导航状态 开车或者步行，非导航状态时为null
    private String startY;//导航状态 开车或者步行，非导航状态时为null
    private String startFloor;//导航状态 开车或者步行，非导航状态时为null
    private String endX;//导航状态 开车或者步行，非导航状态时为null
    private String endY;//导航状态 开车或者步行，非导航状态时为null
    private String endFloor;//导航状态 开车或者步行，非导航状态时为null
    private Integer value=1;



}
