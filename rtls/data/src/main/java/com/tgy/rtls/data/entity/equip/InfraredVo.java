package com.tgy.rtls.data.entity.equip;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wuwei
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.equip
 * @Author: wuwei
 * @CreateTime: 2023-03-21 14:32
 * @Description: TODO
 * @Version: 1.0
 * @date 2023/04/23
 */
@Data
public class InfraredVo implements Serializable {
    private String num;
    private String mapName;
    private String  floor;
    private String placeName;
    @TableField("rawProductId")
    private String rawProductId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;//创建时间

    private String networkstate;
    private String status;
    private String power;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date batteryTime;
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
