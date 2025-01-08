package com.tgy.rtls.data.entity.equip;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/23
 * uwb网关信息
 */
@Data
@ToString
@ApiModel(value = "uwb网关")
@TableName("infrared_original")
public class InfraredOrigin implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime timestamp;//创建时间


    private Integer gatewaynum;//地图id

    private Integer infrarednum;//网关ip
    private Short state;
    private Short count;

    private Integer rssi;//楼层
    private Long ot;
    private int time_count=1;
    private Integer power;




    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
