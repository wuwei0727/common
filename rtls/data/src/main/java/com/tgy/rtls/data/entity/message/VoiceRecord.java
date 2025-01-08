package com.tgy.rtls.data.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.message
 * @date 2020/10/13
 * 语音发送
 */
@Data
@ToString
public class VoiceRecord implements Serializable {
    private Integer id;
    private int direction;//方向：0下行发送到标签 1上行到服务器
    private String title;//语音标题
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    private Integer status;//语音状态：0成功 1发送中 2发送完成 3已读
    private String file;//语音地址
    private String filelocal;//语音地址
    private String random;//唯一随机数：用于区分语音
    private Integer personid;//发送或接受文件的人员id
    private Integer instanceid;
}
