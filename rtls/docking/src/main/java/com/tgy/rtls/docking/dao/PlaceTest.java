package com.tgy.rtls.docking.dao;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-06-08 19:50
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@Accessors(chain = true)
@TableName("parking_place")
public class PlaceTest implements Serializable {
    private Integer id;
    private String name;
    private Integer state;
    private Integer map;
    @TableField(value = "updateTime")
    private LocalDateTime updateTime;

    private Boolean detectionException;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime exceptionTime;

}
