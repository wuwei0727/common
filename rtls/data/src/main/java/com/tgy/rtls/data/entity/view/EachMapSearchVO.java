package com.tgy.rtls.data.entity.view;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wuwei
 * @date 2024/3/7 - 15:33
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EachMapSearchVO {
    private Integer id;
    private String map;
    private String mapName;
    private String place;
    private String placeName;
    private String businessId;
    private String businessName;
    private String type;
}