package com.tgy.rtls.data.entity.location;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.location
 * @date 2020/10/23
 * 定位数据
 */
@Data
@ToString
public class IpLocation implements Serializable {
    private String status;
    private String info;//区域
    private String infocode;//人员id
    private String city;
    private String adcode;
    private String province;
    private String rectangle;

}
