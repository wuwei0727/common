package com.tgy.rtls.data.entity.map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:50
 */
@Data
public class MapBuildCommon {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer map;
    private String floor;
    private String fid;
    private String x;
    private String y;
    private String objectType;
    private String iconType;

    private String mapName;
    private String mapKey;
    private String appName;
    private String fmapID;
    private String mapImg;//地图路径
    private String themeImg;//主题路径

    private String type;
    private String floorName;
}
