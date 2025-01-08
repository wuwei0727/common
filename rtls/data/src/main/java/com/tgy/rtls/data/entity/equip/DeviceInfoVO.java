package com.tgy.rtls.data.entity.equip;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.entity.park.ParkingPlace;
import lombok.Data;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-03-18 14:06
 * @Description: TODO
 * @Version: 1.0
 */
@Data
public class DeviceInfoVO implements Serializable {
    private Integer id;
    private Integer infraredId;
    private Integer placeId;
    private String num;//id
    private Integer map;//地图id
    private Double x;
    private Double y;
    private Short floor;//楼层
    private String fid;
    private String fMapId;
    private String placeName;
    private String mac;
    private String beaconNum;
    private String z;
    private Integer type;

    private boolean changePlace = false;
    private boolean changeDevice = false;
    private List<DeviceInfoVO> infraredOfflineData;
    private List<ParkingPlace> placeNames;
    private Integer decimalNum;
    @TableField("rawProductId")
    private String rawProductId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//修改时间

    /**
     * 获取改变地方
     *
     * @return boolean
     */
    public boolean getChangePlace() {
        return changePlace;
    }

    /**
     * 设置改变地方
     *
     * @param changePlace 改变地方
     */
    public void setChangePlace(boolean changePlace) {
        this.changePlace = changePlace;
    }

    /**
     * 获取改变设备
     *
     * @return boolean
     */
    public boolean getChangeDevice() {
        return changeDevice;
    }

    /**
     * 设置更改设备
     *
     * @param changeDevice 改变设备
     */
    public void setChangeDevice(boolean changeDevice) {
        this.changeDevice = changeDevice;
    }

    /**
     * 字符串
     *
     * @return {@link String}
     */
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }


}
