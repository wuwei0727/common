package com.tgy.rtls.data.entity.view;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author wuwei
 * @date 2024/3/7 - 14:57
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)//链式编程
public class EachMapSearchBusiness {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String map;

    private String mapName;

    private String businessId;
    private String businessName;
    private LocalDateTime addTime;
}