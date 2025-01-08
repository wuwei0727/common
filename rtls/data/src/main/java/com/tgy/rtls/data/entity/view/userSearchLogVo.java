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
 * @date 2024/3/19 - 14:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)//链式编程
public class userSearchLogVo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer lslogid;
    private Integer uslogid;
    private LocalDateTime time;
}
