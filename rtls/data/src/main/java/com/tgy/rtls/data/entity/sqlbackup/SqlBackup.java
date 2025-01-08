package com.tgy.rtls.data.entity.sqlbackup;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.equip
 * @date 2021/1/4
 * 微基站
 */
@Data
@ToString
@ApiModel(value = "备份文件")
public class SqlBackup implements Serializable {
    @ApiModelProperty("基站自增id")
    private Integer id;
    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;//创建时间
/*    @ApiModelProperty("修改时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  */
    private String downloadPath;//
    private String localPath;//
    private Short flag;//
    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
