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
 * 文字发送
 */
@Data
@ToString
public class TextRecord implements Serializable {
    private Integer id;
    private String title;//文字标题
    private String file;//文字内容
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    private Integer status;//语音状态：0成功 1发送中 2发送完成 3已读
    private String random;//唯一随机数：
    private Integer personid;//发送或接受文件的人员id
    private Integer instanceid;
}
