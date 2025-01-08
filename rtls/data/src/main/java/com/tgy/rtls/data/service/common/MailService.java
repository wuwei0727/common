package com.tgy.rtls.data.service.common;

import java.util.Map;


public interface MailService {
    /**
     * TODO 发送带附件的邮件 , 需要进行重载方法
     */
    /**
     * 发送邮件
     * @param from 发送者邮箱
     * @param to  发送目的邮箱
     * @param subject  发送人
     * @param content  内容
     * @param filePath  附件列表
     * @return
     */
    Map<String, Object> sendAttachmentsMail(String from,String to, String subject, String content, String[] filePath);
}
