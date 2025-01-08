package com.tgy.rtls.data.entity.nb_device;


import lombok.Data;

@Data
public class BusinessCarportsData {



    private String  imei;//-- 可能为空
    private String  imsi;//IMSI，可能为空
    private String  nccid;//- NCCID，可能为空
    private String  outer_device_id;//- 第三方平台设备 ID
    private String  group_name;//分组名称
    private String  group_id;//分组 ID
    private String  park_no;//车位号
    private String  address;//安装地址
    private String  latitude;//纬度，坐标系 GCJ02，为空则不含本字段
    private String  longitude;//经度，为空则不含本字段

}
