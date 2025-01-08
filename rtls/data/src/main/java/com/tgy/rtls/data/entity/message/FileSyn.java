package com.tgy.rtls.data.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.message
 * @date 2020/10/26
 * 通讯信息
 */
@Data
@ToString
public class FileSyn implements Serializable {
    private Integer personid;
    private String name;//人员名
    private String num;//人员编号
    private String title;//文字标签  语音自动生成  文字编写
    private String file;//文件信息
    private String filelocal;//文件信息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    private Integer type;//通讯类型 1文字 2语音
    private Integer direction;//0下发 1上传
    private Integer status;//状态：0成功 1已读 2发送失败 -1发送中
}
