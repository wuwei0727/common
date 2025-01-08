package com.tgy.rtls.data.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author wuwei
 * @date 2024/3/21 - 17:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)//链式编程
public class NaviLogVo {
    private Integer id;
    private Integer userId;
    private Integer destId;
    private String shangJiaName;
    private String desc;
    private String map;
    private String mapName;
    private String place;
    private String placeName;
    private String type;
    private LocalDateTime addtime;
}
