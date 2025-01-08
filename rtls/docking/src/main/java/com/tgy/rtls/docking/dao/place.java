package com.tgy.rtls.docking.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.docking.dao
 * @Author: wuwei
 * @CreateTime: 2023-12-08 17:14
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("parking_place")
public class place {
    private Integer id;
    private String name;
    private  Integer map;
    private  Short state=0;
    private  String license;
    private Short type;//0:普通车位  1充电车位
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime addTime;//创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime entryTime;//进入时间

}
