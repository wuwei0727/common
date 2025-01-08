package com.tgy.rtls.data.entity.equip;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2020/12/23
 * uwb网关信息
 */
@Data
@ToString
@ApiModel(value = "uwb网关")
public class InfraredOriginCount implements Serializable {
    private Integer id;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;//创建时间

    private Integer gatewaynum;//地图id

    private Integer infrarednum;//网关ip

    private int total=4;
    private Integer total_start;
    private int total_receive;
    private int total_back;
    private String back_detail=" ";





    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }

}
