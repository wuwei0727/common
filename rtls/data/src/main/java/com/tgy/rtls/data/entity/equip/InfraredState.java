package com.tgy.rtls.data.entity.equip;


import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
//车位检测
public class InfraredState implements Serializable {
    private Integer id;
    private String state;
    private String infrarednum;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date timestamp;//电压检测时间
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }


}
